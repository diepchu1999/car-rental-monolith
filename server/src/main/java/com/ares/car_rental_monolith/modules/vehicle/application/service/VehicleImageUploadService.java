package com.ares.car_rental_monolith.modules.vehicle.application.service;

import com.ares.car_rental_monolith.modules.vehicle.application.command.UploadVehicleImageCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.UploadVehicleImageUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.VehicleImageStoragePort;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
class VehicleImageUploadService implements UploadVehicleImageUseCase {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final VehicleImageStoragePort storage;

    VehicleImageUploadService(VehicleImageStoragePort storage) {
        this.storage = storage;
    }

    @Override
    public String handle(UploadVehicleImageCommand command) {
        if (command.content() == null || command.content().length == 0) {
            throw DomainException.validation("file is empty");
        }
        if (command.contentType() == null || !command.contentType().startsWith("image/")) {
            throw DomainException.validation("file must be an image");
        }
        String extension = extensionOf(command.originalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw DomainException.validation(
                    "unsupported image type, allowed: " + ALLOWED_EXTENSIONS);
        }
        return storage.store(command.content(), extension);
    }

    private static String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }
}
