package com.ares.car_rental_monolith.modules.sample.application.port.out;

import com.ares.car_rental_monolith.modules.sample.application.query.ListSamplesQuery;
import com.ares.car_rental_monolith.modules.sample.application.view.SampleDetail;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.Optional;
import java.util.UUID;

// Cổng RA cho phần đọc. Adapter persistence sẽ implement.
public interface LoadSamplePort {

    Optional<SampleDetail> loadById(UUID id);

    PageResponse<SampleDetail> page(ListSamplesQuery query);
}
