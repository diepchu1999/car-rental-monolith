package com.ares.car_rental_monolith.modules.driver.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.driver.application.port.out.LoadDriverPort;
import com.ares.car_rental_monolith.modules.driver.application.query.SearchDriversQuery;
import com.ares.car_rental_monolith.modules.driver.domain.DriverSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class DriverPersistenceAdapter implements LoadDriverPort {

    private final EntityManager em;
    private final SqlLoader sql;

    DriverPersistenceAdapter(EntityManager em, SqlLoader sql) {
        this.em = em;
        this.sql = sql;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PageResponse<DriverSummary> search(SearchDriversQuery query) {
        int size = query.size();
        int offset = query.pageIndex() * size;

        long total = ((Number) em.createNativeQuery(sql.load(DriverSqlPaths.DRIVERS_COUNT))
                .setParameter("q", query.q())
                .setParameter("status", query.status())
                .getSingleResult()).longValue();

        List<Tuple> rows = em.createNativeQuery(sql.load(DriverSqlPaths.DRIVERS_DATA), Tuple.class)
                .setParameter("q", query.q())
                .setParameter("status", query.status())
                .setParameter("lim", size)
                .setParameter("off", offset)
                .getResultList();

        List<DriverSummary> items = rows.stream().map(DriverPersistenceAdapter::toDomain).toList();
        int page = query.pageIndex() + 1;
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);
        return PageResponse.of(items, total, page, size, totalPages, page < totalPages, page > 1);
    }

    @Override
    public Optional<DriverSummary> findById(UUID id) {
        try {
            Tuple t = (Tuple) em.createNativeQuery(sql.load(DriverSqlPaths.DRIVER_DETAIL), Tuple.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(toDomain(t));
        } catch (NoResultException ignored) {
            return Optional.empty();
        }
    }

    private static DriverSummary toDomain(Tuple t) {
        Object idVal = t.get("id");
        UUID id = idVal instanceof UUID u ? u : UUID.fromString(idVal.toString());
        Object expiryVal = t.get("license_expiry_date");
        LocalDate expiry = expiryVal == null ? null
                : expiryVal instanceof LocalDate ld ? ld
                : expiryVal instanceof Date d ? d.toLocalDate()
                : null;
        return new DriverSummary(
                id,
                t.get("driver_code", String.class),
                t.get("full_name", String.class),
                t.get("phone", String.class),
                t.get("license_number", String.class),
                t.get("license_class", String.class),
                expiry,
                t.get("years_of_experience", Integer.class),
                t.get("rating_average", BigDecimal.class),
                t.get("rating_count", Integer.class),
                t.get("status", String.class)
        );
    }
}
