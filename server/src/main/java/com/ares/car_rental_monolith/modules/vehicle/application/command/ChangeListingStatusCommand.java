package com.ares.car_rental_monolith.modules.vehicle.application.command;

import com.ares.car_rental_monolith.modules.vehicle.application.query.Enums;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleListingStatusAction;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;

public record ChangeListingStatusCommand(UUID vehicleId, VehicleListingStatusAction action) {

    public static ChangeListingStatusCommand from(UUID vehicleId, String action) {
        if (vehicleId == null) {
            throw DomainException.validation("vehicleId is required");
        }
        VehicleListingStatusAction parsed =
                Enums.parseStrict(VehicleListingStatusAction.class, "action", action);
        if (parsed == null) {
            throw DomainException.validation("action is required");
        }
        return new ChangeListingStatusCommand(vehicleId, parsed);
    }
}
