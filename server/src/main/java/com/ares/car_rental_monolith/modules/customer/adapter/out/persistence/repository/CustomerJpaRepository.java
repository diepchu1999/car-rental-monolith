package com.ares.car_rental_monolith.modules.customer.adapter.out.persistence.repository;

import com.ares.car_rental_monolith.modules.customer.adapter.out.persistence.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID>,
        JpaSpecificationExecutor<CustomerJpaEntity> {
}
