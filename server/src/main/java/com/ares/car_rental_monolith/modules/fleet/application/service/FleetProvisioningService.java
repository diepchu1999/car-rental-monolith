package com.ares.car_rental_monolith.modules.fleet.application.service;

import com.ares.car_rental_monolith.modules.fleet.api.FleetVehicleProvisioning;
import com.ares.car_rental_monolith.modules.fleet.api.RegisterCompanyVehicleCommand;
import com.ares.car_rental_monolith.modules.fleet.application.port.out.WriteFleetPort;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class FleetProvisioningService implements FleetVehicleProvisioning {

    private final WriteFleetPort port;

    FleetProvisioningService(WriteFleetPort port) {
        this.port = port;
    }

    @Override
    public UUID registerCompanyVehicle(RegisterCompanyVehicleCommand command) {
        if (command.vehicleId() == null) {
            throw DomainException.validation("vehicleId is required");
        }
        String assetCode = command.assetCode() == null ? null : command.assetCode().trim();
        if (assetCode == null || assetCode.isBlank()) {
            throw DomainException.validation("assetCode is required for company vehicles");
        }
        if (port.assetCodeExists(assetCode)) {
            throw DomainException.conflict("assetCode already exists: " + assetCode);
        }
        if (command.branchId() != null && !port.branchExists(command.branchId())) {
            throw DomainException.validation("branchId does not exist");
        }

        UUID companyVehicleId = UUID.randomUUID();
        port.insertCompanyVehicle(companyVehicleId, command.vehicleId(), assetCode, command.branchId());
        return companyVehicleId;
    }
}
