package com.ares.car_rental_monolith.modules.sample.application.service;

import com.ares.car_rental_monolith.modules.sample.application.port.in.GetSampleUseCase;
import com.ares.car_rental_monolith.modules.sample.application.port.in.ListSamplesUseCase;
import com.ares.car_rental_monolith.modules.sample.application.port.out.LoadSamplePort;
import com.ares.car_rental_monolith.modules.sample.application.query.ListSamplesQuery;
import com.ares.car_rental_monolith.modules.sample.application.view.SampleDetail;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Impl use case ĐỌC. Một service có thể gom nhiều use case đọc cùng nhóm.
@Service
@Transactional(readOnly = true)
class SampleQueryService implements GetSampleUseCase, ListSamplesUseCase {

    private final LoadSamplePort loadPort;

    SampleQueryService(LoadSamplePort loadPort) {
        this.loadPort = loadPort;
    }

    @Override
    public SampleDetail handle(UUID id) {
        return loadPort.loadById(id)
                .orElseThrow(() -> DomainException.notFound("Sample not found: " + id));
    }

    @Override
    public PageResponse<SampleDetail> handle(ListSamplesQuery query) {
        return loadPort.page(query);
    }
}
