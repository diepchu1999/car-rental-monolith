package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.command.SyncVehicleImagesCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;

@FunctionalInterface
public interface SyncVehicleImagesUseCase {
    VehicleDetail handle(SyncVehicleImagesCommand command);
}
