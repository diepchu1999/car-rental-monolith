package com.ares.car_rental_monolith.modules.vehicle.application.command;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateListingCommand(
        UUID vehicleId,
        String title,
        String description,
        String provinceCode,
        String communeCode,
        // Resolved display names for the codes above. Filled by the service after
        // validating the codes against the administrative-unit catalog; null when
        // the command is first built from the request.
        String provinceName,
        String communeName,
        String pickupAddress,
        BigDecimal baseDailyRate,
        String currency,
        Boolean instantBookingEnabled,
        Boolean deliveryEnabled
) {
    public static UpdateListingCommand from(
            UUID vehicleId,
            String title, String description,
            String provinceCode, String communeCode, String pickupAddress,
            BigDecimal baseDailyRate, String currency,
            Boolean instantBookingEnabled, Boolean deliveryEnabled
    ) {
        if (vehicleId == null) throw DomainException.validation("vehicleId is required");
        if (isBlank(provinceCode)) throw DomainException.validation("provinceCode is required");
        if (isBlank(communeCode)) throw DomainException.validation("communeCode is required");
        if (baseDailyRate != null && baseDailyRate.signum() < 0) {
            throw DomainException.validation("baseDailyRate must be >= 0");
        }
        return new UpdateListingCommand(vehicleId,
                trim(title), trim(description),
                trim(provinceCode), trim(communeCode), null, null,
                trim(pickupAddress), baseDailyRate, trim(currency),
                instantBookingEnabled, deliveryEnabled);
    }

    // Returns a copy with the resolved province/commune display names attached.
    public UpdateListingCommand withResolvedLocation(String resolvedProvinceName, String resolvedCommuneName) {
        return new UpdateListingCommand(vehicleId, title, description,
                provinceCode, communeCode, resolvedProvinceName, resolvedCommuneName,
                pickupAddress, baseDailyRate, currency,
                instantBookingEnabled, deliveryEnabled);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
