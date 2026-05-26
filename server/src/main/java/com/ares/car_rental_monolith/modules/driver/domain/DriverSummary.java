package com.ares.car_rental_monolith.modules.driver.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DriverSummary(
        UUID id,
        String driverCode,
        String fullName,
        String phone,
        String licenseNumber,
        String licenseClass,
        LocalDate licenseExpiryDate,
        Integer yearsOfExperience,
        BigDecimal ratingAverage,
        Integer ratingCount,
        String status
) {}
