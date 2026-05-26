package com.ares.car_rental_monolith.modules.fleet.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.fleet.application.port.out.LoadFleetPort;
import com.ares.car_rental_monolith.modules.fleet.application.query.SearchFleetVehiclesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.BranchSummary;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetVehicleSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class FleetPersistenceAdapter implements LoadFleetPort {

    private static final String VEHICLE_WHERE = """
            WHERE (:q = ''
                OR cv.asset_code ILIKE CONCAT('%', :q, '%')
                OR COALESCE(v.license_plate, '') ILIKE CONCAT('%', :q, '%'))
            AND (CAST(:branchId AS UUID) IS NULL OR cv.branch_id = :branchId)
            """;

    private static final String VEHICLE_DATA_SQL = """
            SELECT cv.id, cv.vehicle_id, cv.asset_code, cv.asset_status,
                   br.id AS branch_id, br.name AS branch_name, br.city AS branch_city,
                   v.license_plate
            FROM fleet.company_vehicles cv
            LEFT JOIN fleet.branches br ON br.id = cv.branch_id
            LEFT JOIN vehicle.vehicles v ON v.id = cv.vehicle_id
            """ + VEHICLE_WHERE + """
            ORDER BY cv.asset_code ASC
            LIMIT :lim OFFSET :off
            """;

    private static final String VEHICLE_COUNT_SQL = """
            SELECT COUNT(*) FROM fleet.company_vehicles cv
            LEFT JOIN vehicle.vehicles v ON v.id = cv.vehicle_id
            """ + VEHICLE_WHERE;

    private static final String BRANCHES_SQL = """
            SELECT id, code, name, city, status
            FROM fleet.branches
            WHERE status = 'ACTIVE'
            ORDER BY name ASC
            """;

    private final EntityManager em;

    FleetPersistenceAdapter(EntityManager em) {
        this.em = em;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PageResponse<FleetVehicleSummary> searchFleetVehicles(SearchFleetVehiclesQuery query) {
        int size = query.size();
        int offset = query.pageIndex() * size;

        long total = ((Number) em.createNativeQuery(VEHICLE_COUNT_SQL)
                .setParameter("q", query.q())
                .setParameter("branchId", query.branchId())
                .getSingleResult()).longValue();

        List<Tuple> rows = em.createNativeQuery(VEHICLE_DATA_SQL, Tuple.class)
                .setParameter("q", query.q())
                .setParameter("branchId", query.branchId())
                .setParameter("lim", size)
                .setParameter("off", offset)
                .getResultList();

        List<FleetVehicleSummary> items = rows.stream().map(FleetPersistenceAdapter::toVehicle).toList();
        int page = query.pageIndex() + 1;
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);
        return PageResponse.of(items, total, page, size, totalPages, page < totalPages, page > 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BranchSummary> listActiveBranches() {
        List<Tuple> rows = em.createNativeQuery(BRANCHES_SQL, Tuple.class).getResultList();
        return rows.stream().map(FleetPersistenceAdapter::toBranch).toList();
    }

    private static FleetVehicleSummary toVehicle(Tuple t) {
        return new FleetVehicleSummary(
                uuid(t, "id"),
                uuid(t, "vehicle_id"),
                t.get("asset_code", String.class),
                t.get("asset_status", String.class),
                uuid(t, "branch_id"),
                t.get("branch_name", String.class),
                t.get("branch_city", String.class),
                t.get("license_plate", String.class)
        );
    }

    private static BranchSummary toBranch(Tuple t) {
        return new BranchSummary(
                uuid(t, "id"),
                t.get("code", String.class),
                t.get("name", String.class),
                t.get("city", String.class),
                t.get("status", String.class)
        );
    }

    private static UUID uuid(Tuple t, String col) {
        Object v = t.get(col);
        if (v == null) return null;
        return v instanceof UUID u ? u : UUID.fromString(v.toString());
    }
}
