package com.ares.car_rental_monolith.modules.driver.application.query;

public record SearchDriversQuery(String q, String status, int page, int size) {

    public static SearchDriversQuery from(String q, String status, Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 20 : Math.min(size, 100);
        return new SearchDriversQuery(
                q == null ? "" : q.trim(),
                status == null || status.isBlank() ? null : status.trim().toUpperCase(),
                safePage, safeSize);
    }

    public int pageIndex() {
        return page - 1;
    }
}
