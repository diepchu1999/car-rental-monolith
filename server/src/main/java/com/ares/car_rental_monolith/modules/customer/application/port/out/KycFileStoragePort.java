package com.ares.car_rental_monolith.modules.customer.application.port.out;

import java.util.UUID;

public interface KycFileStoragePort {

    // Lưu bytes của 1 file KYC dưới hierarchy {customerId}/{kycId}/ và trả về
    // public URL mà client có thể GET lại (vd "/media/kyc-documents/.../front-xxx.jpg").
    // Adapter local nắm cả base dir lẫn base URL — service không tự ghép path.
    String store(UUID customerId, UUID kycId, String side, byte[] content, String extension);
}
