package com.ares.car_rental_monolith.modules.customer.application.port.out;

import com.ares.car_rental_monolith.modules.customer.application.query.ListCustomersQuery;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import com.ares.car_rental_monolith.shared.api.PageResponse;

public interface PageCustomersPort {

    PageResponse<CustomerDetail> page(ListCustomersQuery query);
}
