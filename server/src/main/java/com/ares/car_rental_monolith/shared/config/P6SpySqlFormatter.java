package com.ares.car_rental_monolith.shared.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import java.util.Locale;

public class P6SpySqlFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(
            int connectionId, String now, long elapsed,
            String category, String prepared, String sql, String url) {
        if (sql == null || sql.isBlank()) {
            return "";
        }
        String caller = RepositoryQueryContext.get();
        String callerInfo = (caller != null) ? " | " + caller : "";
        return System.lineSeparator()
                + "SQL " + elapsed + "ms | " + category + callerInfo
                + System.lineSeparator()
                + formatSql(sql)
                + ";"
                + System.lineSeparator();
    }

    private String formatSql(String sql) {
        String formatted = sql.trim().replaceAll("\\s+", " ");
        String lower = formatted.toLowerCase(Locale.ROOT);

        if (lower.startsWith("select ")) {
            return formatSelect(formatted);
        }
        if (lower.startsWith("insert ")) {
            return formatted
                    .replaceFirst("(?i)^insert into ", "insert into" + System.lineSeparator() + "    ")
                    .replaceFirst("(?i) values ", System.lineSeparator() + "values" + System.lineSeparator() + "    ");
        }
        if (lower.startsWith("update ")) {
            return formatted
                    .replaceFirst("(?i)^update ", "update" + System.lineSeparator() + "    ")
                    .replaceFirst("(?i) set ", System.lineSeparator() + "set" + System.lineSeparator() + "    ")
                    .replaceFirst("(?i) where ", System.lineSeparator() + "where" + System.lineSeparator() + "    ")
                    .replaceAll("(?i) and ", System.lineSeparator() + "    and ");
        }
        if (lower.startsWith("delete ")) {
            return formatted
                    .replaceFirst("(?i)^delete from ", "delete from" + System.lineSeparator() + "    ")
                    .replaceFirst("(?i) where ", System.lineSeparator() + "where" + System.lineSeparator() + "    ")
                    .replaceAll("(?i) and ", System.lineSeparator() + "    and ");
        }
        return formatted;
    }

    private String formatSelect(String sql) {
        int fromIndex = sql.toLowerCase(Locale.ROOT).indexOf(" from ");
        if (fromIndex < 0) return sql;

        String selectColumns = sql.substring("select ".length(), fromIndex);
        String remaining = sql.substring(fromIndex + " from ".length());

        StringBuilder formatted = new StringBuilder("select");
        for (String column : selectColumns.split(",")) {
            formatted.append(System.lineSeparator()).append("    ").append(column.trim()).append(",");
        }
        if (formatted.charAt(formatted.length() - 1) == ',') {
            formatted.deleteCharAt(formatted.length() - 1);
        }
        formatted.append(System.lineSeparator()).append("from").append(System.lineSeparator())
                .append("    ").append(remaining);

        return formatted.toString()
                .replaceFirst("(?i) where ", System.lineSeparator() + "where" + System.lineSeparator() + "    ")
                .replaceAll("(?i) and ", System.lineSeparator() + "    and ")
                .replaceAll("(?i) or ", System.lineSeparator() + "    or ")
                .replaceFirst("(?i) order by ", System.lineSeparator() + "order by" + System.lineSeparator() + "    ")
                .replaceFirst("(?i) group by ", System.lineSeparator() + "group by" + System.lineSeparator() + "    ")
                .replaceFirst("(?i) limit ", System.lineSeparator() + "limit ");
    }
}
