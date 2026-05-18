package com.ares.car_rental_monolith.modules.vehicle.infrastructure.persistence.jpa;

import com.ares.car_rental_monolith.modules.vehicle.application.dto.VehicleResponse;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.VehicleQueryPort;
import com.ares.car_rental_monolith.modules.vehicle.application.query.VehicleSearchCriteria;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaVehicleQueryAdapter implements VehicleQueryPort {

    private final VehicleJpaRepository vehicleJpaRepository;

    public JpaVehicleQueryAdapter(VehicleJpaRepository vehicleJpaRepository) {
        this.vehicleJpaRepository = vehicleJpaRepository;
    }

    @Override
    public List<VehicleResponse> findVehicles(VehicleSearchCriteria criteria) {
        return findByCriteria(criteria)
                .stream()
                .map(VehicleJpaMapper::toResponse)
                .toList();
    }

    private List<VehicleJpaEntity> findByCriteria(VehicleSearchCriteria criteria) {
        if (criteria.source() != null && criteria.status() != null) {
            return vehicleJpaRepository.findBySourceAndStatusOrderByCreatedAtDesc(
                    criteria.source(),
                    criteria.status()
            );
        }

        if (criteria.source() != null) {
            return vehicleJpaRepository.findBySourceOrderByCreatedAtDesc(criteria.source());
        }

        if (criteria.status() != null) {
            return vehicleJpaRepository.findByStatusOrderByCreatedAtDesc(criteria.status());
        }

        return vehicleJpaRepository.findAllByOrderByCreatedAtDesc();
    }
}
