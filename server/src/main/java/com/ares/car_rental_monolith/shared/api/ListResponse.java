package com.ares.car_rental_monolith.shared.api;

import java.util.List;

public record ListResponse<T>(List<T> items, int total) {
    public static <T> ListResponse<T> of(List<T> items) {
        return new ListResponse<>(items, items.size());
    }
}
