package com.ares.car_rental_monolith.modules.customer.adapter.in.rest;

import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.request.ChangeCustomerStatusRequest;
import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.request.ChangeHostStatusRequest;
import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.request.CreateCustomerRequest;
import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.request.UpdateCustomerRequest;
import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.response.AdminCustomerDetailResponse;
import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.response.CustomerSummaryResponse;
import com.ares.car_rental_monolith.modules.customer.application.command.ChangeCustomerStatusCommand;
import com.ares.car_rental_monolith.modules.customer.application.command.ChangeHostStatusCommand;
import com.ares.car_rental_monolith.modules.customer.application.command.CreateCustomerCommand;
import com.ares.car_rental_monolith.modules.customer.application.command.UpdateCustomerCommand;
import com.ares.car_rental_monolith.modules.customer.application.port.in.ChangeCustomerStatusUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.ChangeHostStatusUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.CreateCustomerUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.GetCustomerStatsUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.GetCustomerUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.ListCustomersUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.SearchCustomersUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.UpdateCustomerUseCase;
import com.ares.car_rental_monolith.modules.customer.application.query.ListCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.application.query.SearchCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerStats;
import com.ares.car_rental_monolith.shared.api.ApiResponse;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {

    private final SearchCustomersUseCase searchCustomers;
    private final CreateCustomerUseCase createCustomer;
    private final GetCustomerUseCase getCustomer;
    private final ListCustomersUseCase listCustomers;
    private final GetCustomerStatsUseCase customerStats;
    private final ChangeCustomerStatusUseCase changeCustomerStatus;
    private final ChangeHostStatusUseCase changeHostStatus;
    private final UpdateCustomerUseCase updateCustomer;

    public AdminCustomerController(
            SearchCustomersUseCase searchCustomers,
            CreateCustomerUseCase createCustomer,
            GetCustomerUseCase getCustomer,
            ListCustomersUseCase listCustomers,
            GetCustomerStatsUseCase customerStats,
            ChangeCustomerStatusUseCase changeCustomerStatus,
            ChangeHostStatusUseCase changeHostStatus,
            UpdateCustomerUseCase updateCustomer
    ) {
        this.searchCustomers = searchCustomers;
        this.createCustomer = createCustomer;
        this.getCustomer = getCustomer;
        this.listCustomers = listCustomers;
        this.customerStats = customerStats;
        this.changeCustomerStatus = changeCustomerStatus;
        this.changeHostStatus = changeHostStatus;
        this.updateCustomer = updateCustomer;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<CustomerSummaryResponse>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            // hostOnly=true: chỉ trả active host (vehicle owner picker dùng). Mặc định
            // false để search chung vẫn trả cả renter lẫn host.
            @RequestParam(required = false) Boolean hostOnly
    ) {
        PageResponse<CustomerSummaryResponse> result = searchCustomers
                .handle(SearchCustomersQuery.from(q, page, size, hostOnly))
                .map(CustomerSummaryResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(
                "CUSTOMERS_SEARCHED", "Customers searched", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminCustomerDetailResponse>>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            // Aggregate KYC status: NO_KYC / PENDING / PARTIALLY_APPROVED /
            // FULLY_APPROVED / REJECTED.
            @RequestParam(required = false) String kyc,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PageResponse<AdminCustomerDetailResponse> result = listCustomers
                .handle(ListCustomersQuery.from(q, role, status, kyc, page, size))
                .map(AdminCustomerDetailResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(
                "CUSTOMERS_LISTED", "Customers listed", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminCustomerDetailResponse>> create(
            @RequestBody CreateCustomerRequest body
    ) {
        AdminCustomerDetailResponse created = AdminCustomerDetailResponse.fromDomain(
                createCustomer.handle(CreateCustomerCommand.from(body)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "CUSTOMER_CREATED", "Customer created", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminCustomerDetailResponse>> getById(@PathVariable UUID id) {
        CustomerDetail detail = getCustomer.handle(id);
        return ResponseEntity.ok(ApiResponse.success(
                "CUSTOMER_FETCHED",
                "Customer fetched successfully",
                AdminCustomerDetailResponse.fromDomain(detail)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminCustomerDetailResponse>> update(
            @PathVariable UUID id,
            @RequestBody UpdateCustomerRequest body
    ) {
        CustomerDetail detail = updateCustomer.handle(
                UpdateCustomerCommand.from(id, body));
        return ResponseEntity.ok(ApiResponse.success(
                "CUSTOMER_UPDATED", "Customer updated",
                AdminCustomerDetailResponse.fromDomain(detail)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<CustomerStats>> stats() {
        return ResponseEntity.ok(ApiResponse.success(
                "CUSTOMER_STATS", "Customer stats", customerStats.handle()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminCustomerDetailResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestBody ChangeCustomerStatusRequest body
    ) {
        CustomerDetail detail = changeCustomerStatus.handle(
                ChangeCustomerStatusCommand.from(id, body.action()));
        return ResponseEntity.ok(ApiResponse.success(
                "CUSTOMER_STATUS_UPDATED", "Customer status updated",
                AdminCustomerDetailResponse.fromDomain(detail)));
    }

    @PatchMapping("/{id}/host-status")
    public ResponseEntity<ApiResponse<AdminCustomerDetailResponse>> updateHostStatus(
            @PathVariable UUID id,
            @RequestBody ChangeHostStatusRequest body
    ) {
        CustomerDetail detail = changeHostStatus.handle(
                ChangeHostStatusCommand.from(id, body.action()));
        return ResponseEntity.ok(ApiResponse.success(
                "HOST_STATUS_UPDATED", "Host status updated",
                AdminCustomerDetailResponse.fromDomain(detail)));
    }
}
