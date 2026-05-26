package com.ares.car_rental_monolith.modules.customer.api;

import java.util.UUID;

// Public, cross-module port of the customer module. Other modules depend on this
// interface (not on customer internals) for lightweight existence/eligibility checks.
public interface CustomerDirectory {

    // True when the customer exists and is in ACTIVE status.
    boolean isActiveCustomer(UUID customerId);
}
