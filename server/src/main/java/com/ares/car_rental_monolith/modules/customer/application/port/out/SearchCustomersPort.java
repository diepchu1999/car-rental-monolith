package com.ares.car_rental_monolith.modules.customer.application.port.out;

import com.ares.car_rental_monolith.modules.customer.application.query.SearchCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerSummary;
import com.ares.car_rental_monolith.shared.api.PageResponse;

public interface SearchCustomersPort {
    PageResponse<CustomerSummary> search(SearchCustomersQuery query);
}
