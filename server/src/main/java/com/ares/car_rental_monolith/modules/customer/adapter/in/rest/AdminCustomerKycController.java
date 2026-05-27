package com.ares.car_rental_monolith.modules.customer.adapter.in.rest;

import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.request.RejectKycRequest;
import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.response.AdminCustomerDetailResponse;
import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.response.KycResponse;
import com.ares.car_rental_monolith.modules.customer.application.command.ApproveKycCommand;
import com.ares.car_rental_monolith.modules.customer.application.command.CreateKycCommand;
import com.ares.car_rental_monolith.modules.customer.application.command.RejectKycCommand;
import com.ares.car_rental_monolith.modules.customer.application.port.in.ApproveKycUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.CreateKycUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.GetKycDetailUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.RejectKycUseCase;
import com.ares.car_rental_monolith.shared.api.ApiResponse;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// Tách riêng KycController vì luồng KYC có nhóm endpoint riêng (create + get +
// approve + reject) và sẽ còn mở rộng (re-submit, expire...). URL vẫn bám
// resource cha /admin/customers/{customerId}/kyc/... vì KYC là sub-resource
// của customer.
@RestController
@RequestMapping("/api/v1/admin/customers/{customerId}/kyc")
public class AdminCustomerKycController {

    private final GetKycDetailUseCase getKycDetail;
    private final CreateKycUseCase createKyc;
    private final ApproveKycUseCase approveKyc;
    private final RejectKycUseCase rejectKyc;

    public AdminCustomerKycController(
            GetKycDetailUseCase getKycDetail,
            CreateKycUseCase createKyc,
            ApproveKycUseCase approveKyc,
            RejectKycUseCase rejectKyc
    ) {
        this.getKycDetail = getKycDetail;
        this.createKyc = createKyc;
        this.approveKyc = approveKyc;
        this.rejectKyc = rejectKyc;
    }

    // Multipart: 3 file fields cố định (frontFile/backFile/selfieFile) + 1
    // array otherFiles. Dùng @RequestParam thay vì @RequestPart để cho phép
    // các field text + file đến cùng request mà không phải khai báo DTO.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<KycResponse>> create(
            @PathVariable UUID customerId,
            @RequestParam("legalName") String legalName,
            @RequestParam("documentType") String documentType,
            @RequestParam("documentNumber") String documentNumber,
            @RequestParam(value = "issuedDate", required = false) String issuedDate,
            @RequestParam(value = "issuedPlace", required = false) String issuedPlace,
            @RequestParam(value = "frontFile", required = false) MultipartFile frontFile,
            @RequestParam(value = "backFile", required = false) MultipartFile backFile,
            @RequestParam(value = "selfieFile", required = false) MultipartFile selfieFile,
            @RequestParam(value = "otherFiles", required = false) List<MultipartFile> otherFiles
    ) {
        LocalDate parsedDate = parseDate(issuedDate);

        List<CreateKycCommand.FileInput> others = new ArrayList<>();
        if (otherFiles != null) {
            for (MultipartFile f : otherFiles) {
                CreateKycCommand.FileInput input = toFileInput(f);
                if (input != null) others.add(input);
            }
        }

        CreateKycCommand command = CreateKycCommand.from(
                customerId, legalName, documentType, documentNumber,
                parsedDate, issuedPlace,
                toFileInput(frontFile),
                toFileInput(backFile),
                toFileInput(selfieFile),
                others
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "CUSTOMER_KYC_CREATED", "Customer KYC created successfully",
                KycResponse.fromDomain(createKyc.handle(command))));
    }

    private static CreateKycCommand.FileInput toFileInput(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new CreateKycCommand.FileInput(
                content, file.getOriginalFilename(), file.getContentType());
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw DomainException.validation("issuedDate must be in yyyy-MM-dd format");
        }
    }

    @GetMapping("/{kycId}")
    public ResponseEntity<ApiResponse<KycResponse>> getKyc(
            @PathVariable UUID customerId,
            @PathVariable UUID kycId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "KYC_FETCHED", "KYC fetched",
                KycResponse.fromDomain(getKycDetail.handle(customerId, kycId))));
    }

    @PatchMapping("/{kycId}/approve")
    public ResponseEntity<ApiResponse<AdminCustomerDetailResponse>> approve(
            @PathVariable UUID customerId,
            @PathVariable UUID kycId
    ) {
        // reviewedBy = null cho tới khi auth context được gắn — controller
        // không tự chế ra user id "hệ thống".
        return ResponseEntity.ok(ApiResponse.success(
                "KYC_APPROVED", "KYC approved",
                AdminCustomerDetailResponse.fromDomain(
                        approveKyc.handle(ApproveKycCommand.from(customerId, kycId, null)))));
    }

    @PatchMapping("/{kycId}/reject")
    public ResponseEntity<ApiResponse<AdminCustomerDetailResponse>> reject(
            @PathVariable UUID customerId,
            @PathVariable UUID kycId,
            @RequestBody RejectKycRequest body
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "KYC_REJECTED", "KYC rejected",
                AdminCustomerDetailResponse.fromDomain(
                        rejectKyc.handle(RejectKycCommand.from(customerId, kycId, null, body)))));
    }
}
