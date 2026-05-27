package com.ares.car_rental_monolith.modules.customer.application.port.out;

import com.ares.car_rental_monolith.modules.customer.application.view.CustomerStats;

public interface LoadCustomerStatsPort {

    CustomerStats loadStats();
}
