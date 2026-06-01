package com.ares.car_rental_monolith.modules.fleet.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.fleet.application.port.out.LoadFleetBranchPort;
import com.ares.car_rental_monolith.modules.fleet.application.query.ListFleetBranchesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchStatus;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.UUID;
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

        int page = query.pageIndex() + 1;
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);
        return PageResponse.of(items, total, page, size, totalPages, page < totalPages, page > 1);
    }

    private static FleetBranchDetail toBranch(Tuple t) {
        return new FleetBranchDetail(
                uuid(t, "id"),
                t.get("code", String.class),
                t.get("name", String.class),
                t.get("address", String.class),
                t.get("city", String.class),
                t.get("phone", String.class),
                FleetBranchStatus.valueOf(t.get("status", String.class)),
                // fleet.branches chưa lưu mã tỉnh/phường (xem V040__fleet.sql) — để null
                // tới khi bổ sung cột province_code/commune_code nếu cần.
                null,
                null
        );
    }

    private static UUID uuid(Tuple t, String col) {
        Object v = t.get(col);
        if (v == null) return null;
        return v instanceof UUID u ? u : UUID.fromString(v.toString());
    }
}
