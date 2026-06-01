package com.ares.car_rental_monolith.modules.vehicle.application.query;

import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleFuelType;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleListingStatus;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleSource;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleStatus;
import com.ares.car_rental_monolith.modules.vehicle.domain.VehicleTransmission;
import com.ares.car_rental_monolith.shared.api.PageParams;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.math.BigDecimal;

public record PageVehiclesQuery(
        String q,
        VehicleSource source,
        VehicleStatus status,
        VehicleListingStatus listingStatus,
        String provinceCode,
        String communeCode,
        VehicleFuelType fuelType,
        VehicleTransmission transmission,
        Integer seats,
        BigDecimal minRate,
        BigDecimal maxRate,
        Boolean hasBookings,
        VehicleSortBy sortBy,
        SortDirection sortDir,
        int page,
        int size
) {
    public static PageVehiclesQuery from(
            String q,
            String source,
            String status,
            String listingStatus,
            String provinceCode,
            String communeCode,
            String fuelType,
            String transmission,
            Integer seats,
            BigDecimal minRate,
            BigDecimal maxRate,
            Boolean hasBookings,
            String sortBy,
            String sortDir,
            Integer page,
            Integer size
    ) {
        PageParams pp = PageParams.normalize(page, size, 20, 100);
        if (seats != null && seats < 0) {
            throw DomainException.validation("Parameter 'seats' must be >= 0");
        }
        if (minRate != null && maxRate != null && minRate.compareTo(maxRate) > 0) {
            throw DomainException.validation("Parameter 'minRate' cannot exceed 'maxRate'");
        }
        return new PageVehiclesQuery(
                normalizeSearch(q),
                Enums.parseStrict(VehicleSource.class, "source", source),
                Enums.parseStrict(VehicleStatus.class, "status", status),
                Enums.parseStrict(VehicleListingStatus.class, "listingStatus", listingStatus),
                blankToNull(provinceCode),
                blankToNull(communeCode),
                Enums.parseStrict(VehicleFuelType.class, "fuelType", fuelType),
                Enums.parseStrict(VehicleTransmission.class, "transmission", transmission),
                seats,
                minRate,
                maxRate,
                hasBookings,
                Enums.parseStrict(VehicleSortBy.class, "sortBy", sortBy),
                Enums.parseStrict(SortDirection.class, "sortDir", sortDir),
                pp.page(),
                pp.size()
        );
    }

    public int pageIndex() {
        return page - 1;
    }

    public String sourceCode() {
        return source == null ? null : source.name();
    }

    public String statusCode() {
        return status == null ? null : status.name();
    }

    public String listingStatusCode() {
        return listingStatus == null ? null : listingStatus.name();
    }

    public String fuelTypeCode() {
        return fuelType == null ? null : fuelType.name();
    }

    public String transmissionCode() {
        return transmission == null ? null : transmission.name();
    }

    public VehicleSortBy resolvedSortBy() {
        return sortBy == null ? VehicleSortBy.CREATED_AT : sortBy;
    }

    public SortDirection resolvedSortDir() {
        return sortDir == null ? SortDirection.DESC : sortDir;
    }

    private static String normalizeSearch(String value) {
        if (value == null || value.isBlank()) return "";
        return value.trim().toLowerCase();
    }

    private static String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
