package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import java.util.UUID;

@FunctionalInterface
public interface GetVehicleUseCase {
    VehicleDetail handle(UUID vehicleId);
}
