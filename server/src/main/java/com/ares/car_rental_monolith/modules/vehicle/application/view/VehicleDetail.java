package com.ares.car_rental_monolith.modules.vehicle.application.view;

import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Rich read-model assembled by the persistence adapter from multiple sources.
// Lives in `application/view` because it is a denormalized projection, not a
// domain entity. Sections are nullable so admins can see which related data
// is missing — e.g. a COMPANY_OWNED vehicle has owner==null.
public record VehicleDetail(
        Vehicle vehicle,
        Owner owner,
        Fleet fleet,
        Listing listing,
        List<Image> images,
        List<Feature> features,
        PricePlan activePricePlan,
        List<AvailabilityBlock> upcomingAvailabilityBlocks,
        List<RecentBooking> recentBookings
) {

    public record Owner(
            UUID customerId,
            String fullName,
            String phone,
            String email,
            String hostCode,
            String hostDisplayName
    ) {}

    public record Fleet(
            UUID fleetVehicleId,
            String assetCode,
            String assetStatus,
            UUID branchId,
            String branchName,
            String branchCity
    ) {}

    public record Listing(
            UUID id,
            String title,
            String description,
            String city,
            String district,
            String provinceCode,
            String communeCode,
            String provinceName,
            String communeName,
            String pickupAddress,
            BigDecimal baseDailyRate,
            String currency,
            boolean instantBookingEnabled,
            boolean deliveryEnabled,
            String status,
            OffsetDateTime publishedAt
    ) {}

    public record Image(
            UUID id,
            String fileUrl,
            int sortOrder,
            boolean cover
    ) {}

    public record Feature(
            UUID id,
            String code,
            String name
    ) {}

    public record PricePlan(
            UUID id,
            String name,
            BigDecimal baseDailyRate,
            BigDecimal hourlyRate,
            BigDecimal weekendMultiplier,
            BigDecimal depositAmount,
            String currency,
            String status,
            OffsetDateTime validFrom,
            OffsetDateTime validTo
    ) {}

    public record AvailabilityBlock(
            UUID id,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String reason,
            UUID bookingId,
            String note
    ) {}

    public record RecentBooking(
            UUID id,
            String bookingCode,
            UUID customerId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            BigDecimal totalAmount,
            String currency,
            String status,
            OffsetDateTime createdAt
    ) {}
}
