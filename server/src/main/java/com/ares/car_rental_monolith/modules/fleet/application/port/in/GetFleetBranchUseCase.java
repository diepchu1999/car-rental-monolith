package com.ares.car_rental_monolith.modules.fleet.application.port.in;

import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;

import java.util.UUID;

@FunctionalInterface
public interface GetFleetBranchUseCase {
    FleetBranchDetail handle(UUID fleetId);
}
