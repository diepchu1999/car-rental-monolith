package com.ares.car_rental_monolith.modules.customer.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.customer.application.port.out.LoadCustomerPort;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import com.ares.car_rental_monolith.modules.customer.application.view.KycAggregateStatus;
import com.ares.car_rental_monolith.shared.sql.SqlLoader;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

// Read-side adapter cho LoadCustomerPort: existence/eligibility check
// (isActiveCustomer), full customer detail load và load 1 KYC cụ thể của 1
// customer (cho luồng review/approve/reject).
@Component
class CustomerLoadAdapter implements LoadCustomerPort {

    private final EntityManager em;
    private final SqlLoader sql;

    CustomerLoadAdapter(EntityManager em, SqlLoader sql) {
        this.em = em;
        this.sql = sql;
    }

    @Override
    public boolean isActiveCustomer(UUID customerId) {
        Number count = (Number) em.createNativeQuery(sql.load(CustomerSqlPaths.IS_ACTIVE_CUSTOMER))
                .setParameter("id", customerId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public boolean isActiveHost(UUID customerId) {
        Number count = (Number) em.createNativeQuery(sql.load(CustomerSqlPaths.IS_ACTIVE_HOST))
                .setParameter("id", customerId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public boolean existsCustomer(UUID customerId) {
        Number count = (Number) em.createNativeQuery(sql.load(CustomerSqlPaths.EXISTS_CUSTOMER))
                .setParameter("id", customerId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public Optional<CustomerDetail> loadCustomerDetail(UUID id) {
        Tuple c;
        try {
            c = (Tuple) em.createNativeQuery(sql.load(CustomerSqlPaths.LOAD_CUSTOMER), Tuple.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException ignored) {
            return Optional.empty();
        }

        List<CustomerDetail.Kyc> kycs = loadKycs(id);
        return Optional.of(new CustomerDetail(
                uuid(c, "id"),
                c.get("full_name", String.class),
                c.get("phone", String.class),
                c.get("email", String.class),
                localDate(c, "date_of_birth"),
                c.get("gender", String.class),
                c.get("status", String.class),
                dateTime(c, "created_at"),
                loadRoles(id),
                loadHost(id),
                kycs,
                KycAggregateStatus.from(kycs),
                loadAddresses(id),
                loadActivity(id)
        ));
    }

    @Override
    public Optional<CustomerDetail.Kyc> loadKycForCustomer(UUID customerId, UUID kycId) {
        Tuple t;
        try {
            t = (Tuple) em.createNativeQuery(sql.load(CustomerSqlPaths.LOAD_KYC_BY_ID), Tuple.class)
                    .setParameter("kycId", kycId)
                    .setParameter("customerId", customerId)
                    .getSingleResult();
        } catch (NoResultException ignored) {
            return Optional.empty();
        }
        Map<UUID, List<CustomerDetail.Kyc.Document>> docs =
                loadDocuments(List.of(uuid(t, "id")));
        return Optional.of(toKyc(t, docs));
    }

    @SuppressWarnings("unchecked")
    private List<String> loadRoles(UUID id) {
        List<?> rows = em.createNativeQuery(sql.load(CustomerSqlPaths.LOAD_CUSTOMER_ROLES))
                .setParameter("id", id)
                .getResultList();
        return rows.stream().map(Object::toString).toList();
    }

    private CustomerDetail.HostProfile loadHost(UUID id) {
        try {
            Tuple t = (Tuple) em.createNativeQuery(sql.load(CustomerSqlPaths.LOAD_HOST_PROFILE), Tuple.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return new CustomerDetail.HostProfile(
                    t.get("host_code", String.class),
                    t.get("display_name", String.class),
                    t.get("bio", String.class),
                    t.get("rating_average", BigDecimal.class),
                    intValue(t.get("rating_count")),
                    t.get("status", String.class),
                    dateTime(t, "created_at")
            );
        } catch (NoResultException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<CustomerDetail.Kyc> loadKycs(UUID customerId) {
        List<Tuple> rows = em.createNativeQuery(sql.load(CustomerSqlPaths.LOAD_KYC_LIST), Tuple.class)
                .setParameter("id", customerId)
                .getResultList();
        if (rows.isEmpty()) return List.of();

        List<UUID> ids = rows.stream().map(t -> uuid(t, "id")).toList();
        Map<UUID, List<CustomerDetail.Kyc.Document>> docs = loadDocuments(ids);
        return rows.stream().map(t -> toKyc(t, docs)).toList();
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, List<CustomerDetail.Kyc.Document>> loadDocuments(List<UUID> kycIds) {
        if (kycIds.isEmpty()) return Map.of();
        List<Tuple> rows = em.createNativeQuery(sql.load(CustomerSqlPaths.LOAD_KYC_DOCUMENTS), Tuple.class)
                .setParameter("kycIds", kycIds)
                .getResultList();
        // LinkedHashMap để giữ thứ tự document_side đã ORDER BY ở SQL.
        Map<UUID, List<CustomerDetail.Kyc.Document>> grouped = new HashMap<>();
        for (Tuple t : rows) {
            UUID profileId = uuid(t, "kyc_profile_id");
            grouped.computeIfAbsent(profileId, k -> new ArrayList<>())
                    .add(new CustomerDetail.Kyc.Document(
                            uuid(t, "id"),
                            t.get("document_side", String.class),
                            t.get("file_url", String.class),
                            dateTime(t, "created_at")
                    ));
        }
        return grouped;
    }

    private CustomerDetail.Kyc toKyc(Tuple t, Map<UUID, List<CustomerDetail.Kyc.Document>> docs) {
        UUID kycId = uuid(t, "id");
        return new CustomerDetail.Kyc(
                kycId,
                t.get("kyc_code", String.class),
                t.get("legal_name", String.class),
                t.get("document_type", String.class),
                t.get("document_number", String.class),
                localDate(t, "issued_date"),
                t.get("issued_place", String.class),
                t.get("status", String.class),
                uuid(t, "reviewed_by"),
                dateTime(t, "reviewed_at"),
                t.get("rejection_reason", String.class),
                dateTime(t, "created_at"),
                Collections.unmodifiableList(
                        docs.getOrDefault(kycId, List.of()))
        );
    }

    @SuppressWarnings("unchecked")
    private List<CustomerDetail.Address> loadAddresses(UUID id) {
        List<Tuple> rows = em.createNativeQuery(sql.load(CustomerSqlPaths.LOAD_ADDRESSES), Tuple.class)
                .setParameter("id", id)
                .getResultList();
        return rows.stream().map(t -> new CustomerDetail.Address(
                uuid(t, "id"),
                t.get("label", String.class),
                t.get("line1", String.class),
                t.get("province_code", String.class),
                t.get("commune_code", String.class),
                t.get("province_name", String.class),
                t.get("commune_name", String.class),
                t.get("legacy_district", String.class),
                Boolean.TRUE.equals(t.get("is_default", Boolean.class))
        )).toList();
    }

    private CustomerDetail.Activity loadActivity(UUID id) {
        Tuple t = (Tuple) em.createNativeQuery(sql.load(CustomerSqlPaths.LOAD_CUSTOMER_ACTIVITY), Tuple.class)
                .setParameter("id", id)
                .getSingleResult();
        BigDecimal revenue = t.get("total_revenue", BigDecimal.class);
        return new CustomerDetail.Activity(
                longValue(t.get("booking_count")),
                longValue(t.get("vehicle_count")),
                revenue == null ? BigDecimal.ZERO : revenue
        );
    }

    private static UUID uuid(Tuple t, String col) {
        Object v = t.get(col);
        if (v == null) return null;
        return v instanceof UUID u ? u : UUID.fromString(v.toString());
    }

    private static OffsetDateTime dateTime(Tuple t, String col) {
        Object v = t.get(col);
        if (v == null) return null;
        if (v instanceof OffsetDateTime odt) return odt;
        // Hibernate 6 + pgjdbc trả TIMESTAMPTZ về Instant theo mặc định khi
        // dùng native query qua Tuple → phải handle riêng, không thì sẽ null.
        if (v instanceof Instant instant) return instant.atOffset(ZoneOffset.UTC);
        if (v instanceof Timestamp ts) return ts.toInstant().atOffset(ZoneOffset.UTC);
        if (v instanceof LocalDateTime ldt) return ldt.atOffset(ZoneOffset.UTC);
        return null;
    }

    private static LocalDate localDate(Tuple t, String col) {
        Object v = t.get(col);
        if (v == null) return null;
        if (v instanceof LocalDate ld) return ld;
        if (v instanceof Date d) return d.toLocalDate();
        return null;
    }

    private static int intValue(Object v) {
        return v instanceof Number n ? n.intValue() : 0;
    }

    private static long longValue(Object v) {
        return v instanceof Number n ? n.longValue() : 0L;
    }
}
