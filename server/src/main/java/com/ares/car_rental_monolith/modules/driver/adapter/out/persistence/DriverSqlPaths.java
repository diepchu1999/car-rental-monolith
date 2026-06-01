package com.ares.car_rental_monolith.modules.driver.adapter.out.persistence;

/**
 * Classpath path tới các file SQL của RIÊNG module driver (package-private).
 * Xem com.ares.car_rental_monolith.shared.sql.SqlLoader.
 */
final class DriverSqlPaths {

    private DriverSqlPaths() {}

    private static final String BASE = "sql/driver/";

    static final String DRIVERS_DATA = BASE + "drivers_data.sql";
    static final String DRIVERS_COUNT = BASE + "drivers_count.sql";
    static final String DRIVER_DETAIL = BASE + "driver_detail.sql";
}
