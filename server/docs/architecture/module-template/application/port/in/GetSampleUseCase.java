package com.ares.car_rental_monolith.modules.sample.application.port.in;

import com.ares.car_rental_monolith.modules.sample.application.view.SampleDetail;
import java.util.UUID;

@FunctionalInterface
public interface GetSampleUseCase {
    SampleDetail handle(UUID id);
}
