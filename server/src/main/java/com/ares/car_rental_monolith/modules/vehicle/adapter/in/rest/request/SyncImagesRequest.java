package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request;

import java.util.List;

public record SyncImagesRequest(List<ImageItem> images) {
    public record ImageItem(String fileUrl, Integer sortOrder, Boolean cover) {}
}
