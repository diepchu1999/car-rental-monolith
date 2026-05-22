package com.ares.car_rental_monolith.modules.vehicle.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Vehicle(
        UUID id,
        UUID ownerCustomerId,
        UUID fleetVehicleId,
        VehicleSource source,
        String brand,
        String model,
        String version,
        Integer manufactureYear,
        String licensePlate,
        Integer seats,
        VehicleTransmission transmission,
        VehicleFuelType fuelType,
        VehicleStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public boolean isActive() {
        return status == VehicleStatus.ACTIVE;
    }

    public boolean isHostOwned() {
        return source == VehicleSource.HOST_OWNED;
    }

    public boolean isCompanyOwned() {
        return source == VehicleSource.COMPANY_OWNED;
    }
}
