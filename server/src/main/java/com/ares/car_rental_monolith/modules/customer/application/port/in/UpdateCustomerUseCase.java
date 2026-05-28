package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.command.UpdateCustomerCommand;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;

@FunctionalInterface
public interface UpdateCustomerUseCase {

    CustomerDetail handle(UpdateCustomerCommand command);
}
