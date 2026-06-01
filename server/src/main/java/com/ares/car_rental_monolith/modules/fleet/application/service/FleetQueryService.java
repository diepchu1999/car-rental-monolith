package com.ares.car_rental_monolith.modules.fleet.application.service;

import com.ares.car_rental_monolith.modules.fleet.application.port.in.GetFleetBranchUseCase;
import com.ares.car_rental_monolith.modules.fleet.application.port.in.ListBranchesUseCase;
import com.ares.car_rental_monolith.modules.fleet.application.port.in.SearchFleetVehiclesUseCase;
import com.ares.car_rental_monolith.modules.fleet.application.port.out.LoadFleetPort;
import com.ares.car_rental_monolith.modules.fleet.application.query.SearchFleetVehiclesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.BranchSummary;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetVehicleSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.List;
import java.util.UUID;

import com.ares.car_rental_monolith.shared.error.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class FleetQueryService implements SearchFleetVehiclesUseCase, ListBranchesUseCase, GetFleetBranchUseCase {

        private final LoadFleetPort port;

    FleetQueryService(LoadFleetPort port) {
        this.port = port;
    }

    @Override
    public PageResponse<FleetVehicleSummary> handle(SearchFleetVehiclesQuery query) {
        return port.searchFleetVehicles(query);
    }

    @Override
    public List<BranchSummary> handle() {
        return port.listActiveBranches();
    }


    @Override
    public FleetBranchDetail handle(UUID fleetId) {
        return port.getBranch(fleetId).orElseThrow(() -> DomainException.notFound("Fleet branch not found: " + fleetId));
    }
}
