package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.command.RejectKycCommand;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;

@FunctionalInterface
public interface RejectKycUseCase {

    CustomerDetail handle(RejectKycCommand command);
}
