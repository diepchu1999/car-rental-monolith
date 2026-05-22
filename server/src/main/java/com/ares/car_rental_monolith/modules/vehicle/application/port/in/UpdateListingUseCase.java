package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.command.UpdateListingCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;

@FunctionalInterface
public interface UpdateListingUseCase {
    VehicleDetail handle(UpdateListingCommand command);
}
