package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface VehicleJpaRepository
        extends JpaRepository<VehicleJpaEntity, UUID>,
                JpaSpecificationExecutor<VehicleJpaEntity> {
}
