package com.ares.car_rental_monolith.modules.vehicle.application.view;

import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;

// Read-model assembled by the application layer from multiple sources
// (vehicle aggregate + customer profile). Lives in `application/view` to
// signal that it is a denormalized projection, not a domain entity.
public record VehicleDetail(
        Vehicle vehicle,
        String ownerCustomerName
) {
    public static VehicleDetail of(Vehicle vehicle, String ownerCustomerName) {
        return new VehicleDetail(vehicle, ownerCustomerName);
    }
}
