package com.ares.car_rental_monolith.modules.fleet.api;

import java.util.UUID;

// Public, cross-module port of the fleet module. Other modules depend on this
// interface (not on fleet internals) to provision a company-vehicle asset record.
// Implemented inside the fleet module; runs in the caller's transaction.
public interface FleetVehicleProvisioning {

    // Creates the fleet.company_vehicles record for an already-allocated vehicle id
    // and returns the new company-vehicle id (used as vehicle.fleet_vehicle_id).
    UUID registerCompanyVehicle(RegisterCompanyVehicleCommand command);
}
