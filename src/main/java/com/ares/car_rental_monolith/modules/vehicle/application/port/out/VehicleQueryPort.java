package com.ares.car_rental_monolith.modules.vehicle.application.port.out;

import com.ares.car_rental_monolith.modules.vehicle.application.dto.VehicleResponse;
import com.ares.car_rental_monolith.modules.vehicle.application.query.VehicleSearchCriteria;
import java.util.List;

public interface VehicleQueryPort {

    List<VehicleResponse> findVehicles(VehicleSearchCriteria criteria);
}
