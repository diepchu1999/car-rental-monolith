package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.command.ChangeCustomerStatusCommand;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;

@FunctionalInterface
public interface ChangeCustomerStatusUseCase {

    CustomerDetail handle(ChangeCustomerStatusCommand command);
}
