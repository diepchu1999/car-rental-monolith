package com.ares.car_rental_monolith.modules.sample.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.sample.adapter.out.persistence.entity.SampleJpaEntity;
import com.ares.car_rental_monolith.modules.sample.adapter.out.persistence.repository.SampleJpaRepository;
import com.ares.car_rental_monolith.modules.sample.application.port.out.WriteSamplePort;
import com.ares.car_rental_monolith.modules.sample.application.view.SampleDetail;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Component;

// Adapter GHI. Để package-private. Ghi đơn giản dùng JPA repository; ghi phức tạp
// (update chéo bảng, upsert) có thể dùng EntityManager native như VehicleWriteAdapter.
@Component
class SampleWriteAdapter implements WriteSamplePort {

    private final SampleJpaRepository repository;

    SampleWriteAdapter(SampleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(SampleDetail sample) {
        SampleJpaEntity entity = new SampleJpaEntity();
        entity.setId(sample.id());
        entity.setName(sample.name());
        entity.setStatus(sample.status());
        entity.setCreatedAt(sample.createdAt());
        entity.setUpdatedAt(OffsetDateTime.now());
        repository.save(entity);
    }
}
