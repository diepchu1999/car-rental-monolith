package com.ares.car_rental_monolith.modules.fleet.domain;

import java.util.UUID;

public record BranchSummary(
        UUID id,
        String code,
        String name,
        String city,
        String status
) {}
