CREATE TABLE pricing.price_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    target_type VARCHAR(40) NOT NULL,
    target_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    base_daily_rate NUMERIC(14, 2) NOT NULL,
    hourly_rate NUMERIC(14, 2),
    weekend_multiplier NUMERIC(5, 2) NOT NULL DEFAULT 1,
    deposit_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    valid_from TIMESTAMPTZ,
    valid_to TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (target_type IN ('VEHICLE', 'DRIVER', 'SERVICE')),
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_price_plans_target
    ON pricing.price_plans(target_type, target_id, status);

CREATE TABLE pricing.promotions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    discount_type VARCHAR(30) NOT NULL,
    discount_value NUMERIC(14, 2) NOT NULL,
    max_discount_amount NUMERIC(14, 2),
    min_booking_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    usage_limit INTEGER,
    used_count INTEGER NOT NULL DEFAULT 0,
    valid_from TIMESTAMPTZ NOT NULL,
    valid_to TIMESTAMPTZ NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (valid_to > valid_from),
    CHECK (discount_type IN ('PERCENT', 'FIXED')),
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED'))
);

CREATE TABLE pricing.quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID,
    vehicle_id UUID NOT NULL,
    driver_id UUID,
    service_type VARCHAR(30) NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    subtotal_amount NUMERIC(14, 2) NOT NULL,
    discount_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    fee_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    deposit_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(14, 2) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (end_at > start_at),
    CHECK (service_type IN ('SELF_DRIVE', 'WITH_DRIVER'))
);

CREATE INDEX idx_quotes_vehicle_time
    ON pricing.quotes(vehicle_id, start_at, end_at);

CREATE TABLE pricing.quote_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quote_id UUID NOT NULL REFERENCES pricing.quotes(id) ON DELETE CASCADE,
    item_type VARCHAR(40) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL DEFAULT 1,
    unit_amount NUMERIC(14, 2) NOT NULL,
    total_amount NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (item_type IN ('BASE_RENTAL', 'DRIVER_FEE', 'DELIVERY_FEE', 'DISCOUNT', 'DEPOSIT', 'SURCHARGE'))
);

CREATE TABLE pricing.promotion_redemptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    promotion_id UUID NOT NULL REFERENCES pricing.promotions(id),
    customer_id UUID NOT NULL,
    booking_id UUID,
    discount_amount NUMERIC(14, 2) NOT NULL,
    redeemed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_promotion_redemptions_customer_id
    ON pricing.promotion_redemptions(customer_id);
