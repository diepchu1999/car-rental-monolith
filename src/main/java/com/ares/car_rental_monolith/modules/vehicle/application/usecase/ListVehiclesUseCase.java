package com.ares.car_rental_monolith.modules.vehicle.application.usecase;

import com.ares.car_rental_monolith.modules.vehicle.application.dto.VehicleResponse;
import com.ares.car_rental_monolith.modules.vehicle.application.port.out.VehicleQueryPort;
import com.ares.car_rental_monolith.modules.vehicle.application.query.VehicleSearchCriteria;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListVehiclesUseCase {

    private final VehicleQueryPort vehicleQueryPort;

    public ListVehiclesUseCase(VehicleQueryPort vehicleQueryPort) {
        this.vehicleQueryPort = vehicleQueryPort;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> listVehicles(String source, String status) {
        return vehicleQueryPort.findVehicles(VehicleSearchCriteria.from(source, status));
    }
}
