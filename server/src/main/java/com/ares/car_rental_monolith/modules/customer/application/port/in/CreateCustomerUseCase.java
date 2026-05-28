package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.command.CreateCustomerCommand;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;

@FunctionalInterface
public interface CreateCustomerUseCase {

    CustomerDetail handle(CreateCustomerCommand command);
}
