package com.ares.car_rental_monolith.modules.sample.application.port.out;

import com.ares.car_rental_monolith.modules.sample.application.view.SampleDetail;

// Cổng RA cho phần ghi.
public interface WriteSamplePort {

    void create(SampleDetail sample);
}
