package com.ares.car_rental_monolith.modules.location.application.port.in;

import com.ares.car_rental_monolith.modules.location.domain.AdministrativeUnit;
import java.util.List;

@FunctionalInterface
public interface SearchAdministrativeUnitsUseCase {
    // Autocomplete across active units. `level` optionally narrows to PROVINCE or
    // COMMUNE; `provinceCode` optionally narrows communes to one province.
    List<AdministrativeUnit> handle(String query, String level, String provinceCode);
}
