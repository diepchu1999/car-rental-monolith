package com.ares.car_rental_monolith.modules.vehicle.application.command;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.math.BigDecimal;
import java.util.UUID;

public record UpsertPricePlanCommand(
        UUID vehicleId,
        String name,
        BigDecimal baseDailyRate,
        BigDecimal hourlyRate,
        BigDecimal weekendMultiplier,
        BigDecimal depositAmount,
        String currency
) {

    public static UpsertPricePlanCommand from(
            UUID vehicleId,
            String name,
            BigDecimal baseDailyRate,
            BigDecimal hourlyRate,
            BigDecimal weekendMultiplier,
            BigDecimal depositAmount,
            String currency
    ) {
        if (vehicleId == null) throw DomainException.validation("vehicleId is required");
        if (name == null || name.isBlank()) throw DomainException.validation("name is required");
        if (baseDailyRate == null || baseDailyRate.signum() <= 0) {
            throw DomainException.validation("baseDailyRate must be > 0");
        }
        return new UpsertPricePlanCommand(vehicleId, name.trim(), baseDailyRate,
                hourlyRate, weekendMultiplier, depositAmount,
                currency == null || currency.isBlank() ? "VND" : currency.trim());
    }
}
