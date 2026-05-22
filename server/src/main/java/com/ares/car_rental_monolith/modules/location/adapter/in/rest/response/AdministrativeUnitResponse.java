package com.ares.car_rental_monolith.modules.location.adapter.in.rest.response;

import com.ares.car_rental_monolith.modules.location.domain.AdministrativeUnit;

public record AdministrativeUnitResponse(
        String code,
        String name,
        String fullName,
        String type,
        String parentCode
) {
    public static AdministrativeUnitResponse from(AdministrativeUnit u) {
        return new AdministrativeUnitResponse(
                u.code(), u.name(), u.fullName(), u.type(), u.parentCode());
    }
}
