package com.ares.car_rental_monolith.modules.sample.application.command;

import com.ares.car_rental_monolith.modules.sample.adapter.in.rest.request.CreateSampleRequest;
import com.ares.car_rental_monolith.shared.error.DomainException;

// Input GHI đã được validate. Mọi kiểm tra hiện diện/enum làm trong from() để API
// trả 400 (VALIDATION_ERROR) thay vì lỗi DB/null ở tầng dưới.
public record CreateSampleCommand(String name) {

    public static CreateSampleCommand from(CreateSampleRequest r) {
        if (r.name() == null || r.name().isBlank()) {
            throw DomainException.validation("name is required");
        }
        return new CreateSampleCommand(r.name().trim());
    }
}
