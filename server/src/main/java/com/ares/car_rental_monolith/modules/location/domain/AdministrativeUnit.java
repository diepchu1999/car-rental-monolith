package com.ares.car_rental_monolith.modules.location.domain;

// A Vietnamese administrative unit in the 2-tier model (PROVINCE / COMMUNE).
// `code` is the official code from the source data and is the stable key other
// modules store; `name`/`fullName` are display labels; `parentCode` links a
// commune to its province (null for provinces).
public record AdministrativeUnit(
        String code,
        String name,
        String fullName,
        String level,
        String type,
        String parentCode
) {}
