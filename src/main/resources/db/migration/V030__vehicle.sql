CREATE TABLE vehicle.vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_customer_id UUID,
    fleet_vehicle_id UUID,
    source VARCHAR(30) NOT NULL,
    brand VARCHAR(80) NOT NULL,
    model VARCHAR(120) NOT NULL,
    version VARCHAR(120),
    manufacture_year INTEGER NOT NULL,
    license_plate VARCHAR(30) NOT NULL,
    seats INTEGER NOT NULL,
    transmission VARCHAR(30) NOT NULL,
    fuel_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (source IN ('HOST_OWNED', 'COMPANY_OWNED')),
    CHECK (transmission IN ('MANUAL', 'AUTOMATIC')),
    CHECK (fuel_type IN ('GASOLINE', 'DIESEL', 'ELECTRIC', 'HYBRID')),
    CHECK (status IN ('DRAFT', 'PENDING_REVIEW', 'ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

CREATE UNIQUE INDEX ux_vehicles_license_plate
    ON vehicle.vehicles(license_plate);

CREATE INDEX idx_vehicles_owner_customer_id
    ON vehicle.vehicles(owner_customer_id);

CREATE INDEX idx_vehicles_source_status
    ON vehicle.vehicles(source, status);

CREATE TABLE vehicle.vehicle_listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL UNIQUE REFERENCES vehicle.vehicles(id) ON DELETE CASCADE,
    title VARCHAR(180) NOT NULL,
    description TEXT,
    city VARCHAR(120) NOT NULL,
    district VARCHAR(120),
    pickup_address TEXT,
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    base_daily_rate NUMERIC(14, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    instant_booking_enabled BOOLEAN NOT NULL DEFAULT false,
    delivery_enabled BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (status IN ('DRAFT', 'PUBLISHED', 'PAUSED', 'REJECTED'))
);

CREATE INDEX idx_vehicle_listings_location
    ON vehicle.vehicle_listings(city, district);

CREATE INDEX idx_vehicle_listings_status
    ON vehicle.vehicle_listings(status);

CREATE TABLE vehicle.vehicle_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicle.vehicles(id) ON DELETE CASCADE,
    file_url TEXT NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_cover BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vehicle_images_vehicle_id
    ON vehicle.vehicle_images(vehicle_id);

CREATE TABLE vehicle.vehicle_features (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicle.vehicles(id) ON DELETE CASCADE,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (vehicle_id, code)
);

CREATE TABLE vehicle.availability_blocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicle.vehicles(id) ON DELETE CASCADE,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    reason VARCHAR(40) NOT NULL,
    booking_id UUID,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (end_at > start_at),
    CHECK (reason IN ('BOOKING', 'MAINTENANCE', 'HOST_BLOCKED', 'ADMIN_BLOCKED'))
);

CREATE INDEX idx_availability_blocks_vehicle_time
    ON vehicle.availability_blocks(vehicle_id, start_at, end_at);
