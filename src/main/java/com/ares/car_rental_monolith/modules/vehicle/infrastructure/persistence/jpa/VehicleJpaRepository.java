package com.ares.car_rental_monolith.modules.vehicle.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, UUID> {

    List<VehicleJpaEntity> findAllByOrderByCreatedAtDesc();

    List<VehicleJpaEntity> findBySourceOrderByCreatedAtDesc(String source);

    List<VehicleJpaEntity> findByStatusOrderByCreatedAtDesc(String status);

    List<VehicleJpaEntity> findBySourceAndStatusOrderByCreatedAtDesc(String source, String status);
}
