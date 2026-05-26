package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleListItem;
import com.ares.car_rental_monolith.shared.api.PageResponse;

@FunctionalInterface
public interface PageVehicleListUseCase {
    PageResponse<VehicleListItem> handle(PageVehiclesQuery query);
}