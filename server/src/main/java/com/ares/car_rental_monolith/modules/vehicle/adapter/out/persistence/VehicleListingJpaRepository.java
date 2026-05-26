package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface VehicleListingJpaRepository extends JpaRepository<VehicleListingJpaEntity, UUID> {
    Optional<VehicleListingJpaEntity> findByVehicleId(UUID vehicleId);

    long countByVehicleId(UUID vehicleId);
}
