package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle_features", schema = "vehicle")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class VehicleFeatureJpaEntity {

    @Id
    private UUID id;

    @Column(name = "vehicle_id")
    private UUID vehicleId;

    private String code;
    private String name;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
