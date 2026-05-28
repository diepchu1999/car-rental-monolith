package com.ares.car_rental_monolith.modules.customer.application.view;

import java.util.List;

// Trạng thái KYC tổng của customer, tính từ danh sách hồ sơ KYC hiện có.
// Tính trên-fly khi load detail/list — không denormalize sang bảng customers
// để tránh maintain sync. Index (customer_id, status) trên kyc_profiles đảm
// bảo aggregate đủ nhanh ở mức page size 20.
public enum KycAggregateStatus {

    NO_KYC,
    PENDING,
    PARTIALLY_APPROVED,
    FULLY_APPROVED,
    REJECTED;

    public static KycAggregateStatus from(List<CustomerDetail.Kyc> kycs) {
        if (kycs == null || kycs.isEmpty()) {
            return NO_KYC;
        }
        int approved = 0;
        int rejected = 0;
        int pending = 0;
        for (CustomerDetail.Kyc k : kycs) {
            switch (k.status()) {
                case "APPROVED" -> approved++;
                case "REJECTED" -> rejected++;
                default -> pending++; // PENDING / EXPIRED đều coi là "chưa duyệt"
            }
        }
        int total = kycs.size();
        if (approved == total) return FULLY_APPROVED;
        if (rejected == total) return REJECTED;
        if (approved > 0) return PARTIALLY_APPROVED;
        if (pending > 0) return PENDING;
        // Tất cả rejected đã catch ở trên; còn lại là edge case lẫn rejected + pending=0.
        return REJECTED;
    }
}
