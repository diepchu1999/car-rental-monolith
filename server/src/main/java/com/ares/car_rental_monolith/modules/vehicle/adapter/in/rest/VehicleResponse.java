package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        UUID ownerCustomerId,
        String ownerCustomerName,
        UUID fleetVehicleId,
        String source,
        String brand,
        String model,
        String version,
        Integer manufactureYear,
        String licensePlate,
        Integer seats,
        String transmission,
        String fuelType,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
