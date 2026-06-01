package com.ares.car_rental_monolith.shared.persistence;

import jakarta.persistence.Tuple;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Chuyển giá trị cột từ native-query {@link Tuple} sang kiểu Java. Gom về 1 nơi
 * vì Hibernate 6 + pgjdbc có thể trả nhiều kiểu khác nhau cho cùng 1 kiểu cột
 * (TIMESTAMPTZ → Instant/Timestamp/LocalDateTime/OffsetDateTime tùy ngữ cảnh
 * query); xử lý rải rác từng adapter dễ ra null ngoài ý muốn.
 *
 * <p>CHỈ map kiểu — không chứa business logic. Persistence adapter vẫn tự
 * execute query và build domain/view object.
 */
public final class Tuples {

    private Tuples() {}

    /** UUID từ cột; chấp nhận cả UUID lẫn String (pgjdbc đôi khi trả String). */
    public static UUID uuid(Tuple t, String col) {
        Object v = t.get(col);
        if (v == null) return null;
        return v instanceof UUID u ? u : UUID.fromString(v.toString());
    }

    /** OffsetDateTime (UTC) từ cột timestamp, gộp mọi kiểu pgjdbc có thể trả. */
    public static OffsetDateTime dateTime(Tuple t, String col) {
        Object v = t.get(col);
        if (v == null) return null;
        if (v instanceof OffsetDateTime odt) return odt;
        if (v instanceof Instant instant) return instant.atOffset(ZoneOffset.UTC);
        if (v instanceof Timestamp ts) return ts.toInstant().atOffset(ZoneOffset.UTC);
        if (v instanceof LocalDateTime ldt) return ldt.atOffset(ZoneOffset.UTC);
        return null;
    }

    /** LocalDate từ cột date (LocalDate hoặc java.sql.Date). */
    public static LocalDate localDate(Tuple t, String col) {
        Object v = t.get(col);
        if (v == null) return null;
        if (v instanceof LocalDate ld) return ld;
        if (v instanceof Date d) return d.toLocalDate();
        return null;
    }

    /** int từ giá trị Number (vd COUNT/aggregate); null/khác → 0. */
    public static int intValue(Object v) {
        return v instanceof Number n ? n.intValue() : 0;
    }

    /** long từ giá trị Number (vd COUNT/aggregate); null/khác → 0. */
    public static long longValue(Object v) {
        return v instanceof Number n ? n.longValue() : 0L;
    }

    /** BigDecimal từ giá trị Number; null/khác → ZERO. */
    public static BigDecimal bigDecimal(Object v) {
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}
