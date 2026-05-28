package com.ares.car_rental_monolith.modules.customer.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.customer.application.port.out.PageCustomersPort;
import com.ares.car_rental_monolith.modules.customer.application.query.ListCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import com.ares.car_rental_monolith.modules.customer.application.view.KycAggregateStatus;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

// Paged admin customer list. One COUNT + one DATA query, cùng dùng WHERE build
// từ filters. Multi-KYC: KHÔNG JOIN trực tiếp kyc_profiles (sẽ nhân row), thay
// bằng LATERAL subquery aggregate (rẻ nhờ index (customer_id, status)). Trang
// list không nạp kycs cụ thể, chỉ trả về kyc_aggregate_status để FE hiển thị
// badge. Addresses cũng không nạp ở list — chỉ detail mới.
@Component
class CustomerPageAdapter implements PageCustomersPort {

    // Aggregate KYC counts inline; alias `ka` được dùng cả ở SELECT (CASE) và
    // WHERE (filter theo aggregate status).
    private static final String JOINS = """
            FROM customer.customers c
            LEFT JOIN customer.host_profiles hp ON hp.customer_id = c.id
            LEFT JOIN LATERAL (
                SELECT
                    COUNT(*) AS total,
                    COUNT(*) FILTER (WHERE status = 'APPROVED') AS approved,
                    COUNT(*) FILTER (WHERE status = 'REJECTED') AS rejected,
                    COUNT(*) FILTER (WHERE status IN ('PENDING', 'EXPIRED')) AS pending
                FROM customer.kyc_profiles
                WHERE customer_id = c.id
            ) ka ON TRUE
            """;

    // Aggregate status được derive bằng CASE; phải khớp KycAggregateStatus enum.
    private static final String KYC_AGGREGATE_CASE = """
            CASE
                WHEN ka.total = 0 THEN 'NO_KYC'
                WHEN ka.approved = ka.total THEN 'FULLY_APPROVED'
                WHEN ka.rejected = ka.total THEN 'REJECTED'
                WHEN ka.approved > 0 THEN 'PARTIALLY_APPROVED'
                ELSE 'PENDING'
            END
            """;

    private static final String DATA_SELECT = """
            SELECT
                c.id, c.full_name, c.phone, c.email, c.date_of_birth, c.gender,
                c.status, c.created_at,
                (SELECT string_agg(cr.role, ',' ORDER BY cr.role)
                 FROM customer.customer_roles cr WHERE cr.customer_id = c.id) AS roles,
                hp.host_code, hp.display_name, hp.bio, hp.rating_average,
                hp.rating_count, hp.status AS host_status, hp.created_at AS host_created_at,
            """ + KYC_AGGREGATE_CASE + """
                AS kyc_aggregate_status,
                (SELECT COUNT(*) FROM booking.bookings b WHERE b.customer_id = c.id) AS booking_count,
                (SELECT COUNT(*) FROM vehicle.vehicles v WHERE v.owner_customer_id = c.id) AS vehicle_count,
                (SELECT COALESCE(SUM(b.total_amount), 0) FROM booking.bookings b
                 WHERE b.host_customer_id = c.id AND b.status = 'COMPLETED') AS total_revenue
            """;

    private final EntityManager em;

    CustomerPageAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    public PageResponse<CustomerDetail> page(ListCustomersQuery query) {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = buildWhere(query, params);

        Query countQuery = em.createNativeQuery("SELECT COUNT(*) " + JOINS + where);
        params.forEach(countQuery::setParameter);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        int size = query.size();
        int offset = query.pageIndex() * size;

        Query dataQuery = em.createNativeQuery(
                DATA_SELECT + JOINS + where
                        + " ORDER BY c.created_at DESC, c.id ASC LIMIT :lim OFFSET :off",
                Tuple.class);
        params.forEach(dataQuery::setParameter);
        dataQuery.setParameter("lim", size);
        dataQuery.setParameter("off", offset);

        @SuppressWarnings("unchecked")
        List<Tuple> rows = dataQuery.getResultList();
        List<CustomerDetail> items = rows.stream().map(CustomerPageAdapter::toDetail).toList();

        int page = query.pageIndex() + 1;
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);
        return PageResponse.of(items, total, page, size, totalPages, page < totalPages, page > 1);
    }

    private static String buildWhere(ListCustomersQuery q, Map<String, Object> params) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");

        if (q.q() != null && !q.q().isEmpty()) {
            where.append("""
                     AND (c.full_name ILIKE :q
                          OR COALESCE(c.phone, '') ILIKE :q
                          OR COALESCE(c.email, '') ILIKE :q
                          OR COALESCE(hp.host_code, '') ILIKE :q)
                    """);
            params.put("q", "%" + q.q() + "%");
        }

        if (q.role() != null) {
            switch (q.role()) {
                case "renter" -> where.append(roleExists("RENTER"));
                case "host" -> where.append(roleExists("HOST"));
                case "both" -> where.append(roleExists("RENTER")).append(roleExists("HOST"));
                default -> { }
            }
        }

        if (q.status() != null) {
            where.append(" AND c.status = :status");
            params.put("status", q.status());
        }

        if (q.kyc() != null) {
            // Filter theo aggregate KYC status, dùng cùng CASE expression ở
            // SELECT. Postgres không cho dùng alias trong WHERE nên inline.
            where.append(" AND ").append(KYC_AGGREGATE_CASE).append(" = :kyc");
            params.put("kyc", q.kyc());
        }

        return where.toString();
    }

    private static String roleExists(String role) {
        return " AND EXISTS (SELECT 1 FROM customer.customer_roles r WHERE r.customer_id = c.id AND r.role = '"
                + role + "')";
    }

    private static CustomerDetail toDetail(Tuple t) {
        String aggregate = t.get("kyc_aggregate_status", String.class);
        return new CustomerDetail(
                uuid(t, "id"),
                t.get("full_name", String.class),
                t.get("phone", String.class),
                t.get("email", String.class),
                localDate(t, "date_of_birth"),
                t.get("gender", String.class),
                t.get("status", String.class),
                dateTime(t, "created_at"),
                roles(t.get("roles", String.class)),
                host(t),
                // List page không nạp chi tiết KYC — FE dùng aggregate badge và
                // sẽ fetch detail riêng khi mở popup.
                List.of(),
                KycAggregateStatus.valueOf(aggregate),
                List.of(),
                new CustomerDetail.Activity(
                        longValue(t.get("booking_count")),
                        longValue(t.get("vehicle_count")),
                        bigDecimal(t.get("total_revenue")))
        );
    }

    private static List<String> roles(String agg) {
        if (agg == null || agg.isBlank()) return List.of();
        return Arrays.stream(agg.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static CustomerDetail.HostProfile host(Tuple t) {
        String hostCode = t.get("host_code", String.class);
        if (hostCode == null) return null;
        return new CustomerDetail.HostProfile(
                hostCode,
                t.get("display_name", String.class),
                t.get("bio", String.class),
                t.get("rating_average", BigDecimal.class),
                intValue(t.get("rating_count")),
                t.get("host_status", String.class),
                dateTime(t, "host_created_at")
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
        if (v instanceof Timestamp ts) return ts.toInstant().atOffset(ZoneOffset.UTC);
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

    private static BigDecimal bigDecimal(Object v) {
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}
