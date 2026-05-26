package com.ares.car_rental_monolith.modules.location.application.port.in;

import com.ares.car_rental_monolith.modules.location.application.command.ImportAdministrativeUnitRecord;
import java.util.List;

@FunctionalInterface
public interface ImportAdministrativeUnitsUseCase {
    // Validates and upserts the given units (by code). Returns rows written.
    int handle(List<ImportAdministrativeUnitRecord> records);
}
