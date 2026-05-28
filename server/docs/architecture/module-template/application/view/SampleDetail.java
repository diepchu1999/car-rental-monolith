package com.ares.car_rental_monolith.modules.sample.application.view;

import java.time.OffsetDateTime;
import java.util.UUID;

// Read model trả ra ngoài. Field đặt trùng tên JSON mà frontend cần để response
// map 1:1. KHÔNG để read model ở package domain.
public record SampleDetail(
        UUID id,
        String name,
        String status,
        OffsetDateTime createdAt
) {
}
