package com.ares.car_rental_monolith.modules.fleet.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FleetBranchDetail(UUID id,
                                String code,
                                String name,
                                String address,
                                String city,
                                String phone,
                                FleetBranchStatus status,
                                String provinceCode,
                                String communeCode,
                                OffsetDateTime createdAt,
                                OffsetDateTime updatedAt) {
}
