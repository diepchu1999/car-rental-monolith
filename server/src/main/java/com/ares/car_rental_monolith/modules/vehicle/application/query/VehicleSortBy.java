package com.ares.car_rental_monolith.modules.vehicle.application.query;

// Allowlist of sort columns to prevent SQL injection via the sortBy param.
// The `column` value is interpolated directly into SQL, so only safe identifiers
// belong here.
public enum VehicleSortBy {
    CREATED_AT("v.created_at"),
    UPDATED_AT("v.updated_at"),
    BASE_DAILY_RATE("vl.base_daily_rate"),
    MANUFACTURE_YEAR("v.manufacture_year"),
    BOOKING_COUNT("booking_count");

    private final String column;

    VehicleSortBy(String column) {
        this.column = column;
    }

    public String column() {
        return column;
    }
}
