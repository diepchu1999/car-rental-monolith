package com.ares.car_rental_monolith.modules.customer.adapter.in.rest.request;

import java.time.LocalDate;
import java.util.List;

public record CreateCustomerRequest(
        String fullName,
        String phone,
        String email,
        LocalDate dateOfBirth,
        String gender,
        List<String> roles,
        HostRequest host,
        KycRequest kyc,
        AddressRequest address
) {

    public record HostRequest(
            String hostCode,
            String displayName,
            String bio
    ) {}

    public record KycRequest(
            String legalName,
            String documentType,
            String documentNumber,
            LocalDate issuedDate,
            String issuedPlace
    ) {}

    public record AddressRequest(
            String label,
            String line1,
            String provinceCode,
            String communeCode
    ) {}
}
