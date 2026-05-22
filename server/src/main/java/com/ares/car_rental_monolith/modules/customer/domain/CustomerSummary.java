package com.ares.car_rental_monolith.modules.customer.domain;

import java.util.UUID;

public record CustomerSummary(
        UUID id,
        String fullName,
        String phone,
        String email,
        String status,
        String hostCode
) {}
