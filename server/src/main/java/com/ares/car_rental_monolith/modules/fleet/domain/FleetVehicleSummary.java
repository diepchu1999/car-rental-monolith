package com.ares.car_rental_monolith.modules.fleet.domain;

import java.util.UUID;

public record FleetVehicleSummary(
        UUID id,
        UUID vehicleId,
        String assetCode,
        String assetStatus,
        UUID branchId,
        String branchName,
        String branchCity,
        String licensePlate
) {}
