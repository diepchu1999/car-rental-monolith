package com.ares.car_rental_monolith.modules.vehicle.infrastructure.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles", schema = "vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleJpaEntity {

    @Id
    private UUID id;

    @Column(name = "owner_customer_id")
    private UUID ownerCustomerId;

    @Column(name = "fleet_vehicle_id")
    private UUID fleetVehicleId;

    private String source;

    private String brand;

    private String model;

    private String version;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    @Column(name = "license_plate")
    private String licensePlate;

    private Integer seats;

    private String transmission;

    @Column(name = "fuel_type")
    private String fuelType;

    private String status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
