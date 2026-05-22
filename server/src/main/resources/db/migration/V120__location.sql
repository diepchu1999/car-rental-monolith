CREATE SCHEMA IF NOT EXISTS location;

-- Vietnamese administrative units under the 2-tier model effective 2025-07-01
-- (Quyết định 19/2025/QĐ-TTg): only PROVINCE and COMMUNE levels. Districts are
-- intentionally absent from this catalog. Data is loaded by the importer from an
-- official source file (see db/seed/administrative-units/README.md) — this
-- migration creates the structure only and seeds no rows.
CREATE TABLE location.administrative_units (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    full_name VARCHAR(255),
    level VARCHAR(20) NOT NULL,
    type VARCHAR(30) NOT NULL,
    parent_code VARCHAR(20),
    effective_from DATE NOT NULL DEFAULT DATE '2025-07-01',
    effective_to DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (level IN ('PROVINCE', 'COMMUNE')),
    CHECK (type IN ('PROVINCE', 'CITY', 'COMMUNE', 'WARD', 'SPECIAL_ZONE')),
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- Lookup communes by their parent province (the main dropdown query).
CREATE INDEX idx_admin_units_parent_status
    ON location.administrative_units(parent_code, status);

-- List provinces / filter by level.
CREATE INDEX idx_admin_units_level_status
    ON location.administrative_units(level, status);
