package com.ares.car_rental_monolith.modules.sample.application.query;

import com.ares.car_rental_monolith.shared.api.PageParams;

// Tham số đọc/phân trang. Chuẩn hóa page/size qua PageParams (mỗi endpoint tự
// chọn defaultSize/maxSize); "all"/blank -> null.
public record ListSamplesQuery(String q, String status, int page, int size) {

    public static ListSamplesQuery from(String q, String status, Integer page, Integer size) {
        PageParams pp = PageParams.normalize(page, size, 20, 100);
        String normalizedStatus = (status == null || status.isBlank() || status.equalsIgnoreCase("all"))
                ? null : status.trim();
        return new ListSamplesQuery(q == null ? "" : q.trim(), normalizedStatus, pp.page(), pp.size());
    }

    public int pageIndex() {
        return page - 1;
    }
}
