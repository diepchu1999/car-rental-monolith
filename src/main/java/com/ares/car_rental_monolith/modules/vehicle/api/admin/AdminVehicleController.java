package com.ares.car_rental_monolith.modules.vehicle.api.admin;

import com.ares.car_rental_monolith.common.api.ApiResponse;
import com.ares.car_rental_monolith.common.api.ListResponse;
import com.ares.car_rental_monolith.modules.vehicle.application.dto.VehicleResponse;
import com.ares.car_rental_monolith.modules.vehicle.application.usecase.ListVehiclesUseCase;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/vehicles")
public class AdminVehicleController {

    private final ListVehiclesUseCase listVehiclesUseCase;

    public AdminVehicleController(ListVehiclesUseCase listVehiclesUseCase) {
        this.listVehiclesUseCase = listVehiclesUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ListResponse<VehicleResponse>>> listVehicles(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status
    ) {
        List<VehicleResponse> vehicles = listVehiclesUseCase.listVehicles(source, status);
        return ResponseEntity.ok(ApiResponse.success(
                "VEHICLE_LIST_FETCHED",
                "Vehicle list fetched successfully",
                ListResponse.of(vehicles)
        ));
    }
}
