package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleFuelType;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleSource;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatus;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleTransmission;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

// Loads VehicleDetail in 6 bounded queries (one core + five batched lists).
// All queries are keyed by vehicleId only, so total queries are O(1) per detail
// page rather than O(N) per related row.
@Component
class VehicleDetailQuery {

    private final EntityManager em;
    private final SqlLoader sql;

    VehicleDetailQuery(EntityManager em, SqlLoader sql) {
        this.em = em;
        this.sql = sql;
    }

    Optional<VehicleDetail> load(UUID vehicleId) {
        Tuple core;
        try {
            core = (Tuple) em.createNativeQuery(sql.load(VehicleSqlPaths.VEHICLE_DETAIL_CORE), Tuple.class)
                    .setParameter("id", vehicleId)
                    .getSingleResult();
        } catch (NoResultException ignored) {
            return Optional.empty();
        }

        return Optional.of(new VehicleDetail(
                toVehicle(core),
                toOwner(core),
                toFleet(core),
                toListing(core),
                loadImages(vehicleId),
                loadFeatures(vehicleId),
                loadActivePricePlan(vehicleId).orElse(null),
                loadUpcomingBlocks(vehicleId),
                loadRecentBookings(vehicleId)
        ));
    }

    private static Vehicle toVehicle(Tuple t) {
        return new Vehicle(
                Tuples.uuid(t, "id"),
                Tuples.uuid(t, "owner_customer_id"),
                Tuples.uuid(t, "fleet_vehicle_id"),
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
                Tuples.dateTime(t, "created_at"),
                Tuples.dateTime(t, "updated_at")
        );
    }

    private static VehicleDetail.Owner toOwner(Tuple t) {
        UUID customerId = Tuples.uuid(t, "owner_customer_id");
        if (customerId == null) return null;
        return new VehicleDetail.Owner(
                customerId,
                t.get("owner_full_name", String.class),
                t.get("owner_phone", String.class),
                t.get("owner_email", String.class),
                t.get("host_code", String.class),
                t.get("host_display_name", String.class)
        );
    }

    private static VehicleDetail.Fleet toFleet(Tuple t) {
        UUID fleetVehicleId = Tuples.uuid(t, "fleet_vehicle_id");
        if (fleetVehicleId == null) return null;
        return new VehicleDetail.Fleet(
                fleetVehicleId,
                t.get("asset_code", String.class),
                t.get("asset_status", String.class),
                Tuples.uuid(t, "branch_id"),
                t.get("branch_name", String.class),
                t.get("branch_city", String.class)
        );
    }

    private static VehicleDetail.Listing toListing(Tuple t) {
        UUID listingId = Tuples.uuid(t, "listing_id");
        if (listingId == null) return null;
        return new VehicleDetail.Listing(
                listingId,
                t.get("listing_title", String.class),
                t.get("listing_description", String.class),
                t.get("listing_city", String.class),
                t.get("listing_district", String.class),
                t.get("listing_province_code", String.class),
                t.get("listing_commune_code", String.class),
                t.get("listing_province_name", String.class),
                t.get("listing_commune_name", String.class),
                t.get("listing_pickup_address", String.class),
                t.get("listing_base_daily_rate", BigDecimal.class),
                t.get("listing_currency", String.class),
                Boolean.TRUE.equals(t.get("listing_instant_booking", Boolean.class)),
                Boolean.TRUE.equals(t.get("listing_delivery_enabled", Boolean.class)),
                t.get("listing_status", String.class),
                Tuples.dateTime(t, "listing_published_at")
        );
    }

    @SuppressWarnings("unchecked")
    private List<VehicleDetail.Image> loadImages(UUID id) {
        List<Tuple> rows = em.createNativeQuery(sql.load(VehicleSqlPaths.VEHICLE_IMAGES), Tuple.class)
                .setParameter("id", id).getResultList();
        return rows.stream().map(t -> new VehicleDetail.Image(
                Tuples.uuid(t, "id"),
                t.get("file_url", String.class),
                t.get("sort_order", Integer.class),
                Boolean.TRUE.equals(t.get("is_cover", Boolean.class))
        )).toList();
    }

    @SuppressWarnings("unchecked")
    private List<VehicleDetail.Feature> loadFeatures(UUID id) {
        List<Tuple> rows = em.createNativeQuery(sql.load(VehicleSqlPaths.VEHICLE_FEATURES), Tuple.class)
                .setParameter("id", id).getResultList();
        return rows.stream().map(t -> new VehicleDetail.Feature(
                Tuples.uuid(t, "id"),
                t.get("code", String.class),
                t.get("name", String.class)
        )).toList();
    }

    private Optional<VehicleDetail.PricePlan> loadActivePricePlan(UUID id) {
        try {
            Tuple t = (Tuple) em.createNativeQuery(sql.load(VehicleSqlPaths.VEHICLE_ACTIVE_PRICE_PLAN), Tuple.class)
                    .setParameter("id", id).getSingleResult();
            return Optional.of(new VehicleDetail.PricePlan(
                    Tuples.uuid(t, "id"),
                    t.get("name", String.class),
                    t.get("base_daily_rate", BigDecimal.class),
                    t.get("hourly_rate", BigDecimal.class),
                    t.get("weekend_multiplier", BigDecimal.class),
                    t.get("deposit_amount", BigDecimal.class),
                    t.get("currency", String.class),
                    t.get("status", String.class),
                    Tuples.dateTime(t, "valid_from"),
                    Tuples.dateTime(t, "valid_to")
            ));
        } catch (NoResultException ignored) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private List<VehicleDetail.AvailabilityBlock> loadUpcomingBlocks(UUID id) {
        List<Tuple> rows = em.createNativeQuery(sql.load(VehicleSqlPaths.VEHICLE_UPCOMING_BLOCKS), Tuple.class)
                .setParameter("id", id).getResultList();
        return rows.stream().map(t -> new VehicleDetail.AvailabilityBlock(
                Tuples.uuid(t, "id"),
                Tuples.dateTime(t, "start_at"),
                Tuples.dateTime(t, "end_at"),
                t.get("reason", String.class),
                Tuples.uuid(t, "booking_id"),
                t.get("note", String.class)
        )).toList();
    }

    @SuppressWarnings("unchecked")
    private List<VehicleDetail.RecentBooking> loadRecentBookings(UUID id) {
        List<Tuple> rows = em.createNativeQuery(sql.load(VehicleSqlPaths.VEHICLE_RECENT_BOOKINGS), Tuple.class)
                .setParameter("id", id).getResultList();
        return rows.stream().map(t -> new VehicleDetail.RecentBooking(
                Tuples.uuid(t, "id"),
                t.get("booking_code", String.class),
                Tuples.uuid(t, "customer_id"),
                Tuples.dateTime(t, "start_at"),
                Tuples.dateTime(t, "end_at"),
                t.get("total_amount", BigDecimal.class),
                t.get("currency", String.class),
                t.get("status", String.class),
                Tuples.dateTime(t, "created_at")
        )).toList();
    }

    static final class Tuples {
        private Tuples() {}

        static UUID uuid(Tuple t, String col) {
            Object v = t.get(col);
            if (v == null) return null;
            return v instanceof UUID u ? u : UUID.fromString(v.toString());
        }

        static OffsetDateTime dateTime(Tuple t, String col) {
            Object v = t.get(col);
            if (v == null) return null;
            if (v instanceof OffsetDateTime odt) return odt;
            if (v instanceof Timestamp ts) return ts.toInstant().atOffset(ZoneOffset.UTC);
            return null;
        }
    }
}
