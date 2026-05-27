package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.repository;

import com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence.entity.VehicleImageJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleImageJpaRepository extends JpaRepository<VehicleImageJpaEntity, UUID> {
    long countByVehicleId(UUID vehicleId);

    @org.springframework.data.jpa.repository.Modifying
    @jakarta.transaction.Transactional
    @org.springframework.data.jpa.repository.Query(
            "DELETE FROM VehicleImageJpaEntity i WHERE i.vehicleId = :vehicleId")
    void deleteByVehicleId(UUID vehicleId);
}
