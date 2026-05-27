package com.ares.car_rental_monolith.modules.customer.application.port.out;

import com.ares.car_rental_monolith.modules.customer.application.view.CustomerDetail;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface WriteCustomerPort {

    // Tạo customer mới (customer + roles + tùy chọn host/KYC/address mặc định)
    // trong cùng 1 transaction. KYC ở create là tùy chọn vì mô hình multi-KYC
    // cho phép customer được tạo "rỗng" và nộp KYC sau.
    void create(CustomerDetail customer);

    void saveCustomerStatus(CustomerDetail customer);

    void saveHostStatus(CustomerDetail customer);

    // Cập nhật thông tin cá nhân (KHÔNG đụng KYC / host / address / status).
    // Phục vụ PATCH /admin/customers/{id}.
    void saveCustomerBasics(CustomerDetail customer);

    // Approve / reject 1 hồ sơ KYC. reviewedBy đến từ context auth (admin user
    // id); rejectionReason chỉ truyền khi reject (chuẩn hóa null khi approve).
    // Trả về số row update để service phân biệt "không thuộc customer" vs OK.
    int approveKyc(UUID kycId, UUID reviewedBy, OffsetDateTime now);

    int rejectKyc(UUID kycId, UUID reviewedBy, String rejectionReason, OffsetDateTime now);

    // Tạo 1 hồ sơ KYC mới (kyc_profiles + kyc_documents) trong cùng transaction
    // của service gọi vào. Documents đã có fileUrl public do storage adapter
    // sinh ra — adapter chỉ lưu y nguyên, không xử lý file.
    void createKyc(UUID customerId, CustomerDetail.Kyc kyc);
}
