package com.ares.car_rental_monolith.shared.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Serves uploaded vehicle images straight from the filesystem so the admin UI can
// render them by filename via /media/vehicle-images/{fileName}.
@Configuration
public class MediaResourceConfig implements WebMvcConfigurer {

    private final String vehicleImagesDir;

    public MediaResourceConfig(
            @Value("${app.media.vehicle-images-dir}") String vehicleImagesDir) {
        this.vehicleImagesDir = vehicleImagesDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(vehicleImagesDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/media/vehicle-images/**")
                .addResourceLocations(location);
    }
}
