package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

// Cross-schema lookup against customer.customers. Lives in the vehicle
// persistence adapter because the customer module does not yet expose
// a public API; refactor to call that API once it exists.
@Component
class CustomerNameQuery {

    private final EntityManager entityManager;

    CustomerNameQuery(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    Optional<String> findFullName(UUID customerId) {
        if (customerId == null) {
            return Optional.empty();
        }
        try {
            Object result = entityManager
                    .createNativeQuery("SELECT full_name FROM customer.customers WHERE id = :id")
                    .setParameter("id", customerId)
                    .getSingleResult();
            return Optional.ofNullable((String) result);
        } catch (NoResultException ignored) {
            return Optional.empty();
        }
    }
}
