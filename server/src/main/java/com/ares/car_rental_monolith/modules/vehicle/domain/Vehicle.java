package com.ares.car_rental_monolith.modules.vehicle.domain;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;
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

    private static final Set<VehicleStatus> ACTIVATABLE_FROM = EnumSet.of(
            VehicleStatus.DRAFT,
            VehicleStatus.PENDING_REVIEW,
            VehicleStatus.INACTIVE,
            VehicleStatus.SUSPENDED
    );

    private static final Set<VehicleStatus> SUSPENDABLE_FROM = EnumSet.of(
            VehicleStatus.ACTIVE,
            VehicleStatus.INACTIVE,
            VehicleStatus.PENDING_REVIEW
    );

    private static final Set<VehicleStatus> DEACTIVATABLE_FROM = EnumSet.of(
            VehicleStatus.ACTIVE,
            VehicleStatus.SUSPENDED
    );

    public boolean isActive() {
        return status == VehicleStatus.ACTIVE;
    }

    public boolean isHostOwned() {
        return source == VehicleSource.HOST_OWNED;
    }

    public boolean isCompanyOwned() {
        return source == VehicleSource.COMPANY_OWNED;
    }

    // Returns a new Vehicle with the transition applied, or throws DomainException
    // if the action is not valid from the current status. Pure function — does not
    // touch persistence. Callers commit via the write port.
    public Vehicle apply(VehicleStatusAction action) {
        return switch (action) {
            case ACTIVATE -> transitionTo(VehicleStatus.ACTIVE, ACTIVATABLE_FROM, action);
            case SUSPEND -> transitionTo(VehicleStatus.SUSPENDED, SUSPENDABLE_FROM, action);
            case DEACTIVATE -> transitionTo(VehicleStatus.INACTIVE, DEACTIVATABLE_FROM, action);
        };
    }

    private Vehicle transitionTo(VehicleStatus target, Set<VehicleStatus> allowedFrom, VehicleStatusAction action) {
        if (status == target) {
            throw DomainException.conflict("Vehicle is already " + target.name());
        }
        if (!allowedFrom.contains(status)) {
            throw DomainException.conflict(String.format(
                    "Cannot %s a vehicle from %s state", action.name(), status.name()));
        }
        return new Vehicle(id, ownerCustomerId, fleetVehicleId, source,
                brand, model, version, manufactureYear, licensePlate, seats,
                transmission, fuelType, target, createdAt, OffsetDateTime.now());
    }
}
