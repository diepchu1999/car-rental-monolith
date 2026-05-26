package com.ares.car_rental_monolith.modules.vehicle.application.port.out;

import com.ares.car_rental_monolith.modules.vehicle.application.command.SyncVehicleFeaturesCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.SyncVehicleImagesCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.UpdateListingCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.UpsertPricePlanCommand;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleListing;
import java.util.Optional;
import java.util.UUID;

public interface WriteVehiclePort {

    Optional<Vehicle> findVehicle(UUID vehicleId);

    Vehicle saveVehicleStatus(Vehicle vehicle);

    Optional<VehicleListing> findListingByVehicleId(UUID vehicleId);

    int countListingImages(UUID vehicleId);

    VehicleListing saveListingStatus(VehicleListing listing);

    boolean licensePlateExists(String licensePlate);

    Vehicle createVehicle(Vehicle vehicle);

    void updateListingDraft(UpdateListingCommand command);

    void replaceImages(SyncVehicleImagesCommand command);

    void replaceFeatures(SyncVehicleFeaturesCommand command);

    void upsertActivePricePlan(UpsertPricePlanCommand command);
}
