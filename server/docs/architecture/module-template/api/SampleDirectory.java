package com.ares.car_rental_monolith.modules.sample.api;

import java.util.UUID;

// (TÙY CHỌN) Cổng PUBLIC cho module khác. Chỉ expose thứ tối thiểu module khác cần
// — KHÔNG lộ nội bộ. Module khác import duy nhất package api này.
public interface SampleDirectory {

    boolean exists(UUID sampleId);
}
