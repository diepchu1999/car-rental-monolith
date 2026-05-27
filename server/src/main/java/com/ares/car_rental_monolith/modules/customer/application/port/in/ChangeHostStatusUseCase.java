package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.command.ChangeHostStatusCommand;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;

@FunctionalInterface
public interface ChangeHostStatusUseCase {

    CustomerDetail handle(ChangeHostStatusCommand command);
}
