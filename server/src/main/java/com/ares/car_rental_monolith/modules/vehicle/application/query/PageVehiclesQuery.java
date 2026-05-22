package com.ares.car_rental_monolith.modules.vehicle.application.query;

import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleSource;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatus;

public record PageVehiclesQuery(
        String q,
        VehicleSource source,
        VehicleStatus status,
        int page,
        int size
) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public static PageVehiclesQuery from(
            String q, String source, String status, Integer page, Integer size
    ) {
        int safePage = (page == null || page < 1) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return new PageVehiclesQuery(
                normalizeSearch(q),
                Enums.parse(VehicleSource.class, source),
                Enums.parse(VehicleStatus.class, status),
                safePage,
                safeSize
        );
    }

    public int pageIndex() {
        return page - 1;
    }

    public String sourceCode() {
        return source == null ? null : source.name();
    }

    public String statusCode() {
        return status == null ? null : status.name();
    }

    private static String normalizeSearch(String value) {
        if (value == null || value.isBlank()) return "";
        return value.trim().toLowerCase();
    }
}
