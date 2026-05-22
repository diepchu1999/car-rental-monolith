package com.ares.car_rental_monolith.modules.location.api;

import java.util.Optional;

// Public port for resolving/validating administrative unit codes from other
// modules (e.g. vehicle listings, customer addresses, fleet branches).
// Returns only ACTIVE units.
public interface AdministrativeUnitDirectory {

    Optional<AdministrativeUnitRef> findActiveByCode(String code);
}
