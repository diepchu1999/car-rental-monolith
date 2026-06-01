package com.ares.car_rental_monolith.modules.fleet.application.query;

import com.ares.car_rental_monolith.shared.api.PageParams;

public record ListFleetBranchesQuery(
        String q,
        String status,
        Integer page,
        Integer size) {

    public static ListFleetBranchesQuery from(
            String q, String status, Integer page, Integer size) {
        PageParams pp = PageParams.normalize(page, size, 20, 100);
        return new ListFleetBranchesQuery(
                q == null ? "" : q.trim(),
                normalizeFilter(status),
                pp.page(), pp.size()
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
