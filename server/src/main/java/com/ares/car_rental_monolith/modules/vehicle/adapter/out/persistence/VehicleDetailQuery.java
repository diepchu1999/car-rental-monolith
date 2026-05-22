package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleFuelType;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleSource;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatus;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleTransmission;
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

    private static final String CORE_SQL = """
            SELECT
                v.id, v.owner_customer_id, v.fleet_vehicle_id, v.source,
                v.brand, v.model, v.version, v.manufacture_year, v.license_plate,
                v.seats, v.transmission, v.fuel_type, v.status,
                v.created_at, v.updated_at,
                c.full_name             AS owner_full_name,
                c.phone                 AS owner_phone,
                c.email                 AS owner_email,
                hp.host_code            AS host_code,
                hp.display_name         AS host_display_name,
                cv.asset_code           AS asset_code,
                cv.asset_status         AS asset_status,
                br.id                   AS branch_id,
                br.name                 AS branch_name,
                br.city                 AS branch_city,
                vl.id                   AS listing_id,
                vl.title                AS listing_title,
                vl.description          AS listing_description,
                vl.city                 AS listing_city,
                vl.district             AS listing_district,
                vl.province_code        AS listing_province_code,
                vl.commune_code         AS listing_commune_code,
                pu.name                 AS listing_province_name,
                cu.name                 AS listing_commune_name,
                vl.pickup_address       AS listing_pickup_address,
                vl.base_daily_rate      AS listing_base_daily_rate,
                vl.currency             AS listing_currency,
                vl.instant_booking_enabled AS listing_instant_booking,
                vl.delivery_enabled     AS listing_delivery_enabled,
                vl.status               AS listing_status,
                vl.published_at         AS listing_published_at
            FROM vehicle.vehicles v
            LEFT JOIN customer.customers c ON c.id = v.owner_customer_id
            LEFT JOIN customer.host_profiles hp ON hp.customer_id = v.owner_customer_id
            LEFT JOIN fleet.company_vehicles cv ON cv.vehicle_id = v.id
            LEFT JOIN fleet.branches br ON br.id = cv.branch_id
            LEFT JOIN vehicle.vehicle_listings vl ON vl.vehicle_id = v.id
            LEFT JOIN location.administrative_units pu ON pu.code = vl.province_code
            LEFT JOIN location.administrative_units cu ON cu.code = vl.commune_code
            WHERE v.id = :id
            """;

    private static final String IMAGES_SQL = """
            SELECT id, file_url, sort_order, is_cover
            FROM vehicle.vehicle_images
            WHERE vehicle_id = :id
            ORDER BY is_cover DESC, sort_order ASC
            """;

    private static final String FEATURES_SQL = """
            SELECT id, code, name
            FROM vehicle.vehicle_features
            WHERE vehicle_id = :id
            ORDER BY name ASC
            """;

    private static final String PRICE_PLAN_SQL = """
            SELECT id, name, base_daily_rate, hourly_rate, weekend_multiplier,
                   deposit_amount, currency, status, valid_from, valid_to
            FROM pricing.price_plans
            WHERE target_type = 'VEHICLE'
              AND target_id = :id
              AND status = 'ACTIVE'
            ORDER BY valid_from DESC NULLS LAST
            LIMIT 1
            """;

    private static final String BLOCKS_SQL = """
            SELECT id, start_at, end_at, reason, booking_id, note
            FROM vehicle.availability_blocks
            WHERE vehicle_id = :id
              AND end_at > NOW()
            ORDER BY start_at ASC
            LIMIT 20
            """;

    private static final String RECENT_BOOKINGS_SQL = """
            SELECT id, booking_code, customer_id, start_at, end_at,
                   total_amount, currency, status, created_at
            FROM booking.bookings
            WHERE vehicle_id = :id
            ORDER BY created_at DESC
            LIMIT 10
            """;

    private final EntityManager em;

    VehicleDetailQuery(EntityManager em) {
        this.em = em;
    }

    Optional<VehicleDetail> load(UUID vehicleId) {
        Tuple core;
        try {
            core = (Tuple) em.createNativeQuery(CORE_SQL, Tuple.class)
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
        List<Tuple> rows = em.createNativeQuery(IMAGES_SQL, Tuple.class)
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
        List<Tuple> rows = em.createNativeQuery(FEATURES_SQL, Tuple.class)
                .setParameter("id", id).getResultList();
        return rows.stream().map(t -> new VehicleDetail.Feature(
                Tuples.uuid(t, "id"),
                t.get("code", String.class),
                t.get("name", String.class)
        )).toList();
    }

    private Optional<VehicleDetail.PricePlan> loadActivePricePlan(UUID id) {
        try {
            Tuple t = (Tuple) em.createNativeQuery(PRICE_PLAN_SQL, Tuple.class)
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
        List<Tuple> rows = em.createNativeQuery(BLOCKS_SQL, Tuple.class)
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
        List<Tuple> rows = em.createNativeQuery(RECENT_BOOKINGS_SQL, Tuple.class)
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
