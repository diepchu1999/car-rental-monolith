package com.ares.car_rental_monolith.shared.api;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> items,
        long total,
        int page,
        int size,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResponse<T> of(
            List<T> items, long total, int page, int size,
            int totalPages, boolean hasNext, boolean hasPrevious
    ) {
        return new PageResponse<>(items, total, page, size, totalPages, hasNext, hasPrevious);
    }

    public <U> PageResponse<U> map(Function<? super T, ? extends U> mapper) {
        return new PageResponse<>(
                items.stream().<U>map(mapper).toList(),
                total, page, size, totalPages, hasNext, hasPrevious
        );
    }
}
