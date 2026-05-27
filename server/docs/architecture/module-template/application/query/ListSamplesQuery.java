package com.ares.car_rental_monolith.modules.sample.application.query;

// Tham số đọc/phân trang. Chuẩn hóa page/size và "all"/blank -> null.
public record ListSamplesQuery(String q, String status, int page, int size) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public static ListSamplesQuery from(String q, String status, Integer page, Integer size) {
        int safePage = (page == null || page < 1) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size < 1) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        String normalizedStatus = (status == null || status.isBlank() || status.equalsIgnoreCase("all"))
                ? null : status.trim();
        return new ListSamplesQuery(q == null ? "" : q.trim(), normalizedStatus, safePage, safeSize);
    }

    public int pageIndex() {
        return page - 1;
    }
}
