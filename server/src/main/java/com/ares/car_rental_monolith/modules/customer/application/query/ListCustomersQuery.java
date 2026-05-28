package com.ares.car_rental_monolith.modules.customer.application.query;

// Filters + paging for the admin customer list. role is lower-case
// (renter/host/both); status and kyc are the raw enum values. "all"/blank
// becomes null = no filter.
public record ListCustomersQuery(
        String q,
        String role,
        String status,
        String kyc,
        int page,
        int size
) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public static ListCustomersQuery from(
            String q, String role, String status, String kyc, Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return new ListCustomersQuery(
                q == null ? "" : q.trim(),
                normalizeRole(role),
                normalizeFilter(status),
                normalizeFilter(kyc),
                safePage, safeSize
        );
    }

    private static String normalizeRole(String role) {
        if (role == null) return null;
        String r = role.trim().toLowerCase();
        return (r.isEmpty() || r.equals("all")) ? null : r;
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
