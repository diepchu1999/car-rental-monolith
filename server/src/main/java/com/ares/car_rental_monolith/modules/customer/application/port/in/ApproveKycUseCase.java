package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.command.ApproveKycCommand;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;

@FunctionalInterface
public interface ApproveKycUseCase {

    // Trả về CustomerDetail mới (đã reload) để controller trả về full customer
    // — FE đồng bộ badge ở list/detail/popup trong cùng response.
    CustomerDetail handle(ApproveKycCommand command);
}
