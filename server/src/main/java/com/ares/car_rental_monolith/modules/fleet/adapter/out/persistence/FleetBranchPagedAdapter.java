package com.ares.car_rental_monolith.modules.fleet.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.fleet.application.port.out.LoadFleetBranchPort;
import com.ares.car_rental_monolith.modules.fleet.application.query.ListFleetBranchesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchStatus;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.persistence.Tuples;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class FleetBranchPagedAdapter implements LoadFleetBranchPort {

    private final EntityManager em;
    private final SqlLoader sql;

    FleetBranchPagedAdapter(EntityManager em, SqlLoader sql) {
        this.em = em;
        this.sql = sql;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PageResponse<FleetBranchDetail> loadFleetBranchesList(ListFleetBranchesQuery query) {
        int size = query.size();
        int offset = query.pageIndex() * size;

        long total = ((Number) em.createNativeQuery(sql.load(FleetSqlPaths.FLEET_BRANCHES_COUNT))
                .setParameter("q", query.q())
                .setParameter("status", query.status())
                .getSingleResult()).longValue();

        List<Tuple> rows = em.createNativeQuery(sql.load(FleetSqlPaths.FLEET_BRANCHES_DATA), Tuple.class)
                .setParameter("q", query.q())
                .setParameter("status", query.status())
                .setParameter("lim", size)
                .setParameter("off", offset)
                .getResultList();

        List<FleetBranchDetail> items = rows.stream()
                .map(FleetBranchPagedAdapter::toBranch)
                .toList();

        return PageResponse.ofPageIndex(items, total, query.pageIndex(), size);
    }

    private static FleetBranchDetail toBranch(Tuple t) {
        return new FleetBranchDetail(
                Tuples.uuid(t, "id"),
                t.get("code", String.class),
                t.get("name", String.class),
                t.get("address", String.class),
                t.get("city", String.class),
                t.get("phone", String.class),
                FleetBranchStatus.valueOf(t.get("status", String.class)),
                t.get("province_code", String.class),
                t.get("commune_code", String.class),
                Tuples.dateTime(t, "created_at"),
                Tuples.dateTime(t, "updated_at")
        );
    }
}
