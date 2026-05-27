package com.ares.car_rental_monolith.modules.customer.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.customer.application.port.out.LoadCustomerPort;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import com.ares.car_rental_monolith.modules.customer.application.view.KycAggregateStatus;
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

    private static final String CUSTOMER_SQL = """
            SELECT id, full_name, phone, email, date_of_birth, gender, status, created_at
            FROM customer.customers
            WHERE id = :id
            """;

    private static final String ROLES_SQL = """
            SELECT role FROM customer.customer_roles
            WHERE customer_id = :id
            ORDER BY role
            """;

    private static final String HOST_SQL = """
            SELECT host_code, display_name, bio, rating_average, rating_count, status, created_at
            FROM customer.host_profiles
            WHERE customer_id = :id
            """;

    // Load toàn bộ KYC của customer, sắp theo ngày submit mới nhất trước để
    // FE hiển thị hồ sơ mới nhất ở đầu list.
    private static final String KYC_LIST_SQL = """
            SELECT id, kyc_code, legal_name, document_type, document_number,
                   issued_date, issued_place, status, reviewed_by, reviewed_at,
                   rejection_reason, created_at
            FROM customer.kyc_profiles
            WHERE customer_id = :id
            ORDER BY created_at DESC, id ASC
            """;

    // Lấy 1 KYC cụ thể kèm ràng buộc thuộc đúng customer (chống IDOR /
    // mismatched path params).
    private static final String KYC_BY_ID_SQL = """
            SELECT id, kyc_code, legal_name, document_type, document_number,
                   issued_date, issued_place, status, reviewed_by, reviewed_at,
                   rejection_reason, created_at
            FROM customer.kyc_profiles
            WHERE id = :kycId AND customer_id = :customerId
            """;

    // Documents cho 1 list các kyc_profile_id — gom 1 query thay vì N+1.
    // ORDER BY document_side theo thứ tự hiển thị FRONT/BACK/SELFIE/OTHER.
    private static final String DOCUMENTS_BY_KYCS_SQL = """
            SELECT id, kyc_profile_id, document_side, file_url, created_at
            FROM customer.kyc_documents
            WHERE kyc_profile_id IN (:kycIds)
            ORDER BY
                CASE document_side
                    WHEN 'FRONT' THEN 0 WHEN 'BACK' THEN 1
                    WHEN 'SELFIE' THEN 2 ELSE 3 END,
                created_at ASC
            """;

    private static final String ADDRESSES_SQL = """
            SELECT a.id, a.label, a.line1, a.province_code, a.commune_code,
                   pu.name AS province_name, cu.name AS commune_name,
                   a.district AS legacy_district, a.is_default
            FROM customer.addresses a
            LEFT JOIN location.administrative_units pu ON pu.code = a.province_code
            LEFT JOIN location.administrative_units cu ON cu.code = a.commune_code
            WHERE a.customer_id = :id
            ORDER BY a.is_default DESC, a.created_at ASC
            """;

    private static final String ACTIVITY_SQL = """
            SELECT
                (SELECT COUNT(*) FROM booking.bookings WHERE customer_id = :id) AS booking_count,
                (SELECT COUNT(*) FROM vehicle.vehicles WHERE owner_customer_id = :id) AS vehicle_count,
                (SELECT COALESCE(SUM(total_amount), 0) FROM booking.bookings
                 WHERE host_customer_id = :id AND status = 'COMPLETED') AS total_revenue
            """;

    private final EntityManager em;

    CustomerLoadAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    public boolean isActiveCustomer(UUID customerId) {
        Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM customer.customers WHERE id = :id AND status = 'ACTIVE'")
                .setParameter("id", customerId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public boolean existsCustomer(UUID customerId) {
        Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM customer.customers WHERE id = :id")
                .setParameter("id", customerId)
                .getSingleResult();
        return count.longValue() > 0;
    }

    @Override
    public Optional<CustomerDetail> loadCustomerDetail(UUID id) {
        Tuple c;
        try {
            c = (Tuple) em.createNativeQuery(CUSTOMER_SQL, Tuple.class)
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
            t = (Tuple) em.createNativeQuery(KYC_BY_ID_SQL, Tuple.class)
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
        List<?> rows = em.createNativeQuery(ROLES_SQL)
                .setParameter("id", id)
                .getResultList();
        return rows.stream().map(Object::toString).toList();
    }

    private CustomerDetail.HostProfile loadHost(UUID id) {
        try {
            Tuple t = (Tuple) em.createNativeQuery(HOST_SQL, Tuple.class)
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
        List<Tuple> rows = em.createNativeQuery(KYC_LIST_SQL, Tuple.class)
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
        List<Tuple> rows = em.createNativeQuery(DOCUMENTS_BY_KYCS_SQL, Tuple.class)
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
        List<Tuple> rows = em.createNativeQuery(ADDRESSES_SQL, Tuple.class)
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
        Tuple t = (Tuple) em.createNativeQuery(ACTIVITY_SQL, Tuple.class)
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
