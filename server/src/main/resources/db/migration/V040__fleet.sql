CREATE TABLE fleet.branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE fleet.company_vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL UNIQUE,
    branch_id UUID REFERENCES fleet.branches(id),
    asset_code VARCHAR(60) NOT NULL UNIQUE,
    vin VARCHAR(80),
    purchase_date DATE,
    purchase_price NUMERIC(14, 2),
    current_odometer_km INTEGER NOT NULL DEFAULT 0,
    asset_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    next_maintenance_at DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (asset_status IN ('AVAILABLE', 'RENTED', 'MAINTENANCE', 'RETIRED'))
);

CREATE INDEX idx_company_vehicles_branch_status
    ON fleet.company_vehicles(branch_id, asset_status);

CREATE TABLE fleet.maintenance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_vehicle_id UUID NOT NULL REFERENCES fleet.company_vehicles(id) ON DELETE CASCADE,
    maintenance_type VARCHAR(40) NOT NULL,
    odometer_km INTEGER,
    vendor_name VARCHAR(160),
    cost_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    note TEXT,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (maintenance_type IN ('PERIODIC', 'REPAIR', 'INSPECTION', 'CLEANING'))
);

CREATE INDEX idx_maintenance_records_vehicle_id
    ON fleet.maintenance_records(company_vehicle_id);

CREATE TABLE fleet.insurance_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_vehicle_id UUID NOT NULL REFERENCES fleet.company_vehicles(id) ON DELETE CASCADE,
    provider_name VARCHAR(160) NOT NULL,
    policy_number VARCHAR(120) NOT NULL,
    coverage_type VARCHAR(80) NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE NOT NULL,
    file_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (valid_to >= valid_from)
);

CREATE INDEX idx_insurance_policies_valid_to
    ON fleet.insurance_policies(valid_to);

CREATE TABLE fleet.inspection_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_vehicle_id UUID NOT NULL REFERENCES fleet.company_vehicles(id) ON DELETE CASCADE,
    inspection_date DATE NOT NULL,
    valid_until DATE NOT NULL,
    result VARCHAR(30) NOT NULL,
    file_url TEXT,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (result IN ('PASSED', 'FAILED', 'CONDITIONAL'))
);
