package com.ares.car_rental_monolith.modules.vehicle.application.command;

import com.ares.car_rental_monolith.modules.vehicle.application.query.Enums;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleFuelType;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleSource;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleTransmission;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;

public record CreateVehicleCommand(
        VehicleSource source,
        UUID ownerCustomerId,
        String assetCode,
        UUID branchId,
        String brand,
        String model,
        String version,
        int manufactureYear,
        String licensePlate,
        int seats,
        VehicleTransmission transmission,
        VehicleFuelType fuelType
) {

    public static CreateVehicleCommand from(
            String source,
            UUID ownerCustomerId,
            String assetCode,
            UUID branchId,
            String brand,
            String model,
            String version,
            Integer manufactureYear,
            String licensePlate,
            Integer seats,
            String transmission,
            String fuelType
    ) {
        VehicleSource parsedSource = require(
                Enums.parseStrict(VehicleSource.class, "source", source), "source");
        VehicleTransmission parsedTransmission = require(
                Enums.parseStrict(VehicleTransmission.class, "transmission", transmission), "transmission");
        VehicleFuelType parsedFuelType = require(
                Enums.parseStrict(VehicleFuelType.class, "fuelType", fuelType), "fuelType");

        requireText(brand, "brand");
        requireText(model, "model");
        requireText(licensePlate, "licensePlate");
        requirePositive(manufactureYear, "manufactureYear");
        requirePositive(seats, "seats");

        // Source-specific contract: HOST_OWNED references an existing customer;
        // COMPANY_OWNED provisions a brand-new fleet asset (assetCode), so it does
        // NOT take a pre-existing fleetVehicleId — the fleet record is created during
        // the same transaction. Catching the wrong combination here produces a 400
        // with a clear message instead of a downstream null-deref.
        switch (parsedSource) {
            case HOST_OWNED -> {
                if (ownerCustomerId == null) {
                    throw DomainException.validation(
                            "ownerCustomerId is required for HOST_OWNED vehicles");
                }
                if (assetCode != null && !assetCode.isBlank()) {
                    throw DomainException.validation(
                            "assetCode must be null for HOST_OWNED vehicles");
                }
            }
            case COMPANY_OWNED -> {
                if (assetCode == null || assetCode.isBlank()) {
                    throw DomainException.validation(
                            "assetCode is required for COMPANY_OWNED vehicles");
                }
                if (ownerCustomerId != null) {
                    throw DomainException.validation(
                            "ownerCustomerId must be null for COMPANY_OWNED vehicles");
                }
            }
        }

        return new CreateVehicleCommand(
                parsedSource, ownerCustomerId,
                assetCode == null || assetCode.isBlank() ? null : assetCode.trim(),
                branchId,
                brand.trim(), model.trim(),
                version == null || version.isBlank() ? null : version.trim(),
                manufactureYear, licensePlate.trim(), seats,
                parsedTransmission, parsedFuelType
        );
    }

    private static <T> T require(T value, String field) {
        if (value == null) throw DomainException.validation(field + " is required");
        return value;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw DomainException.validation(field + " is required");
        }
    }

    private static void requirePositive(Integer value, String field) {
        if (value == null || value <= 0) {
            throw DomainException.validation(field + " must be > 0");
        }
    }
}
