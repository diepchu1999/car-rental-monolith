package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request;

import java.util.UUID;

public record CreateVehicleRequest(
        String source,
        UUID ownerCustomerId,
        String assetCode,
        UUID branchId,
        String brand,
        String model,
        String version,
        Integer manufactureYear,
        String licensePlate,
        Integer seats,
        String transmission,
        String fuelType
) {}
