package com.ares.car_rental_monolith.modules.vehicle.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VehicleResponse {
    UUID id;
    UUID ownerCustomerId;
    UUID fleetVehicleId;
    String source;
    String brand;
    String model;
    String version;
    Integer manufactureYear;
    String licensePlate;
    Integer seats;
    String transmission;
    String fuelType;
    String status;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
