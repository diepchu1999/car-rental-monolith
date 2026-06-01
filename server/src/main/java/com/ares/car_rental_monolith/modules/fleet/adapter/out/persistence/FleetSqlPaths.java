package com.ares.car_rental_monolith.modules.fleet.adapter.out.persistence;

/**
 * Classpath path tới các file SQL của RIÊNG module fleet (package-private).
 * Xem com.ares.car_rental_monolith.shared.sql.SqlLoader.
 */
final class FleetSqlPaths {

    private FleetSqlPaths() {}

    private static final String BASE = "sql/fleet/";

    static final String FLEET_BRANCHES_COUNT = BASE + "fleet_branches_count.sql";
    static final String FLEET_BRANCHES_DATA = BASE + "fleet_branches_data.sql";
    static final String SEARCH_FLEET_VEHICLES_DATA = BASE + "search_fleet_vehicles_data.sql";
    static final String SEARCH_FLEET_VEHICLES_COUNT = BASE + "search_fleet_vehicles_count.sql";
    static final String LIST_ACTIVE_BRANCHES = BASE + "list_active_branches.sql";
    static final String ASSET_CODE_EXISTS = BASE + "asset_code_exists.sql";
    static final String BRANCH_EXISTS = BASE + "branch_exists.sql";
    static final String INSERT_COMPANY_VEHICLE = BASE + "insert_company_vehicle.sql";
}
