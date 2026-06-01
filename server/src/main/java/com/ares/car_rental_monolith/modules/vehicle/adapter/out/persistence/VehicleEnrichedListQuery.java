package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.query.VehicleSortBy;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleListItem;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleFuelType;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleSource;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatus;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleTransmission;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.persistence.Tuples;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.util.List;
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

        return PageResponse.ofPageIndex(items, total, query.pageIndex(), size);
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
                Tuples.uuid(t, "id"),
                Tuples.uuid(t, "owner_customer_id"),
                t.get("owner_customer_name", String.class),
                t.get("host_code", String.class),
                Tuples.uuid(t, "fleet_vehicle_id"),
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
                Tuples.longValue(t.get("feature_count")),
                Tuples.longValue(t.get("active_availability_block_count")),
                Tuples.longValue(t.get("booking_count")),
                Tuples.dateTime(t, "created_at"),
                Tuples.dateTime(t, "updated_at")
        );
    }
}
