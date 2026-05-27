package com.ares.car_rental_monolith.modules.customer.application.service;

import com.ares.car_rental_monolith.modules.customer.application.port.in.GetCustomerStatsUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.GetCustomerUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.ListCustomersUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.in.SearchCustomersUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.out.LoadCustomerPort;
import com.ares.car_rental_monolith.modules.customer.application.port.out.LoadCustomerStatsPort;
import com.ares.car_rental_monolith.modules.customer.application.port.out.PageCustomersPort;
import com.ares.car_rental_monolith.modules.customer.application.port.out.SearchCustomersPort;
import com.ares.car_rental_monolith.modules.customer.application.query.ListCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.application.query.SearchCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerStats;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.error.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
class CustomerQueryService
        implements SearchCustomersUseCase, GetCustomerUseCase, ListCustomersUseCase,
        GetCustomerStatsUseCase {

    private final SearchCustomersPort searchPort;
    private final LoadCustomerPort loadPort;
    private final PageCustomersPort pagePort;
    private final LoadCustomerStatsPort statsPort;

    CustomerQueryService(
            SearchCustomersPort searchPort,
            LoadCustomerPort loadPort,
            PageCustomersPort pagePort,
            LoadCustomerStatsPort statsPort
    ) {
        this.searchPort = searchPort;
        this.loadPort = loadPort;
        this.pagePort = pagePort;
        this.statsPort = statsPort;
    }

    @Override
    public PageResponse<CustomerSummary> handle(SearchCustomersQuery query) {
        return searchPort.search(query);
    }

    @Override
    public PageResponse<CustomerDetail> handle(ListCustomersQuery query) {
        return pagePort.page(query);
    }

    @Override
    public CustomerStats handle() {
        return statsPort.loadStats();
    }

    @Override
    public CustomerDetail handle(UUID customerId) {
        return loadPort.loadCustomerDetail(customerId).orElseThrow(() -> DomainException.notFound(
                "Customer not found: " + customerId));
    }
}
