package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.query.VehicleSortBy;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleListItem;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleFuelType;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleSource;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatus;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleTransmission;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
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

    private final EntityManager em;
    private final SqlLoader sql;

    VehicleEnrichedListQuery(EntityManager em, SqlLoader sql) {
        this.em = em;
        this.sql = sql;
    }

    @SuppressWarnings("unchecked")
    PageResponse<VehicleListItem> search(PageVehiclesQuery query) {
        int size = query.size();
        int offset = query.pageIndex() * size;

        long total = ((Number) bindFilters(
                em.createNativeQuery(sql.load(VehicleSqlPaths.VEHICLES_LIST_COUNT)), query)
                .getSingleResult()).longValue();

        // ORDER BY ráp động (cột sort do enum quyết định) nên nối ngoài file SQL.
        String dataSql = sql.load(VehicleSqlPaths.VEHICLES_LIST_DATA)
                + buildOrderBy(query) + " LIMIT :lim OFFSET :off";
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
