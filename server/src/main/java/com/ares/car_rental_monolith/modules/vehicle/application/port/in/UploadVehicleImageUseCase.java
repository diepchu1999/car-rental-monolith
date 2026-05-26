package com.ares.car_rental_monolith.modules.vehicle.application.port.in;

import com.ares.car_rental_monolith.modules.vehicle.application.command.UploadVehicleImageCommand;

@FunctionalInterface
public interface UploadVehicleImageUseCase {

    // Stores the uploaded file and returns the generated filename to persist.
    String handle(UploadVehicleImageCommand command);
}
