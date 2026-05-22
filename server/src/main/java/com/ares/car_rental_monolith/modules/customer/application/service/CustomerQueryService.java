package com.ares.car_rental_monolith.modules.customer.application.service;

import com.ares.car_rental_monolith.modules.customer.application.port.in.SearchCustomersUseCase;
import com.ares.car_rental_monolith.modules.customer.application.port.out.SearchCustomersPort;
import com.ares.car_rental_monolith.modules.customer.application.query.SearchCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.domain.CustomerSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class CustomerQueryService implements SearchCustomersUseCase {

    private final SearchCustomersPort port;

    CustomerQueryService(SearchCustomersPort port) {
        this.port = port;
    }

    @Override
    public PageResponse<CustomerSummary> handle(SearchCustomersQuery query) {
        return port.search(query);
    }
}
