package com.ares.car_rental_monolith.modules.driver.application.port.in;

import com.ares.car_rental_monolith.modules.driver.domain.DriverSummary;
import java.util.UUID;

@FunctionalInterface
public interface GetDriverUseCase {
    DriverSummary handle(UUID driverId);
}
