package com.ares.car_rental_monolith.modules.driver.application.port.out;

import com.ares.car_rental_monolith.modules.driver.application.query.SearchDriversQuery;
import com.ares.car_rental_monolith.modules.driver.domain.DriverSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.Optional;
import java.util.UUID;

public interface LoadDriverPort {
    PageResponse<DriverSummary> search(SearchDriversQuery query);
    Optional<DriverSummary> findById(UUID id);
}
