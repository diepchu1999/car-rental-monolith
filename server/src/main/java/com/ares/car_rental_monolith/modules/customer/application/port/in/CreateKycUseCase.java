package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.command.CreateKycCommand;
import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;

@FunctionalInterface
public interface CreateKycUseCase {

    // Trả về hồ sơ KYC vừa tạo (đã reload kèm documents) để controller trả ra
    // KycResponse — FE refetch customer detail sau khi nhận để cập nhật list.
    CustomerDetail.Kyc handle(CreateKycCommand command);
}
