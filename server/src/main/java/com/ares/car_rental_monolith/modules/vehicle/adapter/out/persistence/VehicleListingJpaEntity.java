package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleListingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle_listings", schema = "vehicle")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class VehicleListingJpaEntity {

    @Id
    private UUID id;

    @Column(name = "vehicle_id")
    private UUID vehicleId;

    private String title;
    private String description;
    private String city;
    private String district;

    @Column(name = "province_code")
    private String provinceCode;

    @Column(name = "commune_code")
    private String communeCode;

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "base_daily_rate")
    private BigDecimal baseDailyRate;

    private String currency;

    @Column(name = "instant_booking_enabled")
    private Boolean instantBookingEnabled;

    @Column(name = "delivery_enabled")
    private Boolean deliveryEnabled;

    @Enumerated(EnumType.STRING)
    private VehicleListingStatus status;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
