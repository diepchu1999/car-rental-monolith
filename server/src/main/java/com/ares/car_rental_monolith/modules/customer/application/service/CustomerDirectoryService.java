package com.ares.car_rental_monolith.modules.customer.application.service;

import com.ares.car_rental_monolith.modules.customer.api.CustomerDirectory;
import com.ares.car_rental_monolith.modules.customer.application.port.out.LoadCustomerPort;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class CustomerDirectoryService implements CustomerDirectory {

    private final LoadCustomerPort port;

    CustomerDirectoryService(LoadCustomerPort port) {
        this.port = port;
    }

    @Override
    public boolean isActiveCustomer(UUID customerId) {
        if (customerId == null) return false;
        return port.isActiveCustomer(customerId);
    }
}
