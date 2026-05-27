package com.ares.car_rental_monolith.modules.customer.application.command;

import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.request.RejectKycRequest;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;

public record RejectKycCommand(
        UUID customerId,
        UUID kycId,
        UUID reviewedBy,
        String rejectionReason
) {

    public static RejectKycCommand from(
            UUID customerId, UUID kycId, UUID reviewedBy, RejectKycRequest body
    ) {
        if (customerId == null) {
            throw DomainException.validation("customerId is required");
        }
        if (kycId == null) {
            throw DomainException.validation("kycId is required");
        }
        if (body == null || body.rejectionReason() == null
                || body.rejectionReason().isBlank()) {
            throw DomainException.validation("rejectionReason is required");
        }
        return new RejectKycCommand(customerId, kycId, reviewedBy, body.rejectionReason().trim());
    }
}
