package com.ares.car_rental_monolith.modules.customer.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.customer.application.port.out.LoadCustomerStatsPort;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerStats;
import com.ares.car_rental_monolith.shared.persistence.Tuples;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Component;

@Component
class CustomerStatsAdapter implements LoadCustomerStatsPort {

    private final EntityManager em;
    private final SqlLoader sql;

    CustomerStatsAdapter(EntityManager em, SqlLoader sql) {
        this.em = em;
        this.sql = sql;
    }

    @Override
    public CustomerStats loadStats() {
        Tuple t = (Tuple) em.createNativeQuery(sql.load(CustomerSqlPaths.CUSTOMER_STATS), Tuple.class)
                .getSingleResult();
        return new CustomerStats(
                Tuples.longValue(t.get("total")),
                Tuples.longValue(t.get("renters")),
                Tuples.longValue(t.get("hosts")),
                Tuples.longValue(t.get("pending_or_blocked"))
        );
    }
}
