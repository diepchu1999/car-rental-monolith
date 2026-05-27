package com.ares.car_rental_monolith.modules.sample.application.service;

import com.ares.car_rental_monolith.modules.sample.application.command.CreateSampleCommand;
import com.ares.car_rental_monolith.modules.sample.application.port.in.CreateSampleUseCase;
import com.ares.car_rental_monolith.modules.sample.application.port.out.WriteSamplePort;
import com.ares.car_rental_monolith.modules.sample.application.view.SampleDetail;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Impl use case GHI. Để package-private. @Transactional cho ghi.
@Service
@Transactional
class SampleCommandService implements CreateSampleUseCase {

    private final WriteSamplePort writePort;

    SampleCommandService(WriteSamplePort writePort) {
        this.writePort = writePort;
    }

    @Override
    public SampleDetail handle(CreateSampleCommand command) {
        OffsetDateTime now = OffsetDateTime.now();
        SampleDetail detail = new SampleDetail(
                UUID.randomUUID(), command.name(), "ACTIVE", now);
        writePort.create(detail);
        return detail;
    }
}
