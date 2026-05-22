package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.command.ChangeListingStatusCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;

@FunctionalInterface
public interface ChangeListingStatusUseCase {
    VehicleDetail handle(ChangeListingStatusCommand command);
}
