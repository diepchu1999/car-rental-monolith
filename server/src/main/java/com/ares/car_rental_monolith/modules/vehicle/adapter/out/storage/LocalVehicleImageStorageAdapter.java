package com.ares.car_rental_monolith.modules.vehicle.adapter.out.storage;

import com.ares.car_rental_monolith.modules.vehicle.application.port.out.VehicleImageStoragePort;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class LocalVehicleImageStorageAdapter implements VehicleImageStoragePort {

    private final Path baseDir;

    LocalVehicleImageStorageAdapter(
            @Value("${app.media.vehicle-images-dir}") String dir) {
        this.baseDir = Path.of(dir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void ensureDirectory() {
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create vehicle image directory: " + baseDir, e);
        }
    }

    @Override
    public String store(byte[] content, String extension) {
        String filename = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
        Path target = baseDir.resolve(filename).normalize();
        try {
            Files.write(target, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot store vehicle image: " + filename, e);
        }
        return filename;
    }
}
