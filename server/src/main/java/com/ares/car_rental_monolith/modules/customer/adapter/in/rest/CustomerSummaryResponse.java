package com.ares.car_rental_monolith.modules.customer.adapter.in.rest;

import com.ares.car_rental_monolith.modules.customer.domain.CustomerSummary;
import java.util.UUID;

public record CustomerSummaryResponse(
        UUID id,
        String fullName,
        String phone,
        String email,
        String status,
        String hostCode
) {
    static CustomerSummaryResponse fromDomain(CustomerSummary s) {
        return new CustomerSummaryResponse(s.id(), s.fullName(), s.phone(),
                s.email(), s.status(), s.hostCode());
    }
}
