package com.ares.car_rental_monolith.modules.fleet.application.port.out;

import java.util.UUID;

public interface WriteFleetPort {

    boolean assetCodeExists(String assetCode);

    boolean branchExists(UUID branchId);

    void insertCompanyVehicle(UUID id, UUID vehicleId, String assetCode, UUID branchId);
}
