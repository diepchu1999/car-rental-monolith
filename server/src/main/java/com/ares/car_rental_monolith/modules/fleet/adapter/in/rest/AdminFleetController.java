package com.ares.car_rental_monolith.modules.fleet.adapter.in.rest;

import com.ares.car_rental_monolith.modules.fleet.adapter.in.rest.response.AdminFleetBranchDetailResponse;
import com.ares.car_rental_monolith.modules.fleet.application.port.in.GetFleetBranchUseCase;
import com.ares.car_rental_monolith.modules.fleet.application.port.in.ListBranchesUseCase;
import com.ares.car_rental_monolith.modules.fleet.application.port.in.ListFleetBranchesUseCase;
import com.ares.car_rental_monolith.modules.fleet.application.port.in.SearchFleetVehiclesUseCase;
import com.ares.car_rental_monolith.modules.fleet.application.query.ListFleetBranchesQuery;
import com.ares.car_rental_monolith.modules.fleet.application.query.SearchFleetVehiclesQuery;
import com.ares.car_rental_monolith.modules.fleet.domain.BranchSummary;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;
import com.ares.car_rental_monolith.modules.fleet.domain.FleetVehicleSummary;
import com.ares.car_rental_monolith.shared.api.ApiResponse;
import com.ares.car_rental_monolith.shared.api.ListResponse;
import com.ares.car_rental_monolith.shared.api.PageResponse;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/fleet")
public class AdminFleetController {

    private final SearchFleetVehiclesUseCase searchFleetVehicles;
    private final ListBranchesUseCase listBranches;
    private final ListFleetBranchesUseCase listFleetBranches;
    private final GetFleetBranchUseCase getFleetBranch;

    public AdminFleetController(
            SearchFleetVehiclesUseCase searchFleetVehicles,
            ListBranchesUseCase listBranches,
            ListFleetBranchesUseCase listFleetBranches,
            GetFleetBranchUseCase getFleetBranch
    ) {
        this.searchFleetVehicles = searchFleetVehicles;
        this.listBranches = listBranches;
        this.listFleetBranches = listFleetBranches;
        this.getFleetBranch = getFleetBranch;
    }

    @GetMapping("/vehicles/search")
    public ResponseEntity<ApiResponse<PageResponse<FleetVehicleSummary>>> searchFleetVehicles(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PageResponse<FleetVehicleSummary> result = searchFleetVehicles
                .handle(SearchFleetVehiclesQuery.from(q, branchId, page, size));
        return ResponseEntity.ok(ApiResponse.success(
                "FLEET_VEHICLES_SEARCHED", "Fleet vehicles searched", result));
    }

    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<ListResponse<BranchSummary>>> branches() {
        List<BranchSummary> branches = listBranches.handle();
        return ResponseEntity.ok(ApiResponse.success(
                "BRANCHES_LISTED", "Branches listed", ListResponse.of(branches)));
    }


    @GetMapping("/branches/paged")
    public ResponseEntity<ApiResponse<PageResponse<AdminFleetBranchDetailResponse>>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size

    ) {
        PageResponse<AdminFleetBranchDetailResponse> result = listFleetBranches.handle(
                ListFleetBranchesQuery.from(q, status, page, size)

        ).map(AdminFleetBranchDetailResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(
                "FLEET_BRANCH_LISTED", "Branch Fleet listed", result));
    }

    @GetMapping("/branches/{id}")
    public ResponseEntity<ApiResponse<AdminFleetBranchDetailResponse>> getByFleetId(
            @PathVariable UUID id
    ) {
        FleetBranchDetail fleetBranchDetail = getFleetBranch.handle(id);
        return ResponseEntity.ok(ApiResponse.success(
                "BRANCH_FETCH",
                "Branch fetch successfully",
                AdminFleetBranchDetailResponse.fromDomain(fleetBranchDetail)
        ));
    }
}
