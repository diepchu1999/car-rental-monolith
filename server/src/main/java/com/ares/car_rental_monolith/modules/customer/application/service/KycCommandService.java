package com.ares.car_rental_monolith.modules.customer.application.service;

import com.ares.car_rental_monolith.modules.customer.application.command.ApproveKycCommand;
import com.ares.car_rental_monolith.modules.customer.application.command.CreateKycCommand;
import com.ares.car_rental_monolith.modules.customer.application.command.RejectKycCommand;
import com.ares.car_rental_monolith.modules.customer.application.port.in.ApproveKycUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.CreateKycUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.GetKycDetailUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.RejectKycUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.out.KycFileStoragePort;
import com.ares.car_rental_monolith.modules.customer.application.port.out.LoadCustomerPort;
import com.ares.car_rental_monolith.modules.customer.application.port.out.WriteCustomerPort;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import com.ares.car_rental_monolith.modules.customer.application.view.KycAggregateStatus;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Service riêng cho luồng KYC (create + get + approve + reject). Tách khỏi
// CustomerCommandService để mỗi service tập trung 1 nhóm nghiệp vụ — đỡ rối khi
// KYC nở thêm rule (re-submit, expire, ...).
@Service
@Transactional
class KycCommandService
        implements GetKycDetailUseCase, CreateKycUseCase, ApproveKycUseCase, RejectKycUseCase {

    private final LoadCustomerPort loadPort;
    private final WriteCustomerPort writePort;
    private final KycFileStoragePort fileStorage;

    KycCommandService(
            LoadCustomerPort loadPort,
            WriteCustomerPort writePort,
            KycFileStoragePort fileStorage) {
        this.loadPort = loadPort;
        this.writePort = writePort;
        this.fileStorage = fileStorage;
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDetail.Kyc handle(UUID customerId, UUID kycId) {
        return loadPort.loadKycForCustomer(customerId, kycId)
                .orElseThrow(() -> DomainException.notFound(
                        "KYC not found or does not belong to customer: " + kycId));
    }

    @Override
    public CustomerDetail.Kyc handle(CreateKycCommand command) {
        if (!loadPort.existsCustomer(command.customerId())) {
            throw DomainException.notFound("Customer not found: " + command.customerId());
        }

        OffsetDateTime now = OffsetDateTime.now();
        UUID kycId = UUID.randomUUID();
        String kycCode = "KYC-" + kycId.toString().substring(0, 8).toUpperCase();

        // Lưu file trước khi insert DB: nếu storage lỗi sẽ rollback transaction
        // mà không phát sinh row mồ côi. Ngược lại nếu insert DB fail sau khi
        // ghi file, transaction rollback nhưng file đã nằm trên đĩa — sẽ thành
        // "rác". Trade-off chấp nhận được cho luồng admin dev/test; cleanup
        // định kỳ có thể quét theo cây thư mục theo customerId/kycId nếu cần.
        List<CustomerDetail.Kyc.Document> docs = new ArrayList<>();
        addDocument(docs, command.customerId(), kycId, "FRONT", command.frontFile(), now);
        addDocument(docs, command.customerId(), kycId, "BACK", command.backFile(), now);
        addDocument(docs, command.customerId(), kycId, "SELFIE", command.selfieFile(), now);
        if (command.otherFiles() != null) {
            for (CreateKycCommand.FileInput f : command.otherFiles()) {
                addDocument(docs, command.customerId(), kycId, "OTHER", f, now);
            }
        }

        CustomerDetail.Kyc kyc = new CustomerDetail.Kyc(
                kycId, kycCode, command.legalName(), command.documentType(),
                command.documentNumber(), command.issuedDate(), command.issuedPlace(),
                "PENDING", null, null, null, now,
                List.copyOf(docs)
        );
        writePort.createKyc(command.customerId(), kyc);

        // Reload để có full state từ DB (kèm thứ tự documents do SQL ORDER BY
        // FRONT/BACK/SELFIE/OTHER quyết định, không phải thứ tự service build).
        return loadPort.loadKycForCustomer(command.customerId(), kycId)
                .orElseThrow(() -> new IllegalStateException(
                        "KYC vanished right after create: " + kycId));
    }

    private void addDocument(
            List<CustomerDetail.Kyc.Document> sink,
            UUID customerId,
            UUID kycId,
            String side,
            CreateKycCommand.FileInput file,
            OffsetDateTime now
    ) {
        if (file == null) return;
        String extension = CreateKycCommand.extensionOf(file.originalFilename());
        String fileUrl = fileStorage.store(customerId, kycId, side, file.content(), extension);
        sink.add(new CustomerDetail.Kyc.Document(UUID.randomUUID(), side, fileUrl, now));
    }

    @Override
    public CustomerDetail handle(ApproveKycCommand command) {
        CustomerDetail.Kyc current = requireKyc(command.customerId(), command.kycId());
        if ("APPROVED".equals(current.status())) {
            throw DomainException.validation("KYC is already approved: " + command.kycId());
        }
        int updated = writePort.approveKyc(
                command.kycId(), command.reviewedBy(), OffsetDateTime.now());
        if (updated == 0) {
            // requireKyc đã kiểm thuộc customer ở trên — UPDATE rớt 0 nghĩa là
            // bị xoá xen kẽ (rất hiếm). Trả notFound chuẩn hoá thành 404.
            throw DomainException.notFound("KYC vanished during approve: " + command.kycId());
        }
        return reloadAndPromoteIfReady(command.customerId());
    }

    @Override
    public CustomerDetail handle(RejectKycCommand command) {
        // Command đã validate rejectionReason ở from(); ở đây chỉ check domain.
        requireKyc(command.customerId(), command.kycId());
        int updated = writePort.rejectKyc(
                command.kycId(), command.reviewedBy(),
                command.rejectionReason(), OffsetDateTime.now());
        if (updated == 0) {
            throw DomainException.notFound("KYC vanished during reject: " + command.kycId());
        }
        return reloadCustomer(command.customerId());
    }

    private CustomerDetail.Kyc requireKyc(UUID customerId, UUID kycId) {
        return loadPort.loadKycForCustomer(customerId, kycId)
                .orElseThrow(() -> DomainException.notFound(
                        "KYC not found or does not belong to customer: " + kycId));
    }

    private CustomerDetail reloadCustomer(UUID customerId) {
        return loadPort.loadCustomerDetail(customerId)
                .orElseThrow(() -> DomainException.notFound(
                        "Customer not found: " + customerId));
    }

    // Sau khi approve KYC: nếu toàn bộ hồ sơ KYC đã được duyệt và customer
    // đang ở PENDING_KYC thì promote lên ACTIVE. Không tự hạ cấp ACTIVE →
    // PENDING_KYC để tránh đè quyết định trước đó của admin (vd customer
    // ban đầu được tạo ACTIVE không kèm KYC, sau này nộp KYC pending).
    private CustomerDetail reloadAndPromoteIfReady(UUID customerId) {
        CustomerDetail reloaded = reloadCustomer(customerId);
        if ("PENDING_KYC".equals(reloaded.status())
                && reloaded.kycAggregateStatus() == KycAggregateStatus.FULLY_APPROVED) {
            writePort.saveCustomerStatus(reloaded.withStatus("ACTIVE"));
            reloaded = reloadCustomer(customerId);
        }
        return reloaded;
    }
}
