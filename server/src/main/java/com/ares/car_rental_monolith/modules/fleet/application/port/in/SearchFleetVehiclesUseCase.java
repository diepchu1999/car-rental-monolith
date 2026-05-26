package com.ares.car_rental_monolith.modules.fleet.application.port.in;

import com.ares.car_rental_monolith.modules.fleet.application.query.SearchFleetVehiclesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetVehicleSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;

@FunctionalInterface
public interface SearchFleetVehiclesUseCase {
    PageResponse<FleetVehicleSummary> handle(SearchFleetVehiclesQuery query);
}
