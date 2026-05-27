package com.ares.car_rental_monolith.modules.customer.application.port.in;

import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import java.util.UUID;

@FunctionalInterface
public interface GetKycDetailUseCase {

    // Trả về 1 KYC kèm documents. Ném notFound nếu kyc không tồn tại hoặc
    // không thuộc customer (chống IDOR).
    CustomerDetail.Kyc handle(UUID customerId, UUID kycId);
}
