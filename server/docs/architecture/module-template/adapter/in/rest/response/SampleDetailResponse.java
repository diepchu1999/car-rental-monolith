package com.ares.car_rental_monolith.modules.sample.adapter.in.rest.response;

import com.ares.car_rental_monolith.modules.sample.application.view.SampleDetail;
import java.time.OffsetDateTime;
import java.util.UUID;

// DTO response. Field trùng tên JSON frontend cần. Có static fromDomain(view).
public record SampleDetailResponse(
        UUID id,
        String name,
        String status,
        OffsetDateTime createdAt
) {
    public static SampleDetailResponse fromDomain(SampleDetail s) {
        return new SampleDetailResponse(s.id(), s.name(), s.status(), s.createdAt());
    }
}
