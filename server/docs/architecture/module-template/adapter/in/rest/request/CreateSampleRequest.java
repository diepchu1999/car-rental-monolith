package com.ares.car_rental_monolith.modules.sample.adapter.in.rest.request;

// DTO request — chỉ là dữ liệu thô từ client. Validate ở Command.from(...).
public record CreateSampleRequest(String name) {
}
