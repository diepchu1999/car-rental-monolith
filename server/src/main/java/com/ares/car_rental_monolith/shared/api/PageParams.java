package com.ares.car_rental_monolith.shared.api;

/**
 * Chuẩn hóa tham số phân trang từ request (page/size có thể null/âm) về giá trị
 * an toàn. Gom logic lặp ở các *Query record. Mỗi endpoint tự truyền
 * {@code defaultSize}/{@code maxSize} riêng (giới hạn trang có chủ đích khác
 * nhau giữa search vs list).
 *
 * <p>page là 1-based (khớp API); {@link #pageIndex()} trả 0-based cho OFFSET.
 */
public record PageParams(int page, int size) {

    public static PageParams normalize(Integer page, Integer size, int defaultSize, int maxSize) {
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? defaultSize : Math.min(size, maxSize);
        return new PageParams(safePage, safeSize);
    }

    public int pageIndex() {
        return page - 1;
    }
}
