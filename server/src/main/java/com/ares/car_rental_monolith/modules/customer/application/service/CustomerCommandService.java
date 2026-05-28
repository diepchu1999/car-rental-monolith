package com.ares.car_rental_monolith.modules.customer.application.service;

import com.ares.car_rental_monolith.modules.customer.application.command.ChangeCustomerStatusCommand;
import com.ares.car_rental_monolith.modules.customer.application.command.ChangeHostStatusCommand;
import com.ares.car_rental_monolith.modules.customer.application.command.CreateCustomerCommand;
import com.ares.car_rental_monolith.modules.customer.application.command.UpdateCustomerCommand;
import com.ares.car_rental_monolith.modules.customer.application.port.in.ChangeCustomerStatusUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.ChangeHostStatusUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.CreateCustomerUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.UpdateCustomerUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.out.LoadCustomerPort;
import com.ares.car_rental_monolith.modules.customer.application.port.out.WriteCustomerPort;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import com.ares.car_rental_monolith.modules.customer.application.view.KycAggregateStatus;
import com.ares.car_rental_monolith.modules.location.api.AdministrativeUnitDirectory;
import com.ares.car_rental_monolith.modules.location.api.AdministrativeUnitRef;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class CustomerCommandService implements
        CreateCustomerUseCase,
        UpdateCustomerUseCase,
        ChangeCustomerStatusUseCase,
        ChangeHostStatusUseCase {

    private final WriteCustomerPort writePort;
    private final LoadCustomerPort loadPort;
    private final AdministrativeUnitDirectory administrativeUnits;

    CustomerCommandService(
            WriteCustomerPort writePort,
            LoadCustomerPort loadPort,
            AdministrativeUnitDirectory administrativeUnits
    ) {
        this.writePort = writePort;
        this.loadPort = loadPort;
        this.administrativeUnits = administrativeUnits;
    }

    @Override
    public CustomerDetail handle(CreateCustomerCommand command) {
        OffsetDateTime now = OffsetDateTime.now();
        UUID customerId = UUID.randomUUID();

        // Admin-created customer: nếu kèm KYC ban đầu thì status PENDING_KYC để
        // hệ thống biết còn cần duyệt; ngược lại để ACTIVE.
        String status = command.kyc() != null ? "PENDING_KYC" : "ACTIVE";

        CustomerDetail.HostProfile host = command.host() == null ? null : buildHost(command.host(), now);
        List<CustomerDetail.Kyc> kycs = command.kyc() == null
                ? List.of()
                : List.of(buildKyc(command.kyc(), now));
        List<CustomerDetail.Address> addresses = command.address() == null
                ? List.of()
                : List.of(buildAddress(command.address()));

        CustomerDetail detail = new CustomerDetail(
                customerId,
                command.fullName(),
                command.phone(),
                command.email(),
                command.dateOfBirth(),
                command.gender(),
                status,
                now,
                command.roles(),
                host,
                kycs,
                KycAggregateStatus.from(kycs),
                addresses,
                new CustomerDetail.Activity(0, 0, BigDecimal.ZERO)
        );

        writePort.create(detail);
        return detail;
    }

    @Override
    public CustomerDetail handle(UpdateCustomerCommand command) {
        CustomerDetail current = requireCustomer(command.customerId());
        // Build snapshot mới chỉ thay 5 field cơ bản; KYC / host / address /
        // status / roles giữ nguyên (rule nghiệp vụ: không sửa KYC qua form
        // customer). Adapter UPDATE thẳng theo các field này.
        CustomerDetail updated = new CustomerDetail(
                current.id(),
                command.fullName(),
                command.phone(),
                command.email(),
                command.dateOfBirth(),
                command.gender(),
                current.status(),
                current.joinedAt(),
                current.roles(),
                current.hostProfile(),
                current.kycs(),
                current.kycAggregateStatus(),
                current.addresses(),
                current.activity()
        );
        writePort.saveCustomerBasics(updated);
        return requireCustomer(command.customerId());
    }

    @Override
    public CustomerDetail handle(ChangeCustomerStatusCommand command) {
        CustomerDetail current = requireCustomer(command.customerId());
        writePort.saveCustomerStatus(current.withStatus(command.targetStatus()));
        return requireCustomer(command.customerId());
    }

    @Override
    public CustomerDetail handle(ChangeHostStatusCommand command) {
        CustomerDetail current = requireCustomer(command.customerId());
        if (current.hostProfile() == null) {
            throw DomainException.validation(
                    "Customer is not a host: " + command.customerId());
        }
        writePort.saveHostStatus(current.withHostStatus(command.targetStatus()));
        return requireCustomer(command.customerId());
    }

    private CustomerDetail requireCustomer(UUID customerId) {
        return loadPort.loadCustomerDetail(customerId)
                .orElseThrow(() -> DomainException.notFound(
                        "Customer not found: " + customerId));
    }

    private CustomerDetail.HostProfile buildHost(CreateCustomerCommand.Host host, OffsetDateTime now) {
        String hostCode = host.hostCode() != null ? host.hostCode() : generateHostCode();
        return new CustomerDetail.HostProfile(
                hostCode, host.displayName(), host.bio(),
                BigDecimal.ZERO, 0, "ACTIVE", now
        );
    }

    private CustomerDetail.Kyc buildKyc(CreateCustomerCommand.Kyc kyc, OffsetDateTime now) {
        UUID kycId = UUID.randomUUID();
        String kycCode = "KYC-" + kycId.toString().substring(0, 8).toUpperCase();
        return new CustomerDetail.Kyc(
                kycId,
                kycCode,
                kyc.legalName(),
                kyc.documentType(),
                kyc.documentNumber(),
                kyc.issuedDate(),
                kyc.issuedPlace(),
                "PENDING",
                null,   // reviewedBy
                null,   // reviewedAt
                null,   // rejectionReason
                now,    // submittedAt
                List.of()
        );
    }

    private CustomerDetail.Address buildAddress(CreateCustomerCommand.Address address) {
        AdministrativeUnitRef province = administrativeUnits.findActiveByCode(address.provinceCode())
                .orElseThrow(() -> DomainException.validation(
                        "provinceCode does not exist or is not active: " + address.provinceCode()));
        if (!"PROVINCE".equals(province.level())) {
            throw DomainException.validation("provinceCode is not a province: " + address.provinceCode());
        }

        AdministrativeUnitRef commune = administrativeUnits.findActiveByCode(address.communeCode())
                .orElseThrow(() -> DomainException.validation(
                        "communeCode does not exist or is not active: " + address.communeCode()));
        if (!"COMMUNE".equals(commune.level())) {
            throw DomainException.validation("communeCode is not a commune: " + address.communeCode());
        }
        if (!province.code().equals(commune.parentCode())) {
            throw DomainException.validation("communeCode " + address.communeCode()
                    + " does not belong to province " + address.provinceCode());
        }

        return new CustomerDetail.Address(
                UUID.randomUUID(),
                address.label() != null ? address.label() : "Mặc định",
                address.line1(),
                province.code(), commune.code(),
                province.name(), commune.name(),
                null, true
        );
    }

    private static String generateHostCode() {
        return "HOST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
