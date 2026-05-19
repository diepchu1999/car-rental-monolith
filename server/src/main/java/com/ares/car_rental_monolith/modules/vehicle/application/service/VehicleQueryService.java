package com.ares.car_rental_monolith.modules.vehicle.application.service;

import com.ares.car_rental_monolith.modules.vehicle.application.port.in.GetVehicleUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.ListVehiclesUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.PageVehiclesUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.LoadVehiclePort;
import com.ares.car_rental_monolith.modules.vehicle.application.query.ListVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class VehicleQueryService
        implements ListVehiclesUseCase, PageVehiclesUseCase, GetVehicleUseCase {

    private final LoadVehiclePort loadVehiclePort;

    VehicleQueryService(LoadVehiclePort loadVehiclePort) {
        this.loadVehiclePort = loadVehiclePort;
    }

    @Override
    public List<Vehicle> handle(ListVehiclesQuery query) {
        return loadVehiclePort.loadVehicles(query);
    }

    @Override
    public PageResponse<Vehicle> handle(PageVehiclesQuery query) {
        return loadVehiclePort.loadVehiclePage(query);
    }

    @Override
    public VehicleDetail handle(UUID vehicleId) {
        return loadVehiclePort.loadVehicleDetail(vehicleId)
                .orElseThrow(() -> DomainException.notFound(
                        "Vehicle not found: " + vehicleId));
    }
}
