package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request;

import java.util.List;

public record SyncFeaturesRequest(List<FeatureItem> features) {
    public record FeatureItem(String code, String name) {}
}
