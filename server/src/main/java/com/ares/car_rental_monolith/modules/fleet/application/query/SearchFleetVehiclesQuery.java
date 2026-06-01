package com.ares.car_rental_monolith.modules.fleet.application.query;

import com.ares.car_rental_monolith.shared.api.PageParams;
import java.util.UUID;

public record SearchFleetVehiclesQuery(String q, UUID branchId, int page, int size) {

    public static SearchFleetVehiclesQuery from(String q, UUID branchId, Integer page, Integer size) {
        PageParams pp = PageParams.normalize(page, size, 20, 50);
        return new SearchFleetVehiclesQuery(q == null ? "" : q.trim(), branchId, pp.page(), pp.size());
    }

    public int pageIndex() {
        return page - 1;
    }
}
