package com.ares.car_rental_monolith.modules.sample.api;

import java.util.UUID;

// (TÙY CHỌN) DTO chia sẻ cross-module. Record bất biến, chỉ chứa dữ liệu module
// khác được phép thấy.
public record SampleRef(
        UUID id,
        String name,
        String status
) {
}
