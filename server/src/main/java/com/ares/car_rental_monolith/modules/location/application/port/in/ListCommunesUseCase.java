package com.ares.car_rental_monolith.modules.location.application.port.in;

import com.ares.car_rental_monolith.modules.location.domain.AdministrativeUnit;
import java.util.List;

@FunctionalInterface
public interface ListCommunesUseCase {
    List<AdministrativeUnit> handle(String provinceCode);
}
