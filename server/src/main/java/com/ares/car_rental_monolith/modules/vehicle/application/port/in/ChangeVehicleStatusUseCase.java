package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.command.ChangeVehicleStatusCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;

@FunctionalInterface
public interface ChangeVehicleStatusUseCase {
    VehicleDetail handle(ChangeVehicleStatusCommand command);
}
