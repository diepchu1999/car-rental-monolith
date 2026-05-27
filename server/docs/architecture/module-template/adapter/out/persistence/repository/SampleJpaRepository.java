package com.ares.car_rental_monolith.modules.sample.adapter.out.persistence.repository;

import com.ares.car_rental_monolith.modules.sample.adapter.out.persistence.entity.SampleJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository ở subpackage riêng -> phải PUBLIC, và import entity.
public interface SampleJpaRepository extends JpaRepository<SampleJpaEntity, UUID> {
}
