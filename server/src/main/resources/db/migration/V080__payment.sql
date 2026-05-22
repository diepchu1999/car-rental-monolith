CREATE TABLE payment.payment_intents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL,
    payer_customer_id UUID NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    provider VARCHAR(40) NOT NULL,
    provider_reference VARCHAR(160),
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (provider IN ('VNPAY', 'MOMO', 'BANK_TRANSFER', 'CASH')),
    CHECK (status IN ('CREATED', 'PENDING', 'SUCCEEDED', 'FAILED', 'CANCELLED', 'EXPIRED'))
);

CREATE INDEX idx_payment_intents_booking_id
    ON payment.payment_intents(booking_id);

CREATE TABLE payment.transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_intent_id UUID NOT NULL REFERENCES payment.payment_intents(id) ON DELETE CASCADE,
    transaction_code VARCHAR(80) NOT NULL UNIQUE,
    provider VARCHAR(40) NOT NULL,
    provider_transaction_id VARCHAR(160),
    amount NUMERIC(14, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    status VARCHAR(30) NOT NULL,
    raw_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_transactions_payment_intent_id
    ON payment.transactions(payment_intent_id);

CREATE TABLE payment.refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES payment.transactions(id),
    refund_code VARCHAR(80) NOT NULL UNIQUE,
    amount NUMERIC(14, 2) NOT NULL,
    reason TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    provider_reference VARCHAR(160),
    refunded_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED'))
);

CREATE TABLE payment.payouts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_type VARCHAR(30) NOT NULL,
    recipient_id UUID NOT NULL,
    booking_id UUID,
    amount NUMERIC(14, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    scheduled_at TIMESTAMPTZ,
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (recipient_type IN ('HOST', 'DRIVER', 'COMPANY')),
    CHECK (status IN ('PENDING', 'PROCESSING', 'PAID', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_payouts_recipient
    ON payment.payouts(recipient_type, recipient_id);
