package com.ares.car_rental_monolith.modules.vehicle.application.command;

import com.ares.car_rental_monolith.modules.vehicle.application.query.Enums;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatusAction;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;

public record ChangeVehicleStatusCommand(UUID vehicleId, VehicleStatusAction action) {

    public static ChangeVehicleStatusCommand from(UUID vehicleId, String action) {
        if (vehicleId == null) {
            throw DomainException.validation("vehicleId is required");
        }
        VehicleStatusAction parsed = Enums.parseStrict(VehicleStatusAction.class, "action", action);
        if (parsed == null) {
            throw DomainException.validation("action is required");
        }
        return new ChangeVehicleStatusCommand(vehicleId, parsed);
    }
}
