package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminVehicleListItemResponse(
        UUID id,
        UUID ownerCustomerId,
        String ownerCustomerName,
        String hostCode,
        UUID fleetVehicleId,
        String assetCode,
        String branchName,
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
        String listingStatus,
        String city,
        String district,
        BigDecimal baseDailyRate,
        String coverImageUrl,
        long featureCount,
        long activeAvailabilityBlockCount,
        long bookingCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
