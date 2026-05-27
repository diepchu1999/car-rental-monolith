package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.view.CustomerStats;

@FunctionalInterface
public interface GetCustomerStatsUseCase {

    CustomerStats handle();
}
