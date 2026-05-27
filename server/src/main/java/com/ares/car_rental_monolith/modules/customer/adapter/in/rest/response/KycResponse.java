package com.ares.car_rental_monolith.modules.customer.adapter.in.rest.response;

import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Response chia sẻ giữa:
//   - section `kycs` trong AdminCustomerDetailResponse (list per customer).
//   - endpoint GET /admin/customers/{id}/kyc/{kycId} (1 hồ sơ riêng).
// Flat shape (documents nằm trong) đồng nhất với cách BE đang nest KYC trong
// customer detail; FE alias type tên thân thiện ở phía client.
public record KycResponse(
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
        List<DocumentResponse> documents
) {

    public record DocumentResponse(
            UUID id,
            String documentSide,
            String fileUrl,
            OffsetDateTime createdAt
    ) {
        public static DocumentResponse fromDomain(CustomerDetail.Kyc.Document d) {
            return new DocumentResponse(d.id(), d.documentSide(), d.fileUrl(), d.createdAt());
        }
    }

    public static KycResponse fromDomain(CustomerDetail.Kyc k) {
        return new KycResponse(
                k.id(), k.kycCode(), k.legalName(), k.documentType(), k.documentNumber(),
                k.issuedDate(), k.issuedPlace(), k.status(),
                k.reviewedBy(), k.reviewedAt(), k.rejectionReason(), k.submittedAt(),
                k.documents().stream().map(DocumentResponse::fromDomain).toList()
        );
    }
}
