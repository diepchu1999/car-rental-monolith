package com.ares.car_rental_monolith.modules.vehicle.domain;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public record VehicleListing(
        UUID id,
        UUID vehicleId,
        String title,
        String city,
        BigDecimal baseDailyRate,
        VehicleListingStatus status,
        OffsetDateTime publishedAt
) {

    private static final Set<VehicleListingStatus> PUBLISHABLE_FROM = EnumSet.of(
            VehicleListingStatus.DRAFT,
            VehicleListingStatus.PAUSED
    );

    // Apply a status action with the data needed for validation (e.g. image count
    // for publish). PublishContext carries the cross-aggregate facts that the
    // service has already loaded; the domain itself stays pure.
    public VehicleListing apply(VehicleListingStatusAction action, PublishContext context) {
        return switch (action) {
            case PUBLISH -> publish(context);
            case PAUSE -> transitionTo(VehicleListingStatus.PAUSED,
                    EnumSet.of(VehicleListingStatus.PUBLISHED), action);
            case ARCHIVE -> transitionTo(VehicleListingStatus.REJECTED,
                    EnumSet.allOf(VehicleListingStatus.class), action);
            case DRAFT -> transitionTo(VehicleListingStatus.DRAFT,
                    EnumSet.of(VehicleListingStatus.PUBLISHED, VehicleListingStatus.PAUSED,
                            VehicleListingStatus.REJECTED), action);
        };
    }

    private VehicleListing publish(PublishContext context) {
        if (!PUBLISHABLE_FROM.contains(status)) {
            throw DomainException.conflict(
                    "Cannot publish a listing from " + status.name() + " state");
        }
        if (baseDailyRate == null || baseDailyRate.signum() <= 0) {
            throw DomainException.validation("Listing must have base_daily_rate > 0 before publishing");
        }
        if (city == null || city.isBlank()) {
            throw DomainException.validation("Listing must have city before publishing");
        }
        if (context.imageCount() <= 0) {
            throw DomainException.validation("Listing must have at least one image before publishing");
        }
        return new VehicleListing(id, vehicleId, title, city, baseDailyRate,
                VehicleListingStatus.PUBLISHED,
                publishedAt == null ? OffsetDateTime.now() : publishedAt);
    }

    private VehicleListing transitionTo(VehicleListingStatus target,
                                         Set<VehicleListingStatus> allowedFrom,
                                         VehicleListingStatusAction action) {
        if (status == target) {
            throw DomainException.conflict("Listing is already " + target.name());
        }
        if (!allowedFrom.contains(status)) {
            throw DomainException.conflict(String.format(
                    "Cannot %s a listing from %s state", action.name(), status.name()));
        }
        return new VehicleListing(id, vehicleId, title, city, baseDailyRate,
                target, publishedAt);
    }

    public record PublishContext(int imageCount) {}
}
