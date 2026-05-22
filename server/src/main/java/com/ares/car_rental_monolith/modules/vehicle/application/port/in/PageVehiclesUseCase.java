package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.shared.api.PageResponse;

@FunctionalInterface
public interface PageVehiclesUseCase {
    PageResponse<Vehicle> handle(PageVehiclesQuery query);
}
