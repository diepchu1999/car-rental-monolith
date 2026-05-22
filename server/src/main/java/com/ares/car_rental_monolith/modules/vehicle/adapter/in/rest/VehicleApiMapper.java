package com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest;

import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.response.AdminVehicleDetailResponse;
import com.ares.car_rental_monolith.modules.vehicle.adapter.in.rest.response.AdminVehicleListItemResponse;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleDetail;
import com.ares.car_rental_monolith.modules.vehicle.application.view.VehicleListItem;
import com.ares.car_rental_monolith.modules.vehicle.domain.Vehicle;
import java.util.List;

final class VehicleApiMapper {

    private VehicleApiMapper() {}

    // Used by the plain /list endpoint — enriched fields are null (no joins)
    static AdminVehicleListItemResponse toListItemResponse(Vehicle v) {
        return new AdminVehicleListItemResponse(
                v.id(), v.ownerCustomerId(), null, null,
                v.fleetVehicleId(), null, null,
                enumName(v.source()), v.brand(), v.model(), v.version(),
                v.manufactureYear(), v.licensePlate(), v.seats(),
                enumName(v.transmission()), enumName(v.fuelType()), enumName(v.status()),
                null, null, null, null, null, 0L, 0L, 0L,
                v.createdAt(), v.updatedAt()
        );
    }

    static AdminVehicleListItemResponse toListItemResponse(VehicleListItem item) {
        return new AdminVehicleListItemResponse(
                item.id(),
                item.ownerCustomerId(),
                item.ownerCustomerName(),
                item.hostCode(),
                item.fleetVehicleId(),
                item.assetCode(),
                item.branchName(),
                enumName(item.source()),
                item.brand(),
                item.model(),
                item.version(),
                item.manufactureYear(),
                item.licensePlate(),
                item.seats(),
                enumName(item.transmission()),
                enumName(item.fuelType()),
                enumName(item.status()),
                item.listingStatus(),
                item.city(),
                item.district(),
                item.baseDailyRate(),
                item.coverImageUrl(),
                item.featureCount(),
                item.activeAvailabilityBlockCount(),
                item.bookingCount(),
                item.createdAt(),
                item.updatedAt()
        );
    }

    static AdminVehicleDetailResponse toDetailResponse(VehicleDetail detail) {
        var v = detail.vehicle();
        return new AdminVehicleDetailResponse(
                v.id(),
                v.ownerCustomerId(),
                v.fleetVehicleId(),
                enumName(v.source()),
                v.brand(),
                v.model(),
                v.version(),
                v.manufactureYear(),
                v.licensePlate(),
                v.seats(),
                enumName(v.transmission()),
                enumName(v.fuelType()),
                enumName(v.status()),
                v.createdAt(),
                v.updatedAt(),
                toOwner(detail.owner()),
                toFleet(detail.fleet()),
                toListing(detail.listing()),
                toImages(detail.images()),
                toFeatures(detail.features()),
                toPricePlan(detail.activePricePlan()),
                toBlocks(detail.upcomingAvailabilityBlocks()),
                toRecentBookings(detail.recentBookings())
        );
    }

    private static AdminVehicleDetailResponse.Owner toOwner(VehicleDetail.Owner o) {
        return o == null ? null : new AdminVehicleDetailResponse.Owner(
                o.customerId(), o.fullName(), o.phone(), o.email(),
                o.hostCode(), o.hostDisplayName()
        );
    }

    private static AdminVehicleDetailResponse.Fleet toFleet(VehicleDetail.Fleet f) {
        return f == null ? null : new AdminVehicleDetailResponse.Fleet(
                f.fleetVehicleId(), f.assetCode(), f.assetStatus(),
                f.branchId(), f.branchName(), f.branchCity()
        );
    }

    private static AdminVehicleDetailResponse.Listing toListing(VehicleDetail.Listing l) {
        return l == null ? null : new AdminVehicleDetailResponse.Listing(
                l.id(), l.title(), l.description(), l.city(), l.district(),
                l.provinceCode(), l.communeCode(), l.provinceName(), l.communeName(),
                l.pickupAddress(), l.baseDailyRate(), l.currency(),
                l.instantBookingEnabled(), l.deliveryEnabled(),
                l.status(), l.publishedAt()
        );
    }

    private static List<AdminVehicleDetailResponse.Image> toImages(List<VehicleDetail.Image> images) {
        return images.stream()
                .map(i -> new AdminVehicleDetailResponse.Image(
                        i.id(), i.fileUrl(), i.sortOrder(), i.cover()))
                .toList();
    }

    private static List<AdminVehicleDetailResponse.Feature> toFeatures(List<VehicleDetail.Feature> features) {
        return features.stream()
                .map(f -> new AdminVehicleDetailResponse.Feature(f.id(), f.code(), f.name()))
                .toList();
    }

    private static AdminVehicleDetailResponse.PricePlan toPricePlan(VehicleDetail.PricePlan p) {
        return p == null ? null : new AdminVehicleDetailResponse.PricePlan(
                p.id(), p.name(), p.baseDailyRate(), p.hourlyRate(),
                p.weekendMultiplier(), p.depositAmount(), p.currency(),
                p.status(), p.validFrom(), p.validTo()
        );
    }

    private static List<AdminVehicleDetailResponse.AvailabilityBlock> toBlocks(List<VehicleDetail.AvailabilityBlock> blocks) {
        return blocks.stream()
                .map(b -> new AdminVehicleDetailResponse.AvailabilityBlock(
                        b.id(), b.startAt(), b.endAt(), b.reason(), b.bookingId(), b.note()))
                .toList();
    }

    private static List<AdminVehicleDetailResponse.RecentBooking> toRecentBookings(List<VehicleDetail.RecentBooking> bookings) {
        return bookings.stream()
                .map(b -> new AdminVehicleDetailResponse.RecentBooking(
                        b.id(), b.bookingCode(), b.customerId(), b.startAt(), b.endAt(),
                        b.totalAmount(), b.currency(), b.status(), b.createdAt()))
                .toList();
    }

    private static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
