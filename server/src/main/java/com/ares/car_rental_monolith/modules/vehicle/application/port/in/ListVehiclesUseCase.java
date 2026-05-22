package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.query.ListVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import java.util.List;

@FunctionalInterface
public interface ListVehiclesUseCase {
    List<Vehicle> handle(ListVehiclesQuery query);
}
