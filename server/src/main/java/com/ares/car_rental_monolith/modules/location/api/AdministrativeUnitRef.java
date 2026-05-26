package com.ares.car_rental_monolith.modules.location.api;

// Public, cross-module reference to an administrative unit. Other modules use
// this to validate/resolve the codes they store, without depending on the
// location module's internals.
public record AdministrativeUnitRef(
        String code,
        String name,
        String fullName,
        String level,
        String type,
        String parentCode
) {}
