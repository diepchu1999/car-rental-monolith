CREATE TABLE notification.templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(80) NOT NULL UNIQUE,
    channel VARCHAR(30) NOT NULL,
    subject_template TEXT,
    body_template TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE TABLE notification.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_user_id UUID,
    recipient_customer_id UUID,
    channel VARCHAR(30) NOT NULL,
    template_code VARCHAR(80),
    title VARCHAR(255),
    content TEXT NOT NULL,
    data JSONB NOT NULL DEFAULT '{}'::jsonb,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    scheduled_at TIMESTAMPTZ,
    sent_at TIMESTAMPTZ,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),
    CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'READ', 'CANCELLED'))
);

CREATE INDEX idx_notifications_recipient
    ON notification.notifications(recipient_customer_id, status, created_at);

CREATE TABLE notification.delivery_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID NOT NULL REFERENCES notification.notifications(id) ON DELETE CASCADE,
    provider VARCHAR(60),
    provider_message_id VARCHAR(160),
    status VARCHAR(30) NOT NULL,
    error_message TEXT,
    raw_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (status IN ('SENT', 'FAILED', 'DELIVERED', 'BOUNCED'))
);

CREATE TABLE notification.devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    device_token TEXT NOT NULL,
    platform VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    last_seen_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (platform IN ('IOS', 'ANDROID', 'WEB')),
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'REVOKED'))
);

CREATE UNIQUE INDEX ux_devices_token
    ON notification.devices(device_token);
