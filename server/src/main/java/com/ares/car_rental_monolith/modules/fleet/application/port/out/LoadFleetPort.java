package com.ares.car_rental_monolith.modules.fleet.application.port.out;

import com.ares.car_rental_monolith.modules.fleet.application.query.SearchFleetVehiclesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.BranchSummary;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetVehicleSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadFleetPort {
    PageResponse<FleetVehicleSummary> searchFleetVehicles(SearchFleetVehiclesQuery query);
    List<BranchSummary> listActiveBranches();
    Optional<FleetBranchDetail> getBranch(UUID branchId);

}
