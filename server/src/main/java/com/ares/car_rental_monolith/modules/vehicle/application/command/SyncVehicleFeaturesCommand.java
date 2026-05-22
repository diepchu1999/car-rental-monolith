package com.ares.car_rental_monolith.modules.vehicle.application.command;

import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record SyncVehicleFeaturesCommand(UUID vehicleId, List<FeatureInput> features) {

    public record FeatureInput(String code, String name) {}

    public static SyncVehicleFeaturesCommand from(UUID vehicleId, List<FeatureInput> features) {
        if (vehicleId == null) throw DomainException.validation("vehicleId is required");
        if (features == null) throw DomainException.validation("features is required");
        Set<String> seenCodes = new HashSet<>();
        for (FeatureInput f : features) {
            if (f.code() == null || f.code().isBlank()) {
                throw DomainException.validation("feature.code is required");
            }
            if (f.name() == null || f.name().isBlank()) {
                throw DomainException.validation("feature.name is required");
            }
            if (!seenCodes.add(f.code())) {
                throw DomainException.validation("Duplicate feature code: " + f.code());
            }
        }
        return new SyncVehicleFeaturesCommand(vehicleId, List.copyOf(features));
    }
}
