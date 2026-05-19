package com.ares.car_rental_monolith.shared.config;

public final class RepositoryQueryContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private RepositoryQueryContext() {}

    public static void set(String repositoryName) {
        CURRENT.set(repositoryName);
    }

    public static String get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
