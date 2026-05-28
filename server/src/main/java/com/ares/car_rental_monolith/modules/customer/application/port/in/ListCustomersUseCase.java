package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.query.ListCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import com.ares.car_rental_monolith.shared.api.PageResponse;

@FunctionalInterface
public interface ListCustomersUseCase {

    PageResponse<CustomerDetail> handle(ListCustomersQuery query);
}
