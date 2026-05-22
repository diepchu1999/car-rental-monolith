package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.query.SearchCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.domain.CustomerSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;

@FunctionalInterface
public interface SearchCustomersUseCase {
    PageResponse<CustomerSummary> handle(SearchCustomersQuery query);
}
