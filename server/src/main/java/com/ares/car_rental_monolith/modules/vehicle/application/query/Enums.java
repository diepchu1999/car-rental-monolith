package com.ares.car_rental_monolith.modules.vehicle.application.query;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.Arrays;

// Shared enum parsing helper for query + command input. Strict by design:
// invalid values raise VALIDATION_ERROR (HTTP 400) instead of silently being
// dropped, so callers get a clear contract.
public final class Enums {

    private Enums() {}

    public static <E extends Enum<E>> E parseStrict(Class<E> type, String paramName, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw DomainException.validation(String.format(
                    "Invalid value '%s' for parameter '%s'. Allowed values: %s",
                    value, paramName, Arrays.toString(type.getEnumConstants())));
        }
    }
}
