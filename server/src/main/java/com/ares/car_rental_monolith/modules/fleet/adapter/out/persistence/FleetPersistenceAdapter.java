package com.ares.car_rental_monolith.modules.fleet.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.fleet.application.port.out.LoadFleetPort;
import com.ares.car_rental_monolith.modules.fleet.application.query.SearchFleetVehiclesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.BranchSummary;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchStatus;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetVehicleSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.persistence.Tuples;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
class FleetPersistenceAdapter implements LoadFleetPort {

    private final EntityManager em;
    private final SqlLoader sql;

    FleetPersistenceAdapter(EntityManager em, SqlLoader sql) {
        this.em = em;
        this.sql = sql;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PageResponse<FleetVehicleSummary> searchFleetVehicles(SearchFleetVehiclesQuery query) {
        int size = query.size();
        int offset = query.pageIndex() * size;

        long total = ((Number) em.createNativeQuery(sql.load(FleetSqlPaths.SEARCH_FLEET_VEHICLES_COUNT))
                .setParameter("q", query.q())
                .setParameter("branchId", query.branchId())
                .getSingleResult()).longValue();

        List<Tuple> rows = em.createNativeQuery(sql.load(FleetSqlPaths.SEARCH_FLEET_VEHICLES_DATA), Tuple.class)
                .setParameter("q", query.q())
                .setParameter("branchId", query.branchId())
                .setParameter("lim", size)
                .setParameter("off", offset)
                .getResultList();

        List<FleetVehicleSummary> items = rows.stream().map(FleetPersistenceAdapter::toVehicle).toList();
        return PageResponse.ofPageIndex(items, total, query.pageIndex(), size);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BranchSummary> listActiveBranches() {
        List<Tuple> rows = em.createNativeQuery(sql.load(FleetSqlPaths.LIST_ACTIVE_BRANCHES), Tuple.class)
                .getResultList();
        return rows.stream().map(FleetPersistenceAdapter::toBranch).toList();
    }

    @Override
    public Optional<FleetBranchDetail> getBranch(UUID branchId) {
        Tuple c;
        try {
            c = (Tuple) em.createNativeQuery(sql.load(FleetSqlPaths.LOAD_FLEET_BRANCH), Tuple.class)
                    .setParameter("id", branchId)
                    .getSingleResult();
        } catch (NoResultException ignored) {
            return Optional.empty();
        }

        return Optional.of(new FleetBranchDetail(
                Tuples.uuid(c, "id"),
                c.get("code", String.class),
                c.get("name", String.class),
                c.get("address", String.class),
                c.get("city", String.class),
                c.get("phone", String.class),
                FleetBranchStatus.valueOf(c.get("status", String.class)),
                c.get("province_code", String.class),
                c.get("commune_code", String.class),
                Tuples.dateTime(c, "created_at"),
                Tuples.dateTime(c, "updated_at")
        ));
    }

    private static FleetVehicleSummary toVehicle(Tuple t) {
        return new FleetVehicleSummary(
                Tuples.uuid(t, "id"),
                Tuples.uuid(t, "vehicle_id"),
                t.get("asset_code", String.class),
                t.get("asset_status", String.class),
                Tuples.uuid(t, "branch_id"),
                t.get("branch_name", String.class),
                t.get("branch_city", String.class),
                t.get("license_plate", String.class)
        );
    }

    private static BranchSummary toBranch(Tuple t) {
        return new BranchSummary(
                Tuples.uuid(t, "id"),
                t.get("code", String.class),
                t.get("name", String.class),
                t.get("city", String.class),
                t.get("status", String.class)
        );
    }
}
