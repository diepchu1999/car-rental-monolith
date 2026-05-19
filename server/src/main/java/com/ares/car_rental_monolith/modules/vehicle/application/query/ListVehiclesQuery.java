package com.ares.car_rental_monolith.modules.vehicle.application.query;

import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleSource;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatus;

public record ListVehiclesQuery(
        VehicleSource source,
        VehicleStatus status
) {
    public static ListVehiclesQuery from(String source, String status) {
        return new ListVehiclesQuery(
                Enums.parse(VehicleSource.class, source),
                Enums.parse(VehicleStatus.class, status)
        );
    }
}
