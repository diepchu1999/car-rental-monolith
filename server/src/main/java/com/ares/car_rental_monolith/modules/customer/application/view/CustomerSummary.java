package com.ares.car_rental_monolith.modules.customer.application.view;

import java.util.UUID;

// Lightweight read projection for the customer search/picker.
public record CustomerSummary(
        UUID id,
        String fullName,
        String phone,
        String email,
        String status,
        String hostCode
) {}
