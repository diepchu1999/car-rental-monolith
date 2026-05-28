package com.ares.car_rental_monolith.modules.customer.application.command;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;

public record ApproveKycCommand(UUID customerId, UUID kycId, UUID reviewedBy) {

    public static ApproveKycCommand from(UUID customerId, UUID kycId, UUID reviewedBy) {
        if (customerId == null) {
            throw DomainException.validation("customerId is required");
        }
        if (kycId == null) {
            throw DomainException.validation("kycId is required");
        }
        // reviewedBy có thể null trong giai đoạn chưa có auth context — vẫn ghi
        // được, FE hiển thị "—". Khi auth gắn, controller sẽ truyền user id thật.
        return new ApproveKycCommand(customerId, kycId, reviewedBy);
    }
}
