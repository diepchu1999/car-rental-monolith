package com.ares.car_rental_monolith.modules.sample.adapter.out.persistence;

import com.ares.car_rental_monolith.modules.sample.adapter.out.persistence.entity.SampleJpaEntity;
import com.ares.car_rental_monolith.modules.sample.adapter.out.persistence.repository.SampleJpaRepository;
import com.ares.car_rental_monolith.modules.sample.application.port.out.LoadSamplePort;
import com.ares.car_rental_monolith.modules.sample.application.query.ListSamplesQuery;
import com.ares.car_rental_monolith.modules.sample.application.view.SampleDetail;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

// Adapter ĐỌC. Bản đơn giản dùng JPA repository. Khi cần lọc/join phức tạp hoặc
// đọc chéo schema, chuyển sang EntityManager native query + Tuple như
// CustomerPageAdapter / VehicleDetailQuery.
@Component
class SampleLoadAdapter implements LoadSamplePort {

    private final SampleJpaRepository repository;

    SampleLoadAdapter(SampleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<SampleDetail> loadById(UUID id) {
        return repository.findById(id).map(SampleLoadAdapter::toDetail);
    }

    @Override
    public PageResponse<SampleDetail> page(ListSamplesQuery query) {
        Page<SampleJpaEntity> result = repository.findAll(
                PageRequest.of(query.pageIndex(), query.size()));
        List<SampleDetail> items = result.getContent().stream()
                .map(SampleLoadAdapter::toDetail).toList();
        int page = query.pageIndex() + 1;
        return PageResponse.of(
                items, result.getTotalElements(), page, query.size(),
                result.getTotalPages(), result.hasNext(), result.hasPrevious());
    }

    private static SampleDetail toDetail(SampleJpaEntity e) {
        return new SampleDetail(e.getId(), e.getName(), e.getStatus(), e.getCreatedAt());
    }
}
