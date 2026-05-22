package com.ares.car_rental_monolith.modules.vehicle.application.port.out;

public interface VehicleImageStoragePort {

    // Persists the bytes under a freshly generated name and returns that filename.
    String store(byte[] content, String extension);
}
