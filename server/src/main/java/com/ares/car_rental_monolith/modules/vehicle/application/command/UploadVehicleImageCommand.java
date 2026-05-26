package com.ares.car_rental_monolith.modules.vehicle.application.command;

// Carries the raw uploaded bytes plus the client-supplied metadata needed to
// validate the file and derive a stored filename. Validation lives in the service.
public record UploadVehicleImageCommand(
        byte[] content,
        String originalFilename,
        String contentType
) {}
