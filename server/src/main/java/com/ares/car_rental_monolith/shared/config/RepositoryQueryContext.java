package com.ares.car_rental_monolith.shared.config;

import org.slf4j.MDC;

public final class RepositoryQueryContext {

    // Lưu caller qua SLF4J MDC thay vì ThreadLocal static của chính lớp này.
    // Lý do: spring-boot-devtools chạy 2 classloader — bean Spring (RepositoryLoggingAspect)
    // nằm ở restart classloader, còn p6spy (cắm làm JDBC driver) + P6SpySqlFormatter do
    // base classloader nạp. Một ThreadLocal static sẽ bị nạp 2 bản khác nhau → aspect set
    // ở bản này, formatter đọc bản kia = luôn null. org.slf4j.MDC nằm trong slf4j-api (chỉ
    // base loader nạp, cả 2 loader cùng delegate về đó) nên là store dùng chung, hết lệch.
    // MDC cũng là cách chính tắc để gắn context vào dòng log, dùng được cả khi không có devtools.
    private static final String CALLER_KEY = "sqlCaller";

    private RepositoryQueryContext() {}

    public static void set(String repositoryName) {
        MDC.put(CALLER_KEY, repositoryName);
    }

    public static String get() {
        return MDC.get(CALLER_KEY);
    }

    public static void clear() {
        MDC.remove(CALLER_KEY);
    }
}
