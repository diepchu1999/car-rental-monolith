package com.ares.car_rental_monolith.modules.customer.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.customer.application.port.out.LoadCustomerPort;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CustomerDirectoryAdapter implements LoadCustomerPort {

    private final EntityManager em;

    CustomerDirectoryAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    public boolean isActiveCustomer(UUID customerId) {
        Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM customer.customers WHERE id = :id AND status = 'ACTIVE'")
                .setParameter("id", customerId)
                .getSingleResult();
        return count.longValue() > 0;
    }
}
