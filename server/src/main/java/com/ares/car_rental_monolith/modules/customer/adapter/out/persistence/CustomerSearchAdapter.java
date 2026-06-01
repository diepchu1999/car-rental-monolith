package com.ares.car_rental_monolith.modules.customer.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.customer.application.port.out.SearchCustomersPort;
import com.ares.car_rental_monolith.modules.customer.application.query.SearchCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

// Search by free-text q against name/phone/email/hostCode. ILIKE allows the
// pg_trgm extension to back this with a GIN index (see V031). The hostCode
// match resolves to the host_profiles table. Mặc định trả cả renter lẫn host;
// khi hostOnly=true thì chỉ trả active host (dùng cho vehicle owner picker, nơi
// chủ xe HOST_OWNED bắt buộc là host). SQL ở sql/customer/*.sql (CustomerSqlPaths).
@Component
class CustomerSearchAdapter implements SearchCustomersPort {

    private final EntityManager em;
    private final SqlLoader sql;

    CustomerSearchAdapter(EntityManager em, SqlLoader sql) {
        this.em = em;
        this.sql = sql;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PageResponse<CustomerSummary> search(SearchCustomersQuery query) {
        int size = query.size();
        int offset = query.pageIndex() * size;

        long total = ((Number) em.createNativeQuery(sql.load(CustomerSqlPaths.SEARCH_CUSTOMERS_COUNT))
                .setParameter("q", query.q())
                .setParameter("hostOnly", query.hostOnly())
                .getSingleResult()).longValue();

        List<Tuple> rows = em.createNativeQuery(sql.load(CustomerSqlPaths.SEARCH_CUSTOMERS_DATA), Tuple.class)
                .setParameter("q", query.q())
                .setParameter("hostOnly", query.hostOnly())
                .setParameter("lim", size)
                .setParameter("off", offset)
                .getResultList();

        List<CustomerSummary> items = rows.stream().map(CustomerSearchAdapter::toSummary).toList();

        int page = query.pageIndex() + 1;
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);
        return PageResponse.of(items, total, page, size, totalPages, page < totalPages, page > 1);
    }

    private static CustomerSummary toSummary(Tuple t) {
        Object idVal = t.get("id");
        UUID id = idVal instanceof UUID u ? u : UUID.fromString(idVal.toString());
        return new CustomerSummary(
                id,
                t.get("full_name", String.class),
                t.get("phone", String.class),
                t.get("email", String.class),
                t.get("status", String.class),
                t.get("host_code", String.class)
        );
    }
}
