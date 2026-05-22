package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.command.UpsertPricePlanCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;

@FunctionalInterface
public interface UpsertPricePlanUseCase {
    VehicleDetail handle(UpsertPricePlanCommand command);
}
