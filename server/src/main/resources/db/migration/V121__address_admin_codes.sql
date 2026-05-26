-- Add official administrative codes (2-tier model, effective 2025-07-01) to the
-- address-bearing tables. The codes reference location.administrative_units(code)
-- by value only — no cross-schema FK, consistent with this codebase's module
-- isolation. Legacy free-text columns (city / district / ward) are intentionally
-- KEPT and not dropped, so historical bookings, customers, branches and listings
-- remain readable. New address entry populates province_code / commune_code;
-- legacy district is read-only.

ALTER TABLE vehicle.vehicle_listings
    ADD COLUMN province_code VARCHAR(20),
    ADD COLUMN commune_code VARCHAR(20);

ALTER TABLE customer.addresses
    ADD COLUMN province_code VARCHAR(20),
    ADD COLUMN commune_code VARCHAR(20);

ALTER TABLE fleet.branches
    ADD COLUMN province_code VARCHAR(20),
    ADD COLUMN commune_code VARCHAR(20);

CREATE INDEX idx_vehicle_listings_admin_codes
    ON vehicle.vehicle_listings(province_code, commune_code);

CREATE INDEX idx_addresses_admin_codes
    ON customer.addresses(province_code, commune_code);

CREATE INDEX idx_branches_admin_codes
    ON fleet.branches(province_code, commune_code);
