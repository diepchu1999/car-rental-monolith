package com.ares.car_rental_monolith.shared.sql;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * Đọc SQL từ classpath ({@code src/main/resources}) và cache theo resourcePath.
 *
 * <p>Cache nằm trong instance của bean singleton — KHÔNG phải static/global
 * mutable state. File SQL là build artifact read-only nên cache vĩnh viễn an
 * toàn, tránh đọc đĩa lặp lại mỗi lần adapter execute query. Với devtools,
 * mỗi lần restart classloader tạo bean mới → cache tự reset.
 */
@Component
public class ClasspathSqlQueryLoader implements SqlLoader {

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    @Override
    public String load(String resourcePath) {
        return cache.computeIfAbsent(resourcePath, ClasspathSqlQueryLoader::read);
    }

    private static String read(String resourcePath) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            throw new IllegalArgumentException(
                    "SQL resource not found on classpath: " + resourcePath);
        }
        try (InputStream in = resource.getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Failed to read SQL resource: " + resourcePath, e);
        }
    }
}
