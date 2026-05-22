package com.ares.car_rental_monolith.modules.location.application.port.out;

import com.ares.car_rental_monolith.modules.location.application.command.ImportAdministrativeUnitRecord;
import java.util.List;

public interface WriteAdministrativeUnitPort {

    // Upsert by `code`: insert new units, update name/full_name/level/type/
    // parent_code/effective dates/status for existing ones. Returns the number of
    // rows written.
    int upsertAll(List<ImportAdministrativeUnitRecord> records);
}
