package com.ares.car_rental_monolith.modules.vehicle.application.command;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.List;
import java.util.UUID;

public record SyncVehicleImagesCommand(UUID vehicleId, List<ImageInput> images) {

    public record ImageInput(String fileUrl, int sortOrder, boolean cover) {}

    public static SyncVehicleImagesCommand from(UUID vehicleId, List<ImageInput> images) {
        if (vehicleId == null) throw DomainException.validation("vehicleId is required");
        if (images == null) throw DomainException.validation("images is required");
        long coverCount = images.stream().filter(ImageInput::cover).count();
        if (coverCount > 1) {
            throw DomainException.validation("Only one image can be marked as cover");
        }
        for (ImageInput image : images) {
            if (image.fileUrl() == null || image.fileUrl().isBlank()) {
                throw DomainException.validation("image.fileUrl is required");
            }
        }
        return new SyncVehicleImagesCommand(vehicleId, List.copyOf(images));
    }
}
