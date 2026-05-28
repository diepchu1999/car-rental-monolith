package com.ares.car_rental_monolith.modules.sample.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Entity ở subpackage riêng -> phải PUBLIC. No-arg constructor để PUBLIC (mặc định
// của @NoArgsConstructor) vì adapter ở package persistence/ gọi `new ...()`. Nếu
// chỉ ghi bằng native SQL (không `new` entity) thì có thể để protected.
@Entity
@Table(name = "samples", schema = "sample")
@Getter
@Setter
@NoArgsConstructor
public class SampleJpaEntity {

    @Id
    private UUID id;

    private String name;

    private String status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
