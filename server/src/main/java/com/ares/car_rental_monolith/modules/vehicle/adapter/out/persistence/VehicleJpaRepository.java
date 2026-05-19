package com.ares.car_rental_monolith.modules.vehicle.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface VehicleJpaRepository
        extends JpaRepository<VehicleJpaEntity, UUID>,
                JpaSpecificationExecutor<VehicleJpaEntity> {

    // PostgreSQL ILIKE — index-friendly (no LOWER() call), enables pg_trgm GIN indexes
    // for trigram search at scale. Source/status compared as VARCHAR to match the
    // @Enumerated(EnumType.STRING) column representation.
    @Query(
        value = """
            SELECT v.*
            FROM vehicle.vehicles v
            WHERE (:q = '' OR v.brand ILIKE CONCAT('%', :q, '%')
                OR v.model ILIKE CONCAT('%', :q, '%')
                OR v.version ILIKE CONCAT('%', :q, '%')
                OR v.license_plate ILIKE CONCAT('%', :q, '%'))
            AND (:source IS NULL OR v.source = :source)
            AND (:status IS NULL OR v.status = :status)
            ORDER BY v.created_at DESC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM vehicle.vehicles v
            WHERE (:q = '' OR v.brand ILIKE CONCAT('%', :q, '%')
                OR v.model ILIKE CONCAT('%', :q, '%')
                OR v.version ILIKE CONCAT('%', :q, '%')
                OR v.license_plate ILIKE CONCAT('%', :q, '%'))
            AND (:source IS NULL OR v.source = :source)
            AND (:status IS NULL OR v.status = :status)
            """,
        nativeQuery = true
    )
    Page<VehicleJpaEntity> search(
            @Param("q") String q,
            @Param("source") String source,
            @Param("status") String status,
            Pageable pageable
    );
}
