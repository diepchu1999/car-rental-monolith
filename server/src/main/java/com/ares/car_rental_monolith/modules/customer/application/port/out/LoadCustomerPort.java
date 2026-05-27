package com.ares.car_rental_monolith.modules.customer.application.port.out;

import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;

import java.util.Optional;
import java.util.UUID;

public interface LoadCustomerPort {
    boolean isActiveCustomer(UUID customerId);

    // Tồn tại bất kể status (ACTIVE/PENDING_KYC/SUSPENDED...). Dùng cho luồng
    // tạo KYC: customer mới đăng ký có thể chưa ACTIVE nhưng vẫn cần được nộp KYC.
    boolean existsCustomer(UUID customerId);

    Optional<CustomerDetail> loadCustomerDetail(UUID id);

    // Trả về KYC chỉ khi nó thực sự thuộc customerId truyền vào, dùng cho luồng
    // review/approve/reject. Service tự throw notFound nếu rỗng — adapter không
    // ném exception, đảm bảo port pure-data.
    Optional<CustomerDetail.Kyc> loadKycForCustomer(UUID customerId, UUID kycId);
}
