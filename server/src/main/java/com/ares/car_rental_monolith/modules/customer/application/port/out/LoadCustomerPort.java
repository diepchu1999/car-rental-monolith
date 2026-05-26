package com.ares.car_rental_monolith.modules.customer.application.port.out;

import java.util.UUID;

public interface LoadCustomerPort {
    boolean isActiveCustomer(UUID customerId);
}
