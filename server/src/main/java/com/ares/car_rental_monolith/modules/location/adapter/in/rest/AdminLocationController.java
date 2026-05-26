package com.ares.car_rental_monolith.modules.location.adapter.in.rest;

import com.ares.car_rental_monolith.modules.location.adapter.in.rest.response.AdministrativeUnitResponse;
import com.ares.car_rental_monolith.modules.location.application.port.in.ListCommunesUseCase;
import com.ares.car_rental_monolith.modules.location.application.port.in.ListProvincesUseCase;
import com.ares.car_rental_monolith.modules.location.application.port.in.SearchAdministrativeUnitsUseCase;
import com.ares.car_rental_monolith.modules.location.domain.AdministrativeUnit;
import com.ares.car_rental_monolith.shared.api.ApiResponse;
import com.ares.car_rental_monolith.shared.api.ListResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/locations")
public class AdminLocationController {

    private final ListProvincesUseCase listProvinces;
    private final ListCommunesUseCase listCommunes;
    private final SearchAdministrativeUnitsUseCase searchUnits;

    public AdminLocationController(
            ListProvincesUseCase listProvinces,
            ListCommunesUseCase listCommunes,
            SearchAdministrativeUnitsUseCase searchUnits
    ) {
        this.listProvinces = listProvinces;
        this.listCommunes = listCommunes;
        this.searchUnits = searchUnits;
    }

    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<ListResponse<AdministrativeUnitResponse>>> provinces() {
        return ResponseEntity.ok(ApiResponse.success(
                "PROVINCES_LISTED", "Provinces listed", toResponse(listProvinces.handle())));
    }

    @GetMapping("/communes")
    public ResponseEntity<ApiResponse<ListResponse<AdministrativeUnitResponse>>> communes(
            @RequestParam String provinceCode
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "COMMUNES_LISTED", "Communes listed",
                toResponse(listCommunes.handle(provinceCode))));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ListResponse<AdministrativeUnitResponse>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String provinceCode
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "ADMINISTRATIVE_UNITS_SEARCHED", "Administrative units searched",
                toResponse(searchUnits.handle(q, level, provinceCode))));
    }

    private static ListResponse<AdministrativeUnitResponse> toResponse(List<AdministrativeUnit> units) {
        return ListResponse.of(units.stream().map(AdministrativeUnitResponse::from).toList());
    }
}
