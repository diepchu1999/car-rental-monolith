package com.ares.car_rental_monolith.modules.fleet.adapter.in.rest.response;

import com.ares.car_rental_monolith.modules.fleet.domain.FleetBranchDetail;

import java.util.UUID;

public record AdminFleetBranchDetailResponse(
        UUID uuid,
        String code,
        String name,
        String address,
        String city,
        String phone,
        String status,
        String provinceCode,
        String communeCode
) {

    public static AdminFleetBranchDetailResponse fromDomain(FleetBranchDetail c) {
        return new AdminFleetBranchDetailResponse(
                c.id(),
                c.code(),
                c.name(),
                c.address(),
                c.city(),
                c.phone(),
                c.status().toString(),
                c.provinceCode(),
                c.communeCode()
        );
    }
}
