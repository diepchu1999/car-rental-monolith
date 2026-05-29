package com.ares.car_rental_monolith.modules.fleet.application.port.out;

import com.ares.car_rental_monolith.modules.fleet.application.query.ListFleetBranchesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;
import com.ares.car_rental_monolith.shared.api.PageResponse;

public interface LoadFleetBranchPort {
    PageResponse<FleetBranchDetail> loadFleetBranchesList(ListFleetBranchesQuery query);
}
