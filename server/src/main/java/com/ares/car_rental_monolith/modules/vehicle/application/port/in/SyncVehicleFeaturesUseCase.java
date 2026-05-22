package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.command.SyncVehicleFeaturesCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;

@FunctionalInterface
public interface SyncVehicleFeaturesUseCase {
    VehicleDetail handle(SyncVehicleFeaturesCommand command);
}
