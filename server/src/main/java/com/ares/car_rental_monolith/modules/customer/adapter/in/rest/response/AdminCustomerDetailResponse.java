package com.ares.car_rental_monolith.modules.customer.adapter.in.rest.response;

import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// REST representation của customer. Field name khớp admin-web AdminCustomer.
// Multi-KYC: `kycs` là list và `kycAggregateStatus` là trạng thái tổng hợp
// để FE hiển thị badge ở list/detail (NO_KYC/PENDING/PARTIALLY_APPROVED/
// FULLY_APPROVED/REJECTED).
public record AdminCustomerDetailResponse(
        UUID id,
        String fullName,
        String phone,
        String email,
        LocalDate dateOfBirth,
        String gender,
        String status,
        OffsetDateTime joinedAt,
        List<String> roles,
        HostProfileResponse hostProfile,
        List<KycResponse> kycs,
        String kycAggregateStatus,
        List<AddressResponse> addresses,
        ActivityResponse activity
) {

    public record HostProfileResponse(
            String hostCode,
            String displayName,
            String bio,
            BigDecimal ratingAverage,
            int ratingCount,
            String status,
            OffsetDateTime joinedAt
    ) {}

    public record AddressResponse(
            UUID id,
            String label,
            String line1,
            String provinceCode,
            String communeCode,
            String provinceName,
            String communeName,
            String legacyDistrict,
            boolean isDefault
    ) {}

    public record ActivityResponse(
            long bookingCount,
            long vehicleCount,
            BigDecimal totalRevenue
    ) {}

    public static AdminCustomerDetailResponse fromDomain(CustomerDetail c) {
        return new AdminCustomerDetailResponse(
                c.id(), c.fullName(), c.phone(), c.email(), c.dateOfBirth(),
                c.gender(), c.status(), c.joinedAt(), c.roles(),
                c.hostProfile() == null ? null : new HostProfileResponse(
                        c.hostProfile().hostCode(), c.hostProfile().displayName(),
                        c.hostProfile().bio(), c.hostProfile().ratingAverage(),
                        c.hostProfile().ratingCount(), c.hostProfile().status(),
                        c.hostProfile().joinedAt()),
                c.kycs().stream().map(KycResponse::fromDomain).toList(),
                c.kycAggregateStatus().name(),
                c.addresses().stream().map(a -> new AddressResponse(
                        a.id(), a.label(), a.line1(), a.provinceCode(), a.communeCode(),
                        a.provinceName(), a.communeName(), a.legacyDistrict(), a.isDefault()
                )).toList(),
                new ActivityResponse(
                        c.activity().bookingCount(), c.activity().vehicleCount(),
                        c.activity().totalRevenue())
        );
    }
}
