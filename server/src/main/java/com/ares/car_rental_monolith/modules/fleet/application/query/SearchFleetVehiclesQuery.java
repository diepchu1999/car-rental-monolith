package com.ares.car_rental_monolith.modules.fleet.application.query;

import java.util.UUID;

public record SearchFleetVehiclesQuery(String q, UUID branchId, int page, int size) {

    public static SearchFleetVehiclesQuery from(String q, UUID branchId, Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 20 : Math.min(size, 50);
        return new SearchFleetVehiclesQuery(q == null ? "" : q.trim(), branchId, safePage, safeSize);
    }

    public int pageIndex() {
        return page - 1;
    }
}
