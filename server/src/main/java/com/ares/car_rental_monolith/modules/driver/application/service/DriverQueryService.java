package com.ares.car_rental_monolith.modules.driver.application.service;

import com.ares.car_rental_monolith.modules.driver.application.port.in.GetDriverUseCase;
import com.ares.car_rental_monolith.modules.driver.application.port.in.SearchDriversUseCase;
import com.ares.car_rental_monolith.modules.driver.application.port.out.LoadDriverPort;
import com.ares.car_rental_monolith.modules.driver.application.query.SearchDriversQuery;
import com.ares.car_rental_monolith.modules.driver.domain.DriverSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class DriverQueryService implements SearchDriversUseCase, GetDriverUseCase {

    private final LoadDriverPort port;

    DriverQueryService(LoadDriverPort port) {
        this.port = port;
    }

    @Override
    public PageResponse<DriverSummary> handle(SearchDriversQuery query) {
        return port.search(query);
    }

    @Override
    public DriverSummary handle(UUID driverId) {
        return port.findById(driverId)
                .orElseThrow(() -> DomainException.notFound("Driver not found: " + driverId));
    }
}
