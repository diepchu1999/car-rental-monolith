package com.ares.car_rental_monolith.modules.sample.application.port.in;

import com.ares.car_rental_monolith.modules.sample.application.query.ListSamplesQuery;
import com.ares.car_rental_monolith.modules.sample.application.view.SampleDetail;
import com.ares.car_rental_monolith.shared.api.PageResponse;

@FunctionalInterface
public interface ListSamplesUseCase {
    PageResponse<SampleDetail> handle(ListSamplesQuery query);
}
