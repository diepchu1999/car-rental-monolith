package com.ares.car_rental_monolith.modules.customer.api;

import java.util.UUID;

// Public, cross-module port of the customer module. Other modules depend on this
// interface (not on customer internals) for lightweight existence/eligibility checks.
public interface CustomerDirectory {

    // True when the customer exists and is in ACTIVE status.
    boolean isActiveCustomer(UUID customerId);

    // True when the customer is an ACTIVE host: customer ACTIVE + có host_profiles
    // với status ACTIVE. Dùng cho ràng buộc "chủ xe HOST_OWNED phải là host" — chỉ
    // active-customer là chưa đủ, renter thuần cũng active nhưng không được sở hữu xe.
    boolean isActiveHost(UUID customerId);
}
