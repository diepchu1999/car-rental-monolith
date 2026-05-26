package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest;

import com.ares.car_rental_monolith.modules.vehicle.application.command.ChangeListingStatusCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.ChangeVehicleStatusCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.CreateVehicleCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.SyncVehicleFeaturesCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.SyncVehicleImagesCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.UpdateListingCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.command.UpsertPricePlanCommand;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.ChangeListingStatusUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.ChangeVehicleStatusUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.CreateVehicleUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.GetVehicleUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.ListVehiclesUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.PageVehicleListUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.SyncVehicleFeaturesUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.SyncVehicleImagesUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.UpdateListingUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.UploadVehicleImageUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.port.in.UpsertPricePlanUseCase;
import com.ares.car_rental_monolith.modules.vehicle.application.command.UploadVehicleImageCommand;
import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request.ChangeStatusRequest;
import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request.CreateVehicleRequest;
import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request.SyncFeaturesRequest;
import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request.SyncImagesRequest;
import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request.UpdateListingRequest;
import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.request.UpsertPricePlanRequest;
import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.response.AdminVehicleDetailResponse;
import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.response.AdminVehicleListItemResponse;
import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.response.UploadImageResponse;
import com.ares.car_rental_monolith.modules.vehicle.application.query.ListVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.query.PageVehiclesQuery;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleListItem;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import com.ares.car_rental_monolith.shared.api.ApiResponse;
import com.ares.car_rental_monolith.shared.api.ListResponse;
import com.ares.car_rental_monolith.shared.api.PageResponse;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/vehicles")
public class AdminVehicleController {

    private final ListVehiclesUseCase listVehicles;
    private final PageVehicleListUseCase pageVehicleList;
    private final GetVehicleUseCase getVehicle;
    private final ChangeVehicleStatusUseCase changeVehicleStatus;
    private final ChangeListingStatusUseCase changeListingStatus;
    private final CreateVehicleUseCase createVehicle;
    private final UpdateListingUseCase updateListing;
    private final SyncVehicleImagesUseCase syncImages;
    private final SyncVehicleFeaturesUseCase syncFeatures;
    private final UpsertPricePlanUseCase upsertPricePlan;
    private final UploadVehicleImageUseCase uploadImage;

    public AdminVehicleController(
            ListVehiclesUseCase listVehicles,
            PageVehicleListUseCase pageVehicleList,
            GetVehicleUseCase getVehicle,
            ChangeVehicleStatusUseCase changeVehicleStatus,
            ChangeListingStatusUseCase changeListingStatus,
            CreateVehicleUseCase createVehicle,
            UpdateListingUseCase updateListing,
            SyncVehicleImagesUseCase syncImages,
            SyncVehicleFeaturesUseCase syncFeatures,
            UpsertPricePlanUseCase upsertPricePlan,
            UploadVehicleImageUseCase uploadImage
    ) {
        this.listVehicles = listVehicles;
        this.pageVehicleList = pageVehicleList;
        this.getVehicle = getVehicle;
        this.changeVehicleStatus = changeVehicleStatus;
        this.changeListingStatus = changeListingStatus;
        this.createVehicle = createVehicle;
        this.updateListing = updateListing;
        this.syncImages = syncImages;
        this.syncFeatures = syncFeatures;
        this.upsertPricePlan = upsertPricePlan;
        this.uploadImage = uploadImage;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ListResponse<AdminVehicleListItemResponse>>> list(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status
    ) {
        List<Vehicle> vehicles = listVehicles.handle(ListVehiclesQuery.from(source, status));
        List<AdminVehicleListItemResponse> items = vehicles.stream()
                .map(VehicleApiMapper::toListItemResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "VEHICLE_LIST_FETCHED",
                "Vehicle list fetched successfully",
                ListResponse.of(items)
        ));
    }

    @GetMapping("/paged")
    public ResponseEntity<ApiResponse<PageResponse<AdminVehicleListItemResponse>>> page(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String listingStatus,
            @RequestParam(required = false) String provinceCode,
            @RequestParam(required = false) String communeCode,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) Integer seats,
            @RequestParam(required = false) BigDecimal minRate,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false) Boolean hasBookings,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PageVehiclesQuery query = PageVehiclesQuery.from(
                q, source, status, listingStatus, provinceCode, communeCode,
                fuelType, transmission, seats, minRate, maxRate, hasBookings,
                sortBy, sortDir, page, size
        );
        PageResponse<VehicleListItem> result = pageVehicleList.handle(query);

        return ResponseEntity.ok(ApiResponse.success(
                "VEHICLE_PAGE_FETCHED",
                "Vehicle page fetched successfully",
                result.map(VehicleApiMapper::toListItemResponse)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminVehicleDetailResponse>> getById(@PathVariable UUID id) {
        VehicleDetail detail = getVehicle.handle(id);

        return ResponseEntity.ok(ApiResponse.success(
                "VEHICLE_FETCHED",
                "Vehicle fetched successfully",
                VehicleApiMapper.toDetailResponse(detail)
        ));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminVehicleDetailResponse>> changeStatus(
            @PathVariable UUID id,
            @RequestBody ChangeStatusRequest body
    ) {
        VehicleDetail detail = changeVehicleStatus.handle(
                ChangeVehicleStatusCommand.from(id, body.action()));
        return ResponseEntity.ok(ApiResponse.success(
                "VEHICLE_STATUS_CHANGED",
                "Vehicle status updated",
                VehicleApiMapper.toDetailResponse(detail)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminVehicleDetailResponse>> create(
            @RequestBody CreateVehicleRequest body
    ) {
        VehicleDetail detail = createVehicle.handle(CreateVehicleCommand.from(
                body.source(), body.ownerCustomerId(), body.assetCode(), body.branchId(),
                body.brand(), body.model(), body.version(),
                body.manufactureYear(), body.licensePlate(), body.seats(),
                body.transmission(), body.fuelType()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "VEHICLE_CREATED",
                "Vehicle created",
                VehicleApiMapper.toDetailResponse(detail)
        ));
    }

    @PatchMapping("/{id}/listing")
    public ResponseEntity<ApiResponse<AdminVehicleDetailResponse>> updateListing(
            @PathVariable UUID id,
            @RequestBody UpdateListingRequest body
    ) {
        VehicleDetail detail = updateListing.handle(UpdateListingCommand.from(
                id, body.title(), body.description(), body.provinceCode(), body.communeCode(),
                body.pickupAddress(), body.baseDailyRate(), body.currency(),
                body.instantBookingEnabled(), body.deliveryEnabled()
        ));
        return ResponseEntity.ok(ApiResponse.success(
                "LISTING_UPDATED", "Listing updated",
                VehicleApiMapper.toDetailResponse(detail)));
    }

    @PutMapping("/{id}/images")
    public ResponseEntity<ApiResponse<AdminVehicleDetailResponse>> syncImages(
            @PathVariable UUID id,
            @RequestBody SyncImagesRequest body
    ) {
        java.util.List<SyncVehicleImagesCommand.ImageInput> inputs = body.images() == null
                ? java.util.List.of()
                : body.images().stream()
                        .map(i -> new SyncVehicleImagesCommand.ImageInput(
                                i.fileUrl(),
                                i.sortOrder() == null ? 0 : i.sortOrder(),
                                Boolean.TRUE.equals(i.cover())))
                        .toList();
        VehicleDetail detail = syncImages.handle(
                SyncVehicleImagesCommand.from(id, inputs));
        return ResponseEntity.ok(ApiResponse.success(
                "IMAGES_SYNCED", "Images synced",
                VehicleApiMapper.toDetailResponse(detail)));
    }

    // Uploads a single image file to the server and returns its stored filename.
    // The filename is then persisted via the images sync endpoint above.
    @PostMapping("/images")
    public ResponseEntity<ApiResponse<UploadImageResponse>> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw DomainException.validation("file is required");
        }
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String fileName = uploadImage.handle(new UploadVehicleImageCommand(
                content, file.getOriginalFilename(), file.getContentType()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "IMAGE_UPLOADED", "Image uploaded",
                new UploadImageResponse(fileName)));
    }

    @PutMapping("/{id}/features")
    public ResponseEntity<ApiResponse<AdminVehicleDetailResponse>> syncFeatures(
            @PathVariable UUID id,
            @RequestBody SyncFeaturesRequest body
    ) {
        java.util.List<SyncVehicleFeaturesCommand.FeatureInput> inputs = body.features() == null
                ? java.util.List.of()
                : body.features().stream()
                        .map(f -> new SyncVehicleFeaturesCommand.FeatureInput(f.code(), f.name()))
                        .toList();
        VehicleDetail detail = syncFeatures.handle(
                SyncVehicleFeaturesCommand.from(id, inputs));
        return ResponseEntity.ok(ApiResponse.success(
                "FEATURES_SYNCED", "Features synced",
                VehicleApiMapper.toDetailResponse(detail)));
    }

    @PutMapping("/{id}/price-plan")
    public ResponseEntity<ApiResponse<AdminVehicleDetailResponse>> upsertPricePlan(
            @PathVariable UUID id,
            @RequestBody UpsertPricePlanRequest body
    ) {
        VehicleDetail detail = upsertPricePlan.handle(UpsertPricePlanCommand.from(
                id, body.name(), body.baseDailyRate(),
                body.hourlyRate(), body.weekendMultiplier(),
                body.depositAmount(), body.currency()
        ));
        return ResponseEntity.ok(ApiResponse.success(
                "PRICE_PLAN_UPSERTED", "Price plan upserted",
                VehicleApiMapper.toDetailResponse(detail)));
    }

    @PatchMapping("/{id}/listing/status")
    public ResponseEntity<ApiResponse<AdminVehicleDetailResponse>> changeListingStatus(
            @PathVariable UUID id,
            @RequestBody ChangeStatusRequest body
    ) {
        VehicleDetail detail = changeListingStatus.handle(
                ChangeListingStatusCommand.from(id, body.action()));
        return ResponseEntity.ok(ApiResponse.success(
                "LISTING_STATUS_CHANGED",
                "Listing status updated",
                VehicleApiMapper.toDetailResponse(detail)
        ));
    }
}
