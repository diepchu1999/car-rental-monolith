package com.ares.car_rental_monolith.modules.location.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.location.application.command.ImportAdministrativeUnitRecord;
import com.ares.car_rental_monolith.modules.location.application.port.out.LoadAdministrativeUnitPort;
import com.ares.car_rental_monolith.modules.location.application.port.out.WriteAdministrativeUnitPort;
import com.ares.car_rental_monolith.modules.location.domain.AdministrativeUnit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class AdministrativeUnitPersistenceAdapter
        implements LoadAdministrativeUnitPort, WriteAdministrativeUnitPort {

    private static final String SELECT_COLS = """
            SELECT code, name, full_name, level, type, parent_code
            FROM location.administrative_units
            """;

    private static final String BY_LEVEL_SQL = SELECT_COLS + """
            WHERE status = 'ACTIVE' AND level = :level
            ORDER BY name ASC
            """;

    private static final String BY_PROVINCE_SQL = SELECT_COLS + """
            WHERE status = 'ACTIVE' AND level = 'COMMUNE' AND parent_code = :provinceCode
            ORDER BY name ASC
            """;

    private static final String SEARCH_SQL = SELECT_COLS + """
            WHERE status = 'ACTIVE'
              AND (:q = '' OR name ILIKE CONCAT('%', :q, '%') OR full_name ILIKE CONCAT('%', :q, '%'))
              AND (CAST(:level AS TEXT) IS NULL OR level = :level)
              AND (CAST(:provinceCode AS TEXT) IS NULL OR parent_code = :provinceCode)
            ORDER BY level ASC, name ASC
            LIMIT :lim
            """;

    private static final String BY_CODE_SQL = SELECT_COLS + """
            WHERE status = 'ACTIVE' AND code = :code
            """;

    private static final String UPSERT_SQL = """
            INSERT INTO location.administrative_units
                (code, name, full_name, level, type, parent_code,
                 effective_from, effective_to, status, created_at, updated_at)
            VALUES
                (:code, :name, :fullName, :level, :type, :parentCode,
                 :effectiveFrom, :effectiveTo, :status, now(), now())
            ON CONFLICT (code) DO UPDATE SET
                name = EXCLUDED.name,
                full_name = EXCLUDED.full_name,
                level = EXCLUDED.level,
                type = EXCLUDED.type,
                parent_code = EXCLUDED.parent_code,
                effective_from = EXCLUDED.effective_from,
                effective_to = EXCLUDED.effective_to,
                status = EXCLUDED.status,
                updated_at = now()
            """;

    private final EntityManager em;

    AdministrativeUnitPersistenceAdapter(EntityManager em) {
        this.em = em;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AdministrativeUnit> listByLevel(String level) {
        List<Tuple> rows = em.createNativeQuery(BY_LEVEL_SQL, Tuple.class)
                .setParameter("level", level)
                .getResultList();
        return rows.stream().map(AdministrativeUnitPersistenceAdapter::toDomain).toList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AdministrativeUnit> listCommunesByProvince(String provinceCode) {
        List<Tuple> rows = em.createNativeQuery(BY_PROVINCE_SQL, Tuple.class)
                .setParameter("provinceCode", provinceCode)
                .getResultList();
        return rows.stream().map(AdministrativeUnitPersistenceAdapter::toDomain).toList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AdministrativeUnit> search(String query, String level, String provinceCode, int limit) {
        List<Tuple> rows = em.createNativeQuery(SEARCH_SQL, Tuple.class)
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
        List<Tuple> rows = em.createNativeQuery(BY_CODE_SQL, Tuple.class)
                .setParameter("code", code)
                .getResultList();
        return rows.stream().findFirst().map(AdministrativeUnitPersistenceAdapter::toDomain);
    }

    @Override
    public int upsertAll(List<ImportAdministrativeUnitRecord> records) {
        int written = 0;
        for (ImportAdministrativeUnitRecord r : records) {
            em.createNativeQuery(UPSERT_SQL)
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
