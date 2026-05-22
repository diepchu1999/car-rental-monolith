package com.ares.car_rental_monolith.modules.driver.application.port.in;

import com.ares.car_rental_monolith.modules.driver.application.query.SearchDriversQuery;
import com.ares.car_rental_monolith.modules.driver.domain.DriverSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;

@FunctionalInterface
public interface SearchDriversUseCase {
    PageResponse<DriverSummary> handle(SearchDriversQuery query);
}
