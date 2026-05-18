CREATE TABLE customer.customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(20),
    avatar_url TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (gender IS NULL OR gender IN ('MALE', 'FEMALE', 'OTHER')),
    CHECK (status IN ('ACTIVE', 'BLOCKED', 'DELETED', 'PENDING_KYC'))
);

CREATE UNIQUE INDEX ux_customers_user_id
    ON customer.customers(user_id);

CREATE INDEX idx_customers_phone
    ON customer.customers(phone);

CREATE TABLE customer.customer_roles (
    customer_id UUID NOT NULL REFERENCES customer.customers(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (customer_id, role),
    CHECK (role IN ('RENTER', 'HOST'))
);

CREATE TABLE customer.host_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL UNIQUE REFERENCES customer.customers(id) ON DELETE CASCADE,
    host_code VARCHAR(40) NOT NULL UNIQUE,
    display_name VARCHAR(160) NOT NULL,
    bio TEXT,
    rating_average NUMERIC(3, 2) NOT NULL DEFAULT 0,
    rating_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING_KYC'))
);

CREATE TABLE customer.kyc_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL UNIQUE REFERENCES customer.customers(id) ON DELETE CASCADE,
    legal_name VARCHAR(160) NOT NULL,
    document_type VARCHAR(30) NOT NULL,
    document_number VARCHAR(80) NOT NULL,
    issued_date DATE,
    issued_place VARCHAR(160),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reviewed_by UUID,
    reviewed_at TIMESTAMPTZ,
    rejection_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (document_type IN ('NATIONAL_ID', 'PASSPORT', 'DRIVING_LICENSE')),
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED'))
);

CREATE TABLE customer.kyc_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kyc_profile_id UUID NOT NULL REFERENCES customer.kyc_profiles(id) ON DELETE CASCADE,
    document_side VARCHAR(20) NOT NULL,
    file_url TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (document_side IN ('FRONT', 'BACK', 'SELFIE', 'OTHER'))
);

CREATE TABLE customer.addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customer.customers(id) ON DELETE CASCADE,
    label VARCHAR(80),
    line1 VARCHAR(255) NOT NULL,
    ward VARCHAR(120),
    district VARCHAR(120),
    city VARCHAR(120) NOT NULL,
    country VARCHAR(80) NOT NULL DEFAULT 'Vietnam',
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_addresses_customer_id
    ON customer.addresses(customer_id);
