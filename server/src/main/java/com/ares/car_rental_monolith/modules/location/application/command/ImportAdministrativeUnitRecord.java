package com.ares.car_rental_monolith.modules.location.application.command;

import java.time.LocalDate;

// One parsed unit from the official source file. effectiveFrom/status fall back
// to sensible defaults in the import service when absent in the source.
public record ImportAdministrativeUnitRecord(
        String code,
        String name,
        String fullName,
        String level,
        String type,
        String parentCode,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String status
) {}
