package com.ares.car_rental_monolith.modules.customer.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.customer.application.port.out.LoadCustomerStatsPort;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerStats;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Component;

@Component
class CustomerStatsAdapter implements LoadCustomerStatsPort {

    private static final String STATS_SQL = """
            SELECT
                (SELECT COUNT(*) FROM customer.customers) AS total,
                (SELECT COUNT(DISTINCT cr.customer_id) FROM customer.customer_roles cr
                 WHERE cr.role = 'RENTER') AS renters,
                (SELECT COUNT(DISTINCT cr.customer_id) FROM customer.customer_roles cr
                 WHERE cr.role = 'HOST') AS hosts,
                (SELECT COUNT(*) FROM customer.customers
                 WHERE status IN ('PENDING_KYC', 'BLOCKED')) AS pending_or_blocked
            """;

    private final EntityManager em;

    CustomerStatsAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    public CustomerStats loadStats() {
        Tuple t = (Tuple) em.createNativeQuery(STATS_SQL, Tuple.class).getSingleResult();
        return new CustomerStats(
                longValue(t.get("total")),
                longValue(t.get("renters")),
                longValue(t.get("hosts")),
                longValue(t.get("pending_or_blocked"))
        );
    }

    private static long longValue(Object v) {
        return v instanceof Number n ? n.longValue() : 0L;
    }
}
