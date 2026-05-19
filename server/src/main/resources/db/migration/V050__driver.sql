    CREATE TABLE driver.drivers (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID,
        driver_code VARCHAR(40) NOT NULL UNIQUE,
        full_name VARCHAR(160) NOT NULL,
        phone VARCHAR(30) NOT NULL,
        license_number VARCHAR(80) NOT NULL,
        license_class VARCHAR(20) NOT NULL,
        license_expiry_date DATE,
        years_of_experience INTEGER NOT NULL DEFAULT 0,
        rating_average NUMERIC(3, 2) NOT NULL DEFAULT 0,
        rating_count INTEGER NOT NULL DEFAULT 0,
        status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
        updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
        CHECK (status IN ('AVAILABLE', 'ON_TRIP', 'OFF_DUTY', 'SUSPENDED'))
    );

    CREATE INDEX idx_drivers_status
        ON driver.drivers(status);

    CREATE UNIQUE INDEX ux_drivers_phone
        ON driver.drivers(phone);

    CREATE TABLE driver.driver_documents (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        driver_id UUID NOT NULL REFERENCES driver.drivers(id) ON DELETE CASCADE,
        document_type VARCHAR(40) NOT NULL,
        file_url TEXT NOT NULL,
        expires_at DATE,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
        CHECK (document_type IN ('LICENSE_FRONT', 'LICENSE_BACK', 'PORTRAIT', 'OTHER'))
    );

    CREATE TABLE driver.availability_slots (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        driver_id UUID NOT NULL REFERENCES driver.drivers(id) ON DELETE CASCADE,
        start_at TIMESTAMPTZ NOT NULL,
        end_at TIMESTAMPTZ NOT NULL,
        status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
        note TEXT,
        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
        CHECK (end_at > start_at),
        CHECK (status IN ('AVAILABLE', 'UNAVAILABLE', 'BOOKED'))
    );

    CREATE INDEX idx_availability_slots_driver_time
        ON driver.availability_slots(driver_id, start_at, end_at);

    CREATE TABLE driver.driver_assignments (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        driver_id UUID NOT NULL REFERENCES driver.drivers(id),
        booking_id UUID NOT NULL,
        vehicle_id UUID,
        status VARCHAR(30) NOT NULL DEFAULT 'ASSIGNED',
        assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
        accepted_at TIMESTAMPTZ,
        rejected_at TIMESTAMPTZ,
        completed_at TIMESTAMPTZ,
        note TEXT,
        CHECK (status IN ('ASSIGNED', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED'))
    );

    CREATE INDEX idx_driver_assignments_booking_id
        ON driver.driver_assignments(booking_id);

    CREATE INDEX idx_driver_assignments_driver_status
        ON driver.driver_assignments(driver_id, status);
