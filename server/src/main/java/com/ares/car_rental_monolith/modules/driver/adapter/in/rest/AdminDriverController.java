package com.ares.car_rental_monolith.modules.driver.adapter.in.rest;

import com.ares.car_rental_monolith.modules.driver.application.port.in.GetDriverUseCase;
import com.ares.car_rental_monolith.modules.driver.application.port.in.SearchDriversUseCase;
import com.ares.car_rental_monolith.modules.driver.application.query.SearchDriversQuery;
import com.ares.car_rental_monolith.modules.driver.domain.DriverSummary;
import com.ares.car_rental_monolith.shared.api.ApiResponse;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/drivers")
public class AdminDriverController {

    private final SearchDriversUseCase searchDrivers;
    private final GetDriverUseCase getDriver;

    public AdminDriverController(SearchDriversUseCase searchDrivers, GetDriverUseCase getDriver) {
        this.searchDrivers = searchDrivers;
        this.getDriver = getDriver;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DriverSummary>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PageResponse<DriverSummary> result = searchDrivers
                .handle(SearchDriversQuery.from(q, status, page, size));
        return ResponseEntity.ok(ApiResponse.success(
                "DRIVERS_SEARCHED", "Drivers searched", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverSummary>> getById(@PathVariable UUID id) {
        DriverSummary driver = getDriver.handle(id);
        return ResponseEntity.ok(ApiResponse.success(
                "DRIVER_FETCHED", "Driver fetched", driver));
    }
}
