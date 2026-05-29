package com.ares.car_rental_monolith.modules.fleet.application.port.in;

import com.ares.car_rental_monolith.modules.fleet.application.query.ListFleetBranchesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;
import com.ares.car_rental_monolith.shared.api.PageResponse;

@FunctionalInterface
public interface ListFleetBranchesUseCase {
    PageResponse<FleetBranchDetail> handle(ListFleetBranchesQuery query);
}
