CREATE TABLE review.reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id UUID NOT NULL,
    reviewer_customer_id UUID NOT NULL,
    reviewee_type VARCHAR(30) NOT NULL,
    reviewee_id UUID NOT NULL,
    target_type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    rating INTEGER NOT NULL,
    content TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (rating BETWEEN 1 AND 5),
    CHECK (reviewee_type IN ('RENTER', 'HOST', 'DRIVER', 'COMPANY')),
    CHECK (target_type IN ('VEHICLE', 'DRIVER', 'HOST', 'RENTER')),
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'HIDDEN'))
);

CREATE INDEX idx_reviews_target
    ON review.reviews(target_type, target_id, status);

CREATE INDEX idx_reviews_booking_id
    ON review.reviews(booking_id);

CREATE TABLE review.review_replies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL REFERENCES review.reviews(id) ON DELETE CASCADE,
    author_type VARCHAR(30) NOT NULL,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (author_type IN ('CUSTOMER', 'HOST', 'DRIVER', 'ADMIN'))
);

CREATE TABLE review.review_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL REFERENCES review.reviews(id) ON DELETE CASCADE,
    reporter_id UUID NOT NULL,
    reason_code VARCHAR(80),
    reason_text TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ,
    CHECK (status IN ('OPEN', 'RESOLVED', 'REJECTED'))
);
