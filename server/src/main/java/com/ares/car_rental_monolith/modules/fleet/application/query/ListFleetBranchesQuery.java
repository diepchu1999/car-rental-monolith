package com.ares.car_rental_monolith.modules.fleet.application.query;

public record ListFleetBranchesQuery(
        String q,
        String status,
        Integer page,
        Integer size) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public static ListFleetBranchesQuery from(
            String q, String status, Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return new ListFleetBranchesQuery(
                q == null ? "" : q.trim(),
                normalizeFilter(status),
                safePage, safeSize
        );
    }

    private static String normalizeFilter(String value) {
        if (value == null) return null;
        String v = value.trim();
        return (v.isEmpty() || v.equalsIgnoreCase("all")) ? null : v;
    }

    public int pageIndex() {
        return page - 1;
    }
}
