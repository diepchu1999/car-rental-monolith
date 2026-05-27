package com.ares.car_rental_monolith.modules.sample.adapter.in.rest;

import com.ares.car_rental_monolith.modules.sample.adapter.in.rest.request.CreateSampleRequest;
import com.ares.car_rental_monolith.modules.sample.adapter.in.rest.response.SampleDetailResponse;
import com.ares.car_rental_monolith.modules.sample.application.command.CreateSampleCommand;
import com.ares.car_rental_monolith.modules.sample.application.port.in.CreateSampleUseCase;
import com.ares.car_rental_monolith.modules.sample.application.port.in.GetSampleUseCase;
import com.ares.car_rental_monolith.modules.sample.application.port.in.ListSamplesUseCase;
import com.ares.car_rental_monolith.modules.sample.application.query.ListSamplesQuery;
import com.ares.car_rental_monolith.shared.api.ApiResponse;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller chỉ phụ thuộc port/in + DTO request/response. KHÔNG gọi thẳng adapter
// hay service impl. Mỗi response bọc trong ApiResponse.
@RestController
@RequestMapping("/api/v1/admin/samples")
public class AdminSampleController {

    private final CreateSampleUseCase createSample;
    private final GetSampleUseCase getSample;
    private final ListSamplesUseCase listSamples;

    public AdminSampleController(
            CreateSampleUseCase createSample,
            GetSampleUseCase getSample,
            ListSamplesUseCase listSamples
    ) {
        this.createSample = createSample;
        this.getSample = getSample;
        this.listSamples = listSamples;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SampleDetailResponse>> create(
            @RequestBody CreateSampleRequest body
    ) {
        SampleDetailResponse created = SampleDetailResponse.fromDomain(
                createSample.handle(CreateSampleCommand.from(body)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "SAMPLE_CREATED", "Sample created", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SampleDetailResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "SAMPLE_FETCHED", "Sample fetched",
                SampleDetailResponse.fromDomain(getSample.handle(id))));
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PageResponse<SampleDetailResponse>>> paged(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PageResponse<SampleDetailResponse> result = listSamples
                .handle(ListSamplesQuery.from(q, status, page, size))
                .map(SampleDetailResponse::fromDomain);
        return ResponseEntity.ok(ApiResponse.success(
                "SAMPLES_PAGED", "Samples paged", result));
    }
}
