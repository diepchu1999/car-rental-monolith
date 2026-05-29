package com.ares.car_rental_monolith.modules.customer.application.query;

public record SearchCustomersQuery(String q, int page, int size, boolean hostOnly) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    public static SearchCustomersQuery from(String q, Integer page, Integer size, Boolean hostOnly) {
        int safePage = (page == null || page < 1) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        String normalized = q == null ? "" : q.trim();
        return new SearchCustomersQuery(normalized, safePage, safeSize, Boolean.TRUE.equals(hostOnly));
    }

    public int pageIndex() {
        return page - 1;
    }
}
