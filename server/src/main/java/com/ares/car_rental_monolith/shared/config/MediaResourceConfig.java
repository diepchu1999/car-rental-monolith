package com.ares.car_rental_monolith.shared.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Serves uploaded media straight from the filesystem so the admin UI / Postman
// can fetch by URL. Each media kind has its own base dir + URL prefix:
//   /media/vehicle-images/**  -> vehicle image uploads
//   /media/kyc-documents/**   -> customer KYC document uploads
@Configuration
public class MediaResourceConfig implements WebMvcConfigurer {

    private final String vehicleImagesDir;
    private final String kycDocumentsDir;

    public MediaResourceConfig(
            @Value("${app.media.vehicle-images-dir}") String vehicleImagesDir,
            @Value("${app.media.kyc-documents-dir}") String kycDocumentsDir) {
        this.vehicleImagesDir = vehicleImagesDir;
        this.kycDocumentsDir = kycDocumentsDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/vehicle-images/**")
                .addResourceLocations(toLocation(vehicleImagesDir));
        registry.addResourceHandler("/media/kyc-documents/**")
                .addResourceLocations(toLocation(kycDocumentsDir));
    }

    private static String toLocation(String dir) {
        String location = Path.of(dir).toAbsolutePath().normalize().toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
