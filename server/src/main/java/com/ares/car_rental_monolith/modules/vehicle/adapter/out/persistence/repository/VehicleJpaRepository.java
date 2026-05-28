package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.repository;

import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.entity.VehicleJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VehicleJpaRepository
        extends JpaRepository<VehicleJpaEntity, UUID>,
                JpaSpecificationExecutor<VehicleJpaEntity> {
}
