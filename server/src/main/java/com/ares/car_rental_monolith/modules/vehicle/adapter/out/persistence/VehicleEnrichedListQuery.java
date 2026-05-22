package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.query.VehicleSortBy;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleListItem;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleFuelType;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleSource;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatus;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleTransmission;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class VehicleEnrichedListQuery {

    // Shared WHERE clause + filter params. CAST(... AS TEXT) IS NULL is required
    // because PostgreSQL cannot infer the type of an unbound NULL parameter.
    // Each filter is "param IS NULL OR column = param" so they're optional.
    private static final String WHERE_CLAUSE = """
            WHERE (:q = '' OR v.brand ILIKE CONCAT('%', :q, '%')
                OR v.model ILIKE CONCAT('%', :q, '%')
                OR v.version ILIKE CONCAT('%', :q, '%')
                OR v.license_plate ILIKE CONCAT('%', :q, '%'))
            AND (CAST(:source AS TEXT) IS NULL OR v.source = :source)
            AND (CAST(:status AS TEXT) IS NULL OR v.status = :status)
            AND (CAST(:listingStatus AS TEXT) IS NULL OR vl.status = :listingStatus)
            AND (CAST(:provinceCode AS TEXT) IS NULL OR vl.province_code = :provinceCode)
            AND (CAST(:communeCode AS TEXT) IS NULL OR vl.commune_code = :communeCode)
            AND (CAST(:fuelType AS TEXT) IS NULL OR v.fuel_type = :fuelType)
            AND (CAST(:transmission AS TEXT) IS NULL OR v.transmission = :transmission)
            AND (CAST(:seats AS INTEGER) IS NULL OR v.seats = :seats)
            AND (CAST(:minRate AS NUMERIC) IS NULL OR vl.base_daily_rate >= :minRate)
            AND (CAST(:maxRate AS NUMERIC) IS NULL OR vl.base_daily_rate <= :maxRate)
            AND (CAST(:hasBookings AS BOOLEAN) IS NULL
                OR (:hasBookings = TRUE  AND EXISTS (SELECT 1 FROM booking.bookings bk
                                                    WHERE bk.vehicle_id = v.id))
                OR (:hasBookings = FALSE AND NOT EXISTS (SELECT 1 FROM booking.bookings bk
                                                        WHERE bk.vehicle_id = v.id)))
            """;

    // baseDailyRate comes from vehicle.vehicle_listings.base_daily_rate (the
    // listing's own price). pricing.price_plans is loaded only for detail view.
    private static final String DATA_SELECT = """
            SELECT
                v.id,
                v.owner_customer_id,
                c.full_name             AS owner_customer_name,
                hp.host_code,
                v.fleet_vehicle_id,
                cv.asset_code,
                br.name                 AS branch_name,
                v.source,
                v.brand,
                v.model,
                v.version,
                v.manufacture_year,
                v.license_plate,
                v.seats,
                v.transmission,
                v.fuel_type,
                v.status,
                vl.status               AS listing_status,
                vl.city,
                vl.district,
                vl.base_daily_rate,
                (SELECT vi.file_url
                 FROM vehicle.vehicle_images vi
                 WHERE vi.vehicle_id = v.id
                 ORDER BY vi.is_cover DESC, vi.sort_order ASC
                 LIMIT 1)               AS cover_image_url,
                (SELECT COUNT(*)
                 FROM vehicle.vehicle_features vf
                 WHERE vf.vehicle_id = v.id) AS feature_count,
                (SELECT COUNT(*)
                 FROM vehicle.availability_blocks ab
                 WHERE ab.vehicle_id = v.id
                   AND ab.end_at > NOW()) AS active_availability_block_count,
                (SELECT COUNT(*)
                 FROM booking.bookings bk
                 WHERE bk.vehicle_id = v.id) AS booking_count,
                v.created_at,
                v.updated_at
            FROM vehicle.vehicles v
            LEFT JOIN customer.customers c ON c.id = v.owner_customer_id
            LEFT JOIN customer.host_profiles hp ON hp.customer_id = v.owner_customer_id
            LEFT JOIN fleet.company_vehicles cv ON cv.vehicle_id = v.id
            LEFT JOIN fleet.branches br ON br.id = cv.branch_id
            LEFT JOIN vehicle.vehicle_listings vl ON vl.vehicle_id = v.id
            """ + WHERE_CLAUSE;

    private static final String COUNT_SQL =
            "SELECT COUNT(*) FROM vehicle.vehicles v\n"
            + "LEFT JOIN vehicle.vehicle_listings vl ON vl.vehicle_id = v.id\n"
            + WHERE_CLAUSE;

    private final EntityManager em;

    VehicleEnrichedListQuery(EntityManager em) {
        this.em = em;
    }

    @SuppressWarnings("unchecked")
    PageResponse<VehicleListItem> search(PageVehiclesQuery query) {
        int size = query.size();
        int offset = query.pageIndex() * size;

        long total = ((Number) bindFilters(em.createNativeQuery(COUNT_SQL), query)
                .getSingleResult()).longValue();

        String dataSql = DATA_SELECT + buildOrderBy(query) + " LIMIT :lim OFFSET :off";
        List<Tuple> rows = bindFilters(em.createNativeQuery(dataSql, Tuple.class), query)
                .setParameter("lim", size)
                .setParameter("off", offset)
                .getResultList();

        List<VehicleListItem> items = rows.stream().map(VehicleEnrichedListQuery::toItem).toList();

        int page = query.pageIndex() + 1;
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);
        return PageResponse.of(items, total, page, size, totalPages, page < totalPages, page > 1);
    }

    private static Query bindFilters(Query q, PageVehiclesQuery query) {
        return q.setParameter("q", query.q())
                .setParameter("source", query.sourceCode())
                .setParameter("status", query.statusCode())
                .setParameter("listingStatus", query.listingStatusCode())
                .setParameter("provinceCode", query.provinceCode())
                .setParameter("communeCode", query.communeCode())
                .setParameter("fuelType", query.fuelTypeCode())
                .setParameter("transmission", query.transmissionCode())
                .setParameter("seats", query.seats())
                .setParameter("minRate", query.minRate())
                .setParameter("maxRate", query.maxRate())
                .setParameter("hasBookings", query.hasBookings());
    }

    private static String buildOrderBy(PageVehiclesQuery query) {
        VehicleSortBy sortBy = query.resolvedSortBy();
        String direction = query.resolvedSortDir().name();
        // Stable secondary sort by id keeps page boundaries deterministic when
        // primary sort has ties.
        return "\nORDER BY " + sortBy.column() + " " + direction + " NULLS LAST, v.id ASC\n";
    }

    private static VehicleListItem toItem(Tuple t) {
        return new VehicleListItem(
                uuid(t, "id"),
                uuid(t, "owner_customer_id"),
                t.get("owner_customer_name", String.class),
                t.get("host_code", String.class),
                uuid(t, "fleet_vehicle_id"),
                t.get("asset_code", String.class),
                t.get("branch_name", String.class),
                VehicleSource.valueOf(t.get("source", String.class)),
                t.get("brand", String.class),
                t.get("model", String.class),
                t.get("version", String.class),
                t.get("manufacture_year", Integer.class),
                t.get("license_plate", String.class),
                t.get("seats", Integer.class),
                VehicleTransmission.valueOf(t.get("transmission", String.class)),
                VehicleFuelType.valueOf(t.get("fuel_type", String.class)),
                VehicleStatus.valueOf(t.get("status", String.class)),
                t.get("listing_status", String.class),
                t.get("city", String.class),
                t.get("district", String.class),
                t.get("base_daily_rate", BigDecimal.class),
                t.get("cover_image_url", String.class),
                toLong(t, "feature_count"),
                toLong(t, "active_availability_block_count"),
                toLong(t, "booking_count"),
                toOffsetDateTime(t, "created_at"),
                toOffsetDateTime(t, "updated_at")
        );
    }

    private static UUID uuid(Tuple t, String col) {
        Object v = t.get(col);
        if (v == null) return null;
        return v instanceof UUID u ? u : UUID.fromString(v.toString());
    }

    private static long toLong(Tuple t, String col) {
        Number n = (Number) t.get(col);
        return n == null ? 0L : n.longValue();
    }

    private static OffsetDateTime toOffsetDateTime(Tuple t, String col) {
        Object v = t.get(col);
        if (v == null) return null;
        if (v instanceof OffsetDateTime odt) return odt;
        if (v instanceof Timestamp ts) return ts.toInstant().atOffset(ZoneOffset.UTC);
        return null;
    }
}
