package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.repository;

import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.entity.VehicleFeatureJpaEntity;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface VehicleFeatureJpaRepository extends JpaRepository<VehicleFeatureJpaEntity, UUID> {

    @Modifying
    @Transactional
    @Query("DELETE FROM VehicleFeatureJpaEntity f WHERE f.vehicleId = :vehicleId")
    void deleteByVehicleId(UUID vehicleId);
}
