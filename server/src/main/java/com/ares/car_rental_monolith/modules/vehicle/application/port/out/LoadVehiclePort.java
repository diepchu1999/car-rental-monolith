package com.ares.car_rental_monolith.modules.vehicle.application.port.out;

import com.ares.car_rental_monolith.modules.vehicle.application.query.ListVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadVehiclePort {

    List<Vehicle> loadVehicles(ListVehiclesQuery query);

    PageResponse<Vehicle> loadVehiclePage(PageVehiclesQuery query);

    Optional<VehicleDetail> loadVehicleDetail(UUID vehicleId);
}
