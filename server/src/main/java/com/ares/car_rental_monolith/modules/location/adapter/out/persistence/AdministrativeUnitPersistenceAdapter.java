package com.ares.car_rental_monolith.modules.location.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.location.application.command.ImportAdministrativeUnitRecord;
import com.ares.car_rental_monolith.modules.location.application.port.out.LoadAdministrativeUnitPort;
import com.ares.car_rental_monolith.modules.location.application.port.out.WriteAdministrativeUnitPort;
import com.ares.car_rental_monolith.modules.location.domain.AdministrativeUnit;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class AdministrativeUnitPersistenceAdapter
        implements LoadAdministrativeUnitPort, WriteAdministrativeUnitPort {

    private final EntityManager em;
    private final SqlLoader sql;

    AdministrativeUnitPersistenceAdapter(EntityManager em, SqlLoader sql) {
        this.em = em;
        this.sql = sql;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AdministrativeUnit> listByLevel(String level) {
        List<Tuple> rows = em.createNativeQuery(sql.load(LocationSqlPaths.ADMIN_UNITS_BY_LEVEL), Tuple.class)
                .setParameter("level", level)
                .getResultList();
        return rows.stream().map(AdministrativeUnitPersistenceAdapter::toDomain).toList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AdministrativeUnit> listCommunesByProvince(String provinceCode) {
        List<Tuple> rows = em.createNativeQuery(sql.load(LocationSqlPaths.ADMIN_UNITS_BY_PROVINCE), Tuple.class)
                .setParameter("provinceCode", provinceCode)
                .getResultList();
        return rows.stream().map(AdministrativeUnitPersistenceAdapter::toDomain).toList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AdministrativeUnit> search(String query, String level, String provinceCode, int limit) {
        List<Tuple> rows = em.createNativeQuery(sql.load(LocationSqlPaths.ADMIN_UNITS_SEARCH), Tuple.class)
                .setParameter("q", query)
                .setParameter("level", level)
                .setParameter("provinceCode", provinceCode)
                .setParameter("lim", limit)
                .getResultList();
        return rows.stream().map(AdministrativeUnitPersistenceAdapter::toDomain).toList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<AdministrativeUnit> findActiveByCode(String code) {
        List<Tuple> rows = em.createNativeQuery(sql.load(LocationSqlPaths.ADMIN_UNITS_BY_CODE), Tuple.class)
                .setParameter("code", code)
                .getResultList();
        return rows.stream().findFirst().map(AdministrativeUnitPersistenceAdapter::toDomain);
    }

    @Override
    public int upsertAll(List<ImportAdministrativeUnitRecord> records) {
        int written = 0;
        for (ImportAdministrativeUnitRecord r : records) {
            em.createNativeQuery(sql.load(LocationSqlPaths.UPSERT_ADMIN_UNIT))
                    .setParameter("code", r.code())
                    .setParameter("name", r.name())
                    .setParameter("fullName", r.fullName())
                    .setParameter("level", r.level())
                    .setParameter("type", r.type())
                    .setParameter("parentCode", r.parentCode())
                    .setParameter("effectiveFrom", r.effectiveFrom())
                    .setParameter("effectiveTo", r.effectiveTo())
                    .setParameter("status", r.status())
                    .executeUpdate();
            written++;
        }
        return written;
    }

    private static AdministrativeUnit toDomain(Tuple t) {
        return new AdministrativeUnit(
                t.get("code", String.class),
                t.get("name", String.class),
                t.get("full_name", String.class),
                t.get("level", String.class),
                t.get("type", String.class),
                t.get("parent_code", String.class)
        );
    }
}
