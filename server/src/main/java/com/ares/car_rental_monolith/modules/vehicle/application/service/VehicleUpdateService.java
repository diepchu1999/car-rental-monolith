package com.ares.car_rental_monolith.modules.vehicle.application.service;

import com.ares.car_rental_monolith.modules.vehicle.application.command.SyncVehicleFeaturesCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.SyncVehicleImagesCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.UpdateListingCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.UpsertPricePlanCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.SyncVehicleFeaturesUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.SyncVehicleImagesUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.UpdateListingUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.UpsertPricePlanUseCase;
import com.ares.car_rental_monolith.modules.location.api.AdministrativeUnitDirectory;
import com.ares.car_rental_monolith.modules.location.api.AdministrativeUnitRef;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.LoadVehiclePort;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.WriteVehiclePort;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class VehicleUpdateService implements
        UpdateListingUseCase,
        SyncVehicleImagesUseCase,
        SyncVehicleFeaturesUseCase,
        UpsertPricePlanUseCase {

    private final WriteVehiclePort writePort;
    private final LoadVehiclePort loadPort;
    private final AdministrativeUnitDirectory administrativeUnits;

    VehicleUpdateService(
            WriteVehiclePort writePort,
            LoadVehiclePort loadPort,
            AdministrativeUnitDirectory administrativeUnits
    ) {
        this.writePort = writePort;
        this.loadPort = loadPort;
        this.administrativeUnits = administrativeUnits;
    }

    @Override
    public VehicleDetail handle(UpdateListingCommand command) {
        ensureVehicleExists(command.vehicleId());
        UpdateListingCommand resolved = validateAndResolveLocation(command);
        writePort.updateListingDraft(resolved);
        return reloadDetail(command.vehicleId());
    }

    // Validates that the province/commune codes refer to ACTIVE catalog units in
    // the correct hierarchy, and attaches their display names to the command.
    private UpdateListingCommand validateAndResolveLocation(UpdateListingCommand command) {
        AdministrativeUnitRef province = administrativeUnits.findActiveByCode(command.provinceCode())
                .orElseThrow(() -> DomainException.validation(
                        "provinceCode does not exist or is not active: " + command.provinceCode()));
        if (!"PROVINCE".equals(province.level())) {
            throw DomainException.validation("provinceCode is not a province: " + command.provinceCode());
        }

        AdministrativeUnitRef commune = administrativeUnits.findActiveByCode(command.communeCode())
                .orElseThrow(() -> DomainException.validation(
                        "communeCode does not exist or is not active: " + command.communeCode()));
        if (!"COMMUNE".equals(commune.level())) {
            throw DomainException.validation("communeCode is not a commune: " + command.communeCode());
        }
        if (!province.code().equals(commune.parentCode())) {
            throw DomainException.validation(
                    "communeCode " + command.communeCode()
                            + " does not belong to province " + command.provinceCode());
        }

        return command.withResolvedLocation(province.name(), commune.name());
    }

    @Override
    public VehicleDetail handle(SyncVehicleImagesCommand command) {
        ensureVehicleExists(command.vehicleId());
        writePort.replaceImages(command);
        return reloadDetail(command.vehicleId());
    }

    @Override
    public VehicleDetail handle(SyncVehicleFeaturesCommand command) {
        ensureVehicleExists(command.vehicleId());
        writePort.replaceFeatures(command);
        return reloadDetail(command.vehicleId());
    }

    @Override
    public VehicleDetail handle(UpsertPricePlanCommand command) {
        ensureVehicleExists(command.vehicleId());
        writePort.upsertActivePricePlan(command);
        return reloadDetail(command.vehicleId());
    }

    private void ensureVehicleExists(UUID vehicleId) {
        writePort.findVehicle(vehicleId)
                .orElseThrow(() -> DomainException.notFound("Vehicle not found: " + vehicleId));
    }

    private VehicleDetail reloadDetail(UUID vehicleId) {
        return loadPort.loadVehicleDetail(vehicleId)
                .orElseThrow(() -> DomainException.notFound("Vehicle not found: " + vehicleId));
    }
}
