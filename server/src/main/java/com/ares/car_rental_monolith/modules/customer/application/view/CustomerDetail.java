package com.ares.car_rental_monolith.modules.customer.application.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Read model của customer aggregate. Field name mirror admin-web AdminCustomer
// để response map 1:1. Multi-KYC: một customer có thể có >=0 hồ sơ KYC; trang
// list không nạp danh sách KYC mà chỉ dùng kycAggregateStatus, trang detail
// nạp đầy đủ kycs + documents.
public record CustomerDetail(
        UUID id,
        String fullName,
        String phone,
        String email,
        LocalDate dateOfBirth,
        String gender,
        String status,
        OffsetDateTime joinedAt,
        List<String> roles,
        HostProfile hostProfile,
        List<Kyc> kycs,
        KycAggregateStatus kycAggregateStatus,
        List<Address> addresses,
        Activity activity
) {

    public record HostProfile(
            String hostCode,
            String displayName,
            String bio,
            BigDecimal ratingAverage,
            int ratingCount,
            String status,
            OffsetDateTime joinedAt
    ) {}

    public record Kyc(
            UUID id,
            String kycCode,
            String legalName,
            String documentType,
            String documentNumber,
            LocalDate issuedDate,
            String issuedPlace,
            String status,
            UUID reviewedBy,
            OffsetDateTime reviewedAt,
            String rejectionReason,
            OffsetDateTime submittedAt,
            List<Document> documents
    ) {

        public record Document(
                UUID id,
                String documentSide,
                String fileUrl,
                OffsetDateTime createdAt
        ) {}
    }

    public record Address(
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

    public record Activity(
            long bookingCount,
            long vehicleCount,
            BigDecimal totalRevenue
    ) {}

    public CustomerDetail withStatus(String newStatus) {
        return new CustomerDetail(
                id, fullName, phone, email, dateOfBirth, gender, newStatus, joinedAt,
                roles, hostProfile, kycs, kycAggregateStatus, addresses, activity);
    }

    public CustomerDetail withHostStatus(String newHostStatus) {
        HostProfile updated = new HostProfile(
                hostProfile.hostCode(), hostProfile.displayName(), hostProfile.bio(),
                hostProfile.ratingAverage(), hostProfile.ratingCount(), newHostStatus,
                hostProfile.joinedAt());
        return new CustomerDetail(
                id, fullName, phone, email, dateOfBirth, gender, status, joinedAt,
                roles, updated, kycs, kycAggregateStatus, addresses, activity);
    }
}
