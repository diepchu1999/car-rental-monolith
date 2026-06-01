package com.ares.car_rental_monolith.modules.customer.application.query;

import com.ares.car_rental_monolith.shared.api.PageParams;

public record SearchCustomersQuery(String q, int page, int size, boolean hostOnly) {

    public static SearchCustomersQuery from(String q, Integer page, Integer size, Boolean hostOnly) {
        PageParams pp = PageParams.normalize(page, size, 20, 50);
        String normalized = q == null ? "" : q.trim();
        return new SearchCustomersQuery(normalized, pp.page(), pp.size(), Boolean.TRUE.equals(hostOnly));
    }

    public int pageIndex() {
        return page - 1;
    }
}
