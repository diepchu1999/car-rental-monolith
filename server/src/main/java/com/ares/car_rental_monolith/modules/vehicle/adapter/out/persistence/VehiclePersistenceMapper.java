package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;

final class VehiclePersistenceMapper {

    private VehiclePersistenceMapper() {}

    static Vehicle toDomain(VehicleJpaEntity e) {
        return new Vehicle(
                e.getId(),
                e.getOwnerCustomerId(),
                e.getFleetVehicleId(),
                e.getSource(),
                e.getBrand(),
                e.getModel(),
                e.getVersion(),
                e.getManufactureYear(),
                e.getLicensePlate(),
                e.getSeats(),
                e.getTransmission(),
                e.getFuelType(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
