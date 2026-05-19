CREATE TABLE booking.bookings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_code VARCHAR(40) NOT NULL UNIQUE,
    quote_id UUID,
    customer_id UUID NOT NULL,
    host_customer_id UUID,
    vehicle_id UUID NOT NULL,
    driver_id UUID,
    service_type VARCHAR(30) NOT NULL,
    vehicle_source VARCHAR(30) NOT NULL,
    pickup_address TEXT,
    return_address TEXT,
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    subtotal_amount NUMERIC(14, 2) NOT NULL,
    discount_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    fee_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    deposit_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(14, 2) NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING_PAYMENT',
    vehicle_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    customer_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    driver_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (end_at > start_at),
    CHECK (service_type IN ('SELF_DRIVE', 'WITH_DRIVER')),
    CHECK (vehicle_source IN ('HOST_OWNED', 'COMPANY_OWNED')),
    CHECK (status IN (
        'PENDING_PAYMENT',
        'PENDING_APPROVAL',
        'CONFIRMED',
        'IN_PROGRESS',
        'COMPLETED',
        'CANCELLED',
        'EXPIRED'
    ))
);

CREATE INDEX idx_bookings_customer_id
    ON booking.bookings(customer_id);

CREATE INDEX idx_bookings_vehicle_time
    ON booking.bookings(vehicle_id, start_at, end_at);

CREATE INDEX idx_bookings_status_created_at
    ON booking.bookings(status, created_at);

CREATE TABLE booking.booking_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES booking.bookings(id) ON DELETE CASCADE,
    from_status VARCHAR(40),
    to_status VARCHAR(40) NOT NULL,
    changed_by UUID,
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_booking_status_history_booking_id
    ON booking.booking_status_history(booking_id);

CREATE TABLE booking.trip_checklists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL REFERENCES booking.bookings(id) ON DELETE CASCADE,
    checklist_type VARCHAR(30) NOT NULL,
    odometer_km INTEGER,
    fuel_percent INTEGER,
    battery_percent INTEGER,
    note TEXT,
    checked_by UUID,
    checked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (checklist_type IN ('PICKUP', 'RETURN'))
);

CREATE TABLE booking.trip_checklist_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checklist_id UUID NOT NULL REFERENCES booking.trip_checklists(id) ON DELETE CASCADE,
    item_code VARCHAR(80) NOT NULL,
    item_name VARCHAR(160) NOT NULL,
    condition VARCHAR(30) NOT NULL,
    note TEXT,
    image_url TEXT,
    CHECK (condition IN ('GOOD', 'DAMAGED', 'MISSING', 'NOT_CHECKED'))
);

CREATE TABLE booking.booking_cancellations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL UNIQUE REFERENCES booking.bookings(id) ON DELETE CASCADE,
    cancelled_by UUID,
    cancelled_by_type VARCHAR(30) NOT NULL,
    reason_code VARCHAR(80),
    reason_text TEXT,
    refund_amount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    cancelled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (cancelled_by_type IN ('CUSTOMER', 'HOST', 'DRIVER', 'ADMIN', 'SYSTEM'))
);
