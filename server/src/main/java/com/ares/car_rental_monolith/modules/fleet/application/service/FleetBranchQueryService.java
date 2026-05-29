package com.ares.car_rental_monolith.modules.fleet.application.service;

import com.ares.car_rental_monolith.modules.fleet.application.port.in.ListFleetBranchesUseCase;
import com.ares.car_rental_monolith.modules.fleet.application.port.out.LoadFleetBranchPort;
import com.ares.car_rental_monolith.modules.fleet.application.query.ListFleetBranchesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FleetBranchQueryService implements ListFleetBranchesUseCase {

    private final LoadFleetBranchPort  loadFleetBranchPort;

    FleetBranchQueryService(LoadFleetBranchPort loadFleetBranchPort) {
        this.loadFleetBranchPort = loadFleetBranchPort;
    }

    @Override
    public PageResponse<FleetBranchDetail> handle(ListFleetBranchesQuery query) {
        return loadFleetBranchPort.loadFleetBranchesList(query);
    }
}
