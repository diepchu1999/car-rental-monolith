package com.ares.car_rental_monolith.modules.customer.adapter.in.rest.request;

import java.time.LocalDate;

// Body của PATCH /admin/customers/{id}. Chỉ chứa các field admin được sửa
// trên form chỉnh sửa thông tin cá nhân. Mọi field về KYC / host / address /
// status đều KHÔNG nằm ở đây — kể cả khi FE có gửi nhầm, Command sẽ bỏ qua.
public record UpdateCustomerRequest(
        String fullName,
        String phone,
        String email,
        LocalDate dateOfBirth,
        String gender
) {
}
