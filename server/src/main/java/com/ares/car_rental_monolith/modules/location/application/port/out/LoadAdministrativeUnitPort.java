package com.ares.car_rental_monolith.modules.location.application.port.out;

import com.ares.car_rental_monolith.modules.location.domain.AdministrativeUnit;
import java.util.List;
import java.util.Optional;

public interface LoadAdministrativeUnitPort {

    List<AdministrativeUnit> listByLevel(String level);

    List<AdministrativeUnit> listCommunesByProvince(String provinceCode);

    List<AdministrativeUnit> search(String query, String level, String provinceCode, int limit);

    Optional<AdministrativeUnit> findActiveByCode(String code);
}
