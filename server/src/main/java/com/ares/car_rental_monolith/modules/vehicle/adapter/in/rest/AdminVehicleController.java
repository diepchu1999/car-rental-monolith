package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest;

import com.ares.car_rental_monolith.modules.vehicle.application.port.in.GetVehicleUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.ListVehiclesUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.PageVehiclesUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.query.ListVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.shared.api.ApiResponse;
import com.ares.car_rental_monolith.shared.api.ListResponse;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/vehicles")
public class AdminVehicleController {

    private final ListVehiclesUseCase listVehicles;
    private final PageVehiclesUseCase pageVehicles;
    private final GetVehicleUseCase getVehicle;

    public AdminVehicleController(
            ListVehiclesUseCase listVehicles,
            PageVehiclesUseCase pageVehicles,
            GetVehicleUseCase getVehicle
    ) {
        this.listVehicles = listVehicles;
        this.pageVehicles = pageVehicles;
        this.getVehicle = getVehicle;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ListResponse<VehicleResponse>>> list(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status
    ) {
        List<Vehicle> vehicles = listVehicles.handle(ListVehiclesQuery.from(source, status));
        List<VehicleResponse> body = vehicles.stream().map(VehicleApiMapper::toResponse).toList();

        return ResponseEntity.ok(ApiResponse.success(
                "VEHICLE_LIST_FETCHED",
                "Vehicle list fetched successfully",
                ListResponse.of(body)
        ));
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> page(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PageResponse<Vehicle> result =
                pageVehicles.handle(PageVehiclesQuery.from(q, source, status, page, size));

        return ResponseEntity.ok(ApiResponse.success(
                "VEHICLE_PAGE_FETCHED",
                "Vehicle page fetched successfully",
                result.map(VehicleApiMapper::toResponse)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getById(@PathVariable UUID id) {
        VehicleDetail detail = getVehicle.handle(id);

        return ResponseEntity.ok(ApiResponse.success(
                "VEHICLE_FETCHED",
                "Vehicle fetched successfully",
                VehicleApiMapper.toResponse(detail)
        ));
    }
}
