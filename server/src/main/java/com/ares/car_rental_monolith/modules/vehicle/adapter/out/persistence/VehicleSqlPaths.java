package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

/**
 * Classpath path tới các file SQL của RIÊNG module vehicle (package-private).
 * Xem com.ares.car_rental_monolith.shared.sql.SqlLoader.
 */
final class VehicleSqlPaths {

    private VehicleSqlPaths() {}

    private static final String BASE = "sql/vehicle/";

    // Detail (VehicleDetailQuery)
    static final String VEHICLE_DETAIL_CORE = BASE + "vehicle_detail_core.sql";
    static final String VEHICLE_IMAGES = BASE + "vehicle_images.sql";
    static final String VEHICLE_FEATURES = BASE + "vehicle_features.sql";
    static final String VEHICLE_ACTIVE_PRICE_PLAN = BASE + "vehicle_active_price_plan.sql";
    static final String VEHICLE_UPCOMING_BLOCKS = BASE + "vehicle_upcoming_blocks.sql";
    static final String VEHICLE_RECENT_BOOKINGS = BASE + "vehicle_recent_bookings.sql";

    // Enriched list (VehicleEnrichedListQuery) — ORDER BY ráp động ngoài file
    static final String VEHICLES_LIST_DATA = BASE + "vehicles_list_data.sql";
    static final String VEHICLES_LIST_COUNT = BASE + "vehicles_list_count.sql";

    // Write (VehicleWriteAdapter)
    static final String LICENSE_PLATE_EXISTS = BASE + "license_plate_exists.sql";
    static final String DEACTIVATE_ACTIVE_PRICE_PLANS = BASE + "deactivate_active_price_plans.sql";
    static final String INSERT_PRICE_PLAN = BASE + "insert_price_plan.sql";
}
