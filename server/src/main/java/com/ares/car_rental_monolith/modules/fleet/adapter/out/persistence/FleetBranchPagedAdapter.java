package com.ares.car_rental_monolith.modules.fleet.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.fleet.application.port.out.LoadFleetBranchPort;
import com.ares.car_rental_monolith.modules.fleet.application.query.ListFleetBranchesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchStatus;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class FleetBranchPagedAdapter implements LoadFleetBranchPort {

    // Free-text khớp mã/tên/địa chỉ/thành phố/SĐT (ILIKE; phone có thể NULL nên
    // COALESCE). Status nullable: CAST(... AS TEXT) IS NULL cho phép bỏ filter khi
    // 'all' — Postgres không suy được kiểu của tham số NULL chưa bind nếu không cast.
    private static final String BRANCH_WHERE = """
            WHERE (:q = ''
                OR br.code ILIKE CONCAT('%', :q, '%')
                OR br.name ILIKE CONCAT('%', :q, '%')
                OR br.address ILIKE CONCAT('%', :q, '%')
                OR br.city ILIKE CONCAT('%', :q, '%')
                OR COALESCE(br.phone, '') ILIKE CONCAT('%', :q, '%'))
            AND (CAST(:status AS TEXT) IS NULL OR br.status = :status)
            """;

    private static final String BRANCH_COUNT_SQL = """
            SELECT COUNT(*) FROM fleet.branches br
            """ + BRANCH_WHERE;

    private static final String BRANCH_DATA_SQL = """
            SELECT br.id, br.code, br.name, br.address, br.city, br.phone, br.status
            FROM fleet.branches br
            """ + BRANCH_WHERE + """
            ORDER BY br.created_at DESC, br.id ASC
            LIMIT :lim OFFSET :off
            """;

    private final EntityManager em;

    FleetBranchPagedAdapter(EntityManager em) {
        this.em = em;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PageResponse<FleetBranchDetail> loadFleetBranchesList(ListFleetBranchesQuery query) {
        int size = query.size();
        int offset = query.pageIndex() * size;

        long total = ((Number) em.createNativeQuery(BRANCH_COUNT_SQL)
                .setParameter("q", query.q())
                .setParameter("status", query.status())
                .getSingleResult()).longValue();

        List<Tuple> rows = em.createNativeQuery(BRANCH_DATA_SQL, Tuple.class)
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
