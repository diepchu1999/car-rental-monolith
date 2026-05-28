package com.ares.car_rental_monolith.modules.sample.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

// Aggregate / entity nghiệp vụ THUẦN — không import Spring/JPA. Đặt quy tắc nghiệp
// vụ và (nếu có) hành vi chuyển trạng thái ở đây.
public record Sample(
        UUID id,
        String name,
        SampleStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
