package com.ares.car_rental_monolith.modules.customer.adapter.in.rest;

import com.ares.car_rental_monolith.modules.customer.application.port.in.SearchCustomersUseCase;
import com.ares.car_rental_monolith.modules.customer.application.query.SearchCustomersQuery;
import com.ares.car_rental_monolith.shared.api.ApiResponse;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/customers")
public class AdminCustomerController {

    private final SearchCustomersUseCase searchCustomers;

    public AdminCustomerController(SearchCustomersUseCase searchCustomers) {
        this.searchCustomers = searchCustomers;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<CustomerSummaryResponse>>> search(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PageResponse<CustomerSummaryResponse> result = searchCustomers
                .handle(SearchCustomersQuery.from(q, page, size))
                .map(CustomerSummaryResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(
                "CUSTOMERS_SEARCHED", "Customers searched", result));
    }
}
