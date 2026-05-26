package com.ares.car_rental_monolith.modules.location.application.service;

import com.ares.car_rental_monolith.modules.location.api.AdministrativeUnitDirectory;
import com.ares.car_rental_monolith.modules.location.api.AdministrativeUnitRef;
import com.ares.car_rental_monolith.modules.location.application.port.in.ListCommunesUseCase;
import com.ares.car_rental_monolith.modules.location.application.port.in.ListProvincesUseCase;
import com.ares.car_rental_monolith.modules.location.application.port.in.SearchAdministrativeUnitsUseCase;
import com.ares.car_rental_monolith.modules.location.application.port.out.LoadAdministrativeUnitPort;
import com.ares.car_rental_monolith.modules.location.domain.AdministrativeLevel;
import com.ares.car_rental_monolith.modules.location.domain.AdministrativeUnit;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class AdministrativeUnitQueryService implements
        ListProvincesUseCase,
        ListCommunesUseCase,
        SearchAdministrativeUnitsUseCase,
        AdministrativeUnitDirectory {

    private static final int SEARCH_LIMIT = 30;

    private final LoadAdministrativeUnitPort port;

    AdministrativeUnitQueryService(LoadAdministrativeUnitPort port) {
        this.port = port;
    }

    @Override
    public List<AdministrativeUnit> handle() {
        return port.listByLevel(AdministrativeLevel.PROVINCE.name());
    }

    @Override
    public List<AdministrativeUnit> handle(String provinceCode) {
        if (provinceCode == null || provinceCode.isBlank()) {
            throw DomainException.validation("provinceCode is required");
        }
        return port.listCommunesByProvince(provinceCode.trim());
    }

    @Override
    public List<AdministrativeUnit> handle(String query, String level, String provinceCode) {
        String q = query == null ? "" : query.trim();
        String lvl = normalizeLevel(level);
        String parent = (provinceCode == null || provinceCode.isBlank()) ? null : provinceCode.trim();
        return port.search(q, lvl, parent, SEARCH_LIMIT);
    }

    @Override
    public Optional<AdministrativeUnitRef> findActiveByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return port.findActiveByCode(code.trim()).map(u -> new AdministrativeUnitRef(
                u.code(), u.name(), u.fullName(), u.level(), u.type(), u.parentCode()));
    }

    private static String normalizeLevel(String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        // Strict parse so an invalid ?level= returns 400 rather than silently
        // matching nothing.
        try {
            return AdministrativeLevel.valueOf(level.trim().toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw DomainException.validation("Invalid level: " + level + ". Allowed: PROVINCE, COMMUNE");
        }
    }
}
