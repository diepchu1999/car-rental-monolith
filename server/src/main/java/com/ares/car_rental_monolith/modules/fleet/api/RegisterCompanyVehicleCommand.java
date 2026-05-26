package com.ares.car_rental_monolith.modules.fleet.api;

import java.util.UUID;

// Cross-module input contract. The vehicle module allocates the vehicle id and
// asks fleet to materialise the matching company-vehicle asset record. branchId
// is optional (a vehicle can be onboarded before being assigned to a branch).
public record RegisterCompanyVehicleCommand(
        UUID vehicleId,
        String assetCode,
        UUID branchId
) {}
