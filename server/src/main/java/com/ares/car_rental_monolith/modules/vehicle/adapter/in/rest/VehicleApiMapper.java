package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest;

import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;

final class VehicleApiMapper {

    private VehicleApiMapper() {}

    // List/page responses do not enrich the owner name to avoid N+1 lookups.
    static VehicleResponse toResponse(Vehicle v) {
        return build(v, null);
    }

    static VehicleResponse toResponse(VehicleDetail detail) {
        return build(detail.vehicle(), detail.ownerCustomerName());
    }

    private static VehicleResponse build(Vehicle v, String ownerName) {
        return new VehicleResponse(
                v.id(),
                v.ownerCustomerId(),
                ownerName,
                v.fleetVehicleId(),
                enumName(v.source()),
                v.brand(),
                v.model(),
                v.version(),
                v.manufactureYear(),
                v.licensePlate(),
                v.seats(),
                enumName(v.transmission()),
                enumName(v.fuelType()),
                enumName(v.status()),
                v.createdAt(),
                v.updatedAt()
        );
    }

    private static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
