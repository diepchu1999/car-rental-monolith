package com.ares.car_rental_monolith.modules.customer.application.command;

import com.ares.car_rental_monolith.modules.customer.adapter.in.rest.request.CreateCustomerRequest;
import com.ares.car_rental_monolith.shared.error.DomainException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

// Validated, normalized input for creating a customer. All presence/enum checks
// happen in from(), so the service can assume a clean command and the API
// returns a 400 (VALIDATION_ERROR) instead of a downstream DB/null failure.
public record CreateCustomerCommand(
        String fullName,
        String phone,
        String email,
        LocalDate dateOfBirth,
        String gender,
        List<String> roles,
        Host host,
        Kyc kyc,
        Address address
) {

    public record Host(String hostCode, String displayName, String bio) {}

    public record Kyc(
            String legalName,
            String documentType,
            String documentNumber,
            LocalDate issuedDate,
            String issuedPlace
    ) {}

    public record Address(String label, String line1, String provinceCode, String communeCode) {}

    private static final Set<String> VALID_ROLES = Set.of("RENTER", "HOST");
    private static final Set<String> VALID_GENDERS = Set.of("MALE", "FEMALE", "OTHER");
    private static final Set<String> VALID_DOC_TYPES =
            Set.of("NATIONAL_ID", "PASSPORT", "DRIVING_LICENSE");

    public static CreateCustomerCommand from(CreateCustomerRequest r) {
        requireText(r.fullName(), "fullName");
        requireText(r.phone(), "phone");

        String gender = trimToNull(r.gender());
        if (gender != null && !VALID_GENDERS.contains(gender)) {
            throw DomainException.validation("gender must be one of MALE/FEMALE/OTHER");
        }

        List<String> roles = normalizeRoles(r.roles());
        boolean isHost = roles.contains("HOST");

        Host host = null;
        if (isHost) {
            if (r.host() == null || isBlank(r.host().displayName())) {
                throw DomainException.validation("host.displayName is required when role HOST is selected");
            }
            host = new Host(
                    trimToNull(r.host().hostCode()),
                    r.host().displayName().trim(),
                    trimToNull(r.host().bio())
            );
        }

        Kyc kyc = null;
        if (r.kyc() != null) {
            CreateCustomerRequest.KycRequest k = r.kyc();
            requireText(k.legalName(), "kyc.legalName");
            requireText(k.documentNumber(), "kyc.documentNumber");
            String docType = trimToNull(k.documentType());
            if (docType == null || !VALID_DOC_TYPES.contains(docType)) {
                throw DomainException.validation(
                        "kyc.documentType must be one of NATIONAL_ID/PASSPORT/DRIVING_LICENSE");
            }
            kyc = new Kyc(
                    k.legalName().trim(), docType, k.documentNumber().trim(),
                    k.issuedDate(), trimToNull(k.issuedPlace())
            );
        }

        Address address = null;
        if (r.address() != null && !isBlank(r.address().line1())) {
            CreateCustomerRequest.AddressRequest a = r.address();
            // 2-tier model (like vehicle): a stored address must carry official
            // province + commune codes; the city/ward text is resolved from them.
            if (isBlank(a.provinceCode()) || isBlank(a.communeCode())) {
                throw DomainException.validation(
                        "address.provinceCode and address.communeCode are required");
            }
            address = new Address(
                    trimToNull(a.label()), a.line1().trim(),
                    a.provinceCode().trim(), a.communeCode().trim()
            );
        }

        return new CreateCustomerCommand(
                r.fullName().trim(), r.phone().trim(), trimToNull(r.email()),
                r.dateOfBirth(), gender, roles, host, kyc, address
        );
    }

    private static List<String> normalizeRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw DomainException.validation("at least one role is required");
        }
        List<String> normalized = roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.trim().toUpperCase())
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            throw DomainException.validation("at least one role is required");
        }
        for (String role : normalized) {
            if (!VALID_ROLES.contains(role)) {
                throw DomainException.validation("invalid role: " + role);
            }
        }
        return normalized;
    }

    private static void requireText(String value, String field) {
        if (isBlank(value)) {
            throw DomainException.validation(field + " is required");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
