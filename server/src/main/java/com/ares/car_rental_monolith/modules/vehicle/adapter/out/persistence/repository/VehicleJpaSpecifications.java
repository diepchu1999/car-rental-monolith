package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.repository;

import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.entity.VehicleJpaEntity;
import com.ares.car_rental_monolith.modules.vehicle.application.query.ListVehiclesQuery;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class VehicleJpaSpecifications {

    private VehicleJpaSpecifications() {}

    public static Specification<VehicleJpaEntity> from(ListVehiclesQuery query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>(2);
            if (query.source() != null) {
                predicates.add(cb.equal(root.get("source"), query.source()));
            }
            if (query.status() != null) {
                predicates.add(cb.equal(root.get("status"), query.status()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
