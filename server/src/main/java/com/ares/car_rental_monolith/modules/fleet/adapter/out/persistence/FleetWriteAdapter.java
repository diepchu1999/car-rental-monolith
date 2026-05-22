package com.ares.car_rental_monolith.modules.fleet.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.fleet.application.port.out.WriteFleetPort;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class FleetWriteAdapter implements WriteFleetPort {

    private final EntityManager em;

    FleetWriteAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    public boolean assetCodeExists(String assetCode) {
        Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM fleet.company_vehicles WHERE asset_code = :code")
                .setParameter("code", assetCode)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public boolean branchExists(UUID branchId) {
        Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM fleet.branches WHERE id = :id")
                .setParameter("id", branchId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public void insertCompanyVehicle(UUID id, UUID vehicleId, String assetCode, UUID branchId) {
        OffsetDateTime now = OffsetDateTime.now();
        em.createNativeQuery("""
                INSERT INTO fleet.company_vehicles (
                    id, vehicle_id, branch_id, asset_code,
                    current_odometer_km, asset_status, created_at, updated_at
                ) VALUES (
                    :id, :vehicleId, :branchId, :assetCode,
                    0, 'AVAILABLE', :now, :now
                )
                """)
                .setParameter("id", id)
                .setParameter("vehicleId", vehicleId)
                .setParameter("branchId", branchId)
                .setParameter("assetCode", assetCode)
                .setParameter("now", now)
                .executeUpdate();
    }
}
