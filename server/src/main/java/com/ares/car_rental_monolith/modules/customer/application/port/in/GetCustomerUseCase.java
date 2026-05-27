package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;

import java.util.UUID;

@FunctionalInterface
public interface GetCustomerUseCase {
    CustomerDetail handle(UUID customerId);
}
