package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request;

import java.math.BigDecimal;

public record UpsertPricePlanRequest(
        String name,
        BigDecimal baseDailyRate,
        BigDecimal hourlyRate,
        BigDecimal weekendMultiplier,
        BigDecimal depositAmount,
        String currency
) {}
