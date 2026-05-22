package com.ares.car_rental_monolith.modules.location.application.port.in;

import com.ares.car_rental_monolith.modules.location.domain.AdministrativeUnit;
import java.util.List;

@FunctionalInterface
public interface ListProvincesUseCase {
    List<AdministrativeUnit> handle();
}
