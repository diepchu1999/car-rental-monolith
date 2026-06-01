package com.ares.car_rental_monolith.modules.location.adapter.out.persistence;

/**
 * Classpath path tới các file SQL của RIÊNG module location (package-private).
 * Xem com.ares.car_rental_monolith.shared.sql.SqlLoader.
 */
final class LocationSqlPaths {

    private LocationSqlPaths() {}

    private static final String BASE = "sql/location/";

    static final String ADMIN_UNITS_BY_LEVEL = BASE + "admin_units_by_level.sql";
    static final String ADMIN_UNITS_BY_PROVINCE = BASE + "admin_units_by_province.sql";
    static final String ADMIN_UNITS_SEARCH = BASE + "admin_units_search.sql";
    static final String ADMIN_UNITS_BY_CODE = BASE + "admin_units_by_code.sql";
    static final String UPSERT_ADMIN_UNIT = BASE + "upsert_admin_unit.sql";
}
