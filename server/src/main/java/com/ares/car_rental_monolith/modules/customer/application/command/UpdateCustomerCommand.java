package com.ares.car_rental_monolith.modules.customer.application.command;

import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.request.UpdateCustomerRequest;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

// Validated input cho update thông tin cá nhân của customer. Không nhận field
// KYC / host / status / address — đây là rule nghiệp vụ "không sửa KYC qua
// form customer".
public record UpdateCustomerCommand(
        UUID customerId,
        String fullName,
        String phone,
        String email,
        LocalDate dateOfBirth,
        String gender
) {

    private static final Set<String> VALID_GENDERS = Set.of("MALE", "FEMALE", "OTHER");

    public static UpdateCustomerCommand from(UUID customerId, UpdateCustomerRequest r) {
        if (customerId == null) {
            throw DomainException.validation("customerId is required");
        }
        if (r == null) {
            throw DomainException.validation("body is required");
        }
        requireText(r.fullName(), "fullName");
        requireText(r.phone(), "phone");

        String gender = trimToNull(r.gender());
        if (gender != null && !VALID_GENDERS.contains(gender)) {
            throw DomainException.validation("gender must be one of MALE/FEMALE/OTHER");
        }

        return new UpdateCustomerCommand(
                customerId,
                r.fullName().trim(),
                r.phone().trim(),
                trimToNull(r.email()),
                r.dateOfBirth(),
                gender
        );
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw DomainException.validation(field + " is required");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
