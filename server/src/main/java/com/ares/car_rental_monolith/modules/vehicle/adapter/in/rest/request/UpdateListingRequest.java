package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request;

import java.math.BigDecimal;

public record UpdateListingRequest(
        String title,
        String description,
        String provinceCode,
        String communeCode,
        String pickupAddress,
        BigDecimal baseDailyRate,
        String currency,
        Boolean instantBookingEnabled,
        Boolean deliveryEnabled
) {}
