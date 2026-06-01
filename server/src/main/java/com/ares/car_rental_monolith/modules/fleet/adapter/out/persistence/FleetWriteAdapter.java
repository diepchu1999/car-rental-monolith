package com.ares.car_rental_monolith.modules.fleet.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.fleet.application.port.out.WriteFleetPort;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class FleetWriteAdapter implements WriteFleetPort {

    private final EntityManager em;
    private final SqlLoader sql;

    FleetWriteAdapter(EntityManager em, SqlLoader sql) {
        this.em = em;
        this.sql = sql;
    }

    @Override
    public boolean assetCodeExists(String assetCode) {
        Number count = (Number) em.createNativeQuery(sql.load(FleetSqlPaths.ASSET_CODE_EXISTS))
                .setParameter("code", assetCode)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public boolean branchExists(UUID branchId) {
        Number count = (Number) em.createNativeQuery(sql.load(FleetSqlPaths.BRANCH_EXISTS))
                .setParameter("id", branchId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public void insertCompanyVehicle(UUID id, UUID vehicleId, String assetCode, UUID branchId) {
        OffsetDateTime now = OffsetDateTime.now();
        em.createNativeQuery(sql.load(FleetSqlPaths.INSERT_COMPANY_VEHICLE))
                .setParameter("id", id)
                .setParameter("vehicleId", vehicleId)
                .setParameter("branchId", branchId)
                .setParameter("assetCode", assetCode)
                .setParameter("now", now)
                .executeUpdate();
    }
}
