package com.ares.car_rental_monolith.modules.vehicle.application.service;

import com.ares.car_rental_monolith.modules.vehicle.application.command.ChangeListingStatusCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.ChangeVehicleStatusCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.CreateVehicleCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.ChangeListingStatusUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.ChangeVehicleStatusUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.CreateVehicleUseCase;
import com.ares.car_rental_monolith.modules.customer.api.CustomerDirectory;
import com.ares.car_rental_monolith.modules.fleet.api.FleetVehicleProvisioning;
import com.ares.car_rental_monolith.modules.fleet.api.RegisterCompanyVehicleCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.LoadVehiclePort;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.WriteVehiclePort;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleListing;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatus;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class VehicleCommandService
        implements ChangeVehicleStatusUseCase, ChangeListingStatusUseCase, CreateVehicleUseCase {

    private final WriteVehiclePort writePort;
    private final LoadVehiclePort loadPort;
    private final FleetVehicleProvisioning fleetProvisioning;
    private final CustomerDirectory customerDirectory;

    VehicleCommandService(
            WriteVehiclePort writePort,
            LoadVehiclePort loadPort,
            FleetVehicleProvisioning fleetProvisioning,
            CustomerDirectory customerDirectory
    ) {
        this.writePort = writePort;
        this.loadPort = loadPort;
        this.fleetProvisioning = fleetProvisioning;
        this.customerDirectory = customerDirectory;
    }

    @Override
    public VehicleDetail handle(ChangeVehicleStatusCommand command) {
        Vehicle vehicle = writePort.findVehicle(command.vehicleId())
                .orElseThrow(() -> DomainException.notFound("Vehicle not found: " + command.vehicleId()));
        Vehicle updated = vehicle.apply(command.action());
        writePort.saveVehicleStatus(updated);
        return reloadDetail(command.vehicleId());
    }

    @Override
    public VehicleDetail handle(ChangeListingStatusCommand command) {
        VehicleListing listing = writePort.findListingByVehicleId(command.vehicleId())
                .orElseThrow(() -> DomainException.notFound(
                        "Listing not found for vehicle: " + command.vehicleId()));
        int imageCount = writePort.countListingImages(command.vehicleId());
        VehicleListing updated = listing.apply(
                command.action(),
                new VehicleListing.PublishContext(imageCount));
        writePort.saveListingStatus(updated);
        return reloadDetail(command.vehicleId());
    }

    @Override
    public VehicleDetail handle(CreateVehicleCommand command) {
        // Cross-aggregate checks happen here (in the service) — the Vehicle aggregate
        // doesn't know about customers or fleet vehicles, so the service is the right
        // layer to coordinate.
        if (writePort.licensePlateExists(command.licensePlate())) {
            throw DomainException.conflict(
                    "License plate already exists: " + command.licensePlate());
        }

        UUID vehicleId = UUID.randomUUID();
        UUID fleetVehicleId = null;
        switch (command.source()) {
            case HOST_OWNED -> {
                // Phải là active HOST, không chỉ active customer: renter thuần không
                // được sở hữu xe. Chốt ở đây vì là ràng buộc cross-aggregate (vehicle
                // không biết về host) — đi qua public port của customer module.
                if (!customerDirectory.isActiveHost(command.ownerCustomerId())) {
                    throw DomainException.validation(
                            "ownerCustomerId must reference an active host");
                }
            }
            case COMPANY_OWNED ->
                // Provision the fleet asset via the fleet module's public port, then
                // link it. The new company-vehicle row points back to vehicleId (saved
                // just below) — both rows commit in this one transaction, so the mutual
                // reference is never left dangling.
                fleetVehicleId = fleetProvisioning.registerCompanyVehicle(
                        new RegisterCompanyVehicleCommand(
                                vehicleId, command.assetCode(), command.branchId()));
        }

        OffsetDateTime now = OffsetDateTime.now();
        Vehicle vehicle = new Vehicle(
                vehicleId,
                command.ownerCustomerId(),
                fleetVehicleId,
                command.source(),
                command.brand(), command.model(), command.version(),
                command.manufactureYear(), command.licensePlate(), command.seats(),
                command.transmission(), command.fuelType(),
                VehicleStatus.DRAFT,
                now, now
        );
        Vehicle saved = writePort.createVehicle(vehicle);
        return reloadDetail(saved.id());
    }

    private VehicleDetail reloadDetail(UUID vehicleId) {
        return loadPort.loadVehicleDetail(vehicleId)
                .orElseThrow(() -> DomainException.notFound("Vehicle not found: " + vehicleId));
    }
}
