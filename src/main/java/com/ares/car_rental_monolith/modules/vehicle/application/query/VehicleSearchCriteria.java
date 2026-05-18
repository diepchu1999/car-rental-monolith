package com.ares.car_rental_monolith.modules.vehicle.application.query;

public record VehicleSearchCriteria(
        String source,
        String status
) {

    public static VehicleSearchCriteria from(String source, String status) {
        return new VehicleSearchCriteria(normalize(source), normalize(status));
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }
}
