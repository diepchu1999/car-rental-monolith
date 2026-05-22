package com.ares.car_rental_monolith.modules.location.application.service;

import com.ares.car_rental_monolith.modules.location.application.command.ImportAdministrativeUnitRecord;
import com.ares.car_rental_monolith.modules.location.application.port.in.ImportAdministrativeUnitsUseCase;
import com.ares.car_rental_monolith.modules.location.application.port.out.WriteAdministrativeUnitPort;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class AdministrativeUnitImportService implements ImportAdministrativeUnitsUseCase {

    private static final LocalDate DEFAULT_EFFECTIVE_FROM = LocalDate.of(2025, 7, 1);
    private static final Set<String> VALID_LEVELS = Set.of("PROVINCE", "COMMUNE");
    private static final Set<String> VALID_TYPES =
            Set.of("PROVINCE", "CITY", "COMMUNE", "WARD", "SPECIAL_ZONE");

    private final WriteAdministrativeUnitPort port;

    AdministrativeUnitImportService(WriteAdministrativeUnitPort port) {
        this.port = port;
    }

    @Override
    public int handle(List<ImportAdministrativeUnitRecord> records) {
        if (records == null || records.isEmpty()) {
            throw DomainException.validation("No administrative units to import");
        }
        List<ImportAdministrativeUnitRecord> normalized = new ArrayList<>(records.size());
        for (int i = 0; i < records.size(); i++) {
            normalized.add(validateAndNormalize(records.get(i), i));
        }
        return port.upsertAll(normalized);
    }

    private ImportAdministrativeUnitRecord validateAndNormalize(ImportAdministrativeUnitRecord r, int index) {
        String code = require(r.code(), "code", index);
        String name = require(r.name(), "name", index);
        String level = require(r.level(), "level", index).toUpperCase();
        String type = require(r.type(), "type", index).toUpperCase();

        if (!VALID_LEVELS.contains(level)) {
            throw DomainException.validation(
                    "Invalid level '" + level + "' at index " + index + " (code=" + code + ")");
        }
        if (!VALID_TYPES.contains(type)) {
            throw DomainException.validation(
                    "Invalid type '" + type + "' at index " + index + " (code=" + code + ")");
        }

        String parentCode = blankToNull(r.parentCode());
        if ("COMMUNE".equals(level) && parentCode == null) {
            throw DomainException.validation(
                    "Commune at index " + index + " (code=" + code + ") requires a parentCode");
        }
        if ("PROVINCE".equals(level) && parentCode != null) {
            throw DomainException.validation(
                    "Province at index " + index + " (code=" + code + ") must not have a parentCode");
        }

        LocalDate effectiveFrom = r.effectiveFrom() == null ? DEFAULT_EFFECTIVE_FROM : r.effectiveFrom();
        String status = blankToNull(r.status()) == null ? "ACTIVE" : r.status().toUpperCase();
        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            throw DomainException.validation(
                    "Invalid status '" + status + "' at index " + index + " (code=" + code + ")");
        }

        return new ImportAdministrativeUnitRecord(
                code.trim(), name.trim(), blankToNull(r.fullName()),
                level, type, parentCode,
                effectiveFrom, r.effectiveTo(), status);
    }

    private static String require(String value, String field, int index) {
        if (value == null || value.isBlank()) {
            throw DomainException.validation("Missing '" + field + "' at index " + index);
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
