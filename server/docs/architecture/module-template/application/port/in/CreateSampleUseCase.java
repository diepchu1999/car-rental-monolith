package com.ares.car_rental_monolith.modules.sample.application.port.in;

import com.ares.car_rental_monolith.modules.sample.application.command.CreateSampleCommand;
import com.ares.car_rental_monolith.modules.sample.application.view.SampleDetail;

@FunctionalInterface
public interface CreateSampleUseCase {
    SampleDetail handle(CreateSampleCommand command);
}
