package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleFuelType;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleSource;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatus;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleTransmission;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicles", schema = "vehicle")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class VehicleJpaEntity {

    @Id
    private UUID id;

    @Column(name = "owner_customer_id")
    private UUID ownerCustomerId;

    @Column(name = "fleet_vehicle_id")
    private UUID fleetVehicleId;

    @Enumerated(EnumType.STRING)
    private VehicleSource source;

    private String brand;
    private String model;
    private String version;

    @Column(name = "manufacture_year")
    private Integer manufactureYear;

    @Column(name = "license_plate")
    private String licensePlate;

    private Integer seats;

    @Enumerated(EnumType.STRING)
    private VehicleTransmission transmission;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type")
    private VehicleFuelType fuelType;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
