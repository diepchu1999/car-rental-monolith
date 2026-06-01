package com.ares.car_rental_monolith.modules.driver.application.query;

import com.ares.car_rental_monolith.shared.api.PageParams;

public record SearchDriversQuery(String q, String status, int page, int size) {

    public static SearchDriversQuery from(String q, String status, Integer page, Integer size) {
        PageParams pp = PageParams.normalize(page, size, 20, 100);
        return new SearchDriversQuery(
                q == null ? "" : q.trim(),
                status == null || status.isBlank() ? null : status.trim().toUpperCase(),
                pp.page(), pp.size());
    }

    public int pageIndex() {
        return page - 1;
    }
}
