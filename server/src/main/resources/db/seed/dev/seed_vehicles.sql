BEGIN;

INSERT INTO identity.roles (id, code, name, description)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'CUSTOMER', 'Customer', 'Default renter customer role'),
    ('00000000-0000-0000-0000-000000000102', 'HOST', 'Host', 'Vehicle owner role'),
    ('00000000-0000-0000-0000-000000000103', 'DRIVER', 'Driver', 'Company driver role'),
    ('00000000-0000-0000-0000-000000000104', 'FLEET_MANAGER', 'Fleet manager', 'Fleet operation role'),
    ('00000000-0000-0000-0000-000000000105', 'ADMIN', 'Admin', 'Backoffice administrator role'),
    ('00000000-0000-0000-0000-000000000106', 'FINANCE', 'Finance', 'Payment and payout operator role')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description;

INSERT INTO identity.permissions (id, code, description)
VALUES
    ('00000000-0000-0000-0000-000000000201', 'vehicle.read', 'Read vehicles'),
    ('00000000-0000-0000-0000-000000000202', 'vehicle.review', 'Review vehicle listings'),
    ('00000000-0000-0000-0000-000000000203', 'booking.read', 'Read bookings'),
    ('00000000-0000-0000-0000-000000000204', 'booking.manage', 'Manage bookings'),
    ('00000000-0000-0000-0000-000000000205', 'fleet.read', 'Read fleet data'),
    ('00000000-0000-0000-0000-000000000206', 'fleet.manage', 'Manage fleet data'),
    ('00000000-0000-0000-0000-000000000207', 'payment.read', 'Read payments'),
    ('00000000-0000-0000-0000-000000000208', 'payment.refund', 'Issue refunds'),
    ('00000000-0000-0000-0000-000000000209', 'review.moderate', 'Moderate reviews'),
    ('00000000-0000-0000-0000-000000000210', 'admin.audit.read', 'Read admin audit logs')
ON CONFLICT (code) DO UPDATE SET
    description = EXCLUDED.description;

INSERT INTO identity.role_permissions (role_id, permission_id)
SELECT roles.id, permissions.id
FROM identity.roles roles
JOIN identity.permissions permissions ON (
    roles.code = 'ADMIN'
    OR (roles.code = 'FLEET_MANAGER' AND permissions.code IN ('vehicle.read', 'vehicle.review', 'fleet.read', 'fleet.manage', 'booking.read'))
    OR (roles.code = 'FINANCE' AND permissions.code IN ('booking.read', 'payment.read', 'payment.refund'))
    OR (roles.code = 'HOST' AND permissions.code IN ('vehicle.read', 'booking.read'))
    OR (roles.code = 'CUSTOMER' AND permissions.code IN ('vehicle.read', 'booking.read'))
    OR (roles.code = 'DRIVER' AND permissions.code IN ('booking.read'))
)
ON CONFLICT (role_id, permission_id) DO NOTHING;

CREATE TEMP TABLE __seed_customers ON COMMIT DROP AS
SELECT
    customer_no,
    (
        SUBSTR(MD5('aresdrive-owner-customer-' || customer_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-owner-customer-' || customer_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-owner-customer-' || customer_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-owner-customer-' || customer_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-owner-customer-' || customer_no), 21, 12)
    )::UUID AS customer_id,
    (
        SUBSTR(MD5('aresdrive-customer-user-' || customer_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-customer-user-' || customer_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-customer-user-' || customer_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-customer-user-' || customer_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-customer-user-' || customer_no), 21, 12)
    )::UUID AS user_id,
    'Seed Customer ' || LPAD(customer_no::TEXT, 3, '0') AS full_name,
    '+8490' || LPAD(customer_no::TEXT, 7, '0') AS phone,
    'customer' || LPAD(customer_no::TEXT, 3, '0') || '@aresdrive.test' AS email,
    DATE '1980-01-01' + (customer_no * 37 % 9000) AS date_of_birth,
    CASE customer_no % 3 WHEN 0 THEN 'OTHER' WHEN 1 THEN 'MALE' ELSE 'FEMALE' END AS gender,
    CASE customer_no % 20 WHEN 0 THEN 'PENDING_KYC' WHEN 1 THEN 'BLOCKED' ELSE 'ACTIVE' END AS status,
    now() - (customer_no * INTERVAL '6 hours') AS created_at,
    now() - (customer_no * INTERVAL '3 hours') AS updated_at
FROM generate_series(1, 120) AS series(customer_no);

INSERT INTO identity.users (
    id,
    external_subject,
    phone,
    email,
    password_hash,
    display_name,
    status,
    last_login_at,
    created_at,
    updated_at
)
SELECT
    user_id,
    'seed-customer-' || LPAD(customer_no::TEXT, 3, '0'),
    phone,
    email,
    '$2a$10$seeded.password.hash.for.local.dev.only',
    full_name,
    CASE status WHEN 'BLOCKED' THEN 'LOCKED' WHEN 'PENDING_KYC' THEN 'PENDING' ELSE 'ACTIVE' END,
    now() - (customer_no * INTERVAL '2 hours'),
    created_at,
    updated_at
FROM __seed_customers
ON CONFLICT (id) DO UPDATE SET
    external_subject = EXCLUDED.external_subject,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    display_name = EXCLUDED.display_name,
    status = EXCLUDED.status,
    last_login_at = EXCLUDED.last_login_at,
    updated_at = now();

INSERT INTO customer.customers (
    id,
    user_id,
    full_name,
    phone,
    email,
    date_of_birth,
    gender,
    avatar_url,
    status,
    created_at,
    updated_at
)
SELECT
    customer_id,
    user_id,
    full_name,
    phone,
    email,
    date_of_birth,
    gender,
    'https://cdn.aresdrive.test/seed/customers/avatar-' || LPAD(customer_no::TEXT, 3, '0') || '.jpg',
    status,
    created_at,
    updated_at
FROM __seed_customers
ON CONFLICT (id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    full_name = EXCLUDED.full_name,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    date_of_birth = EXCLUDED.date_of_birth,
    gender = EXCLUDED.gender,
    avatar_url = EXCLUDED.avatar_url,
    status = EXCLUDED.status,
    updated_at = now();

INSERT INTO identity.user_roles (user_id, role_id)
SELECT users.user_id, roles.id
FROM __seed_customers users
JOIN identity.roles roles ON roles.code = 'CUSTOMER'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO identity.user_roles (user_id, role_id)
SELECT users.user_id, roles.id
FROM __seed_customers users
JOIN identity.roles roles ON roles.code = 'HOST'
WHERE users.customer_no <= 75
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO customer.customer_roles (customer_id, role)
SELECT customer_id, 'RENTER'
FROM __seed_customers
ON CONFLICT (customer_id, role) DO NOTHING;

INSERT INTO customer.customer_roles (customer_id, role)
SELECT customer_id, 'HOST'
FROM __seed_customers
WHERE customer_no <= 75
ON CONFLICT (customer_id, role) DO NOTHING;

INSERT INTO customer.host_profiles (
    id,
    customer_id,
    host_code,
    display_name,
    bio,
    rating_average,
    rating_count,
    status,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-host-profile-' || customer_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-host-profile-' || customer_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-host-profile-' || customer_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-host-profile-' || customer_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-host-profile-' || customer_no), 21, 12)
    )::UUID,
    customer_id,
    'HOST-' || LPAD(customer_no::TEXT, 4, '0'),
    'Ares Host ' || LPAD(customer_no::TEXT, 3, '0'),
    'Seed host profile for vehicle UI testing.',
    ROUND((3.8 + ((customer_no % 12) * 0.1))::NUMERIC, 2),
    5 + (customer_no % 80),
    CASE customer_no % 18 WHEN 0 THEN 'SUSPENDED' WHEN 1 THEN 'PENDING_KYC' ELSE 'ACTIVE' END,
    created_at,
    updated_at
FROM __seed_customers
WHERE customer_no <= 75
ON CONFLICT (customer_id) DO UPDATE SET
    host_code = EXCLUDED.host_code,
    display_name = EXCLUDED.display_name,
    bio = EXCLUDED.bio,
    rating_average = EXCLUDED.rating_average,
    rating_count = EXCLUDED.rating_count,
    status = EXCLUDED.status,
    updated_at = now();

INSERT INTO customer.kyc_profiles (
    id,
    customer_id,
    legal_name,
    document_type,
    document_number,
    issued_date,
    issued_place,
    status,
    reviewed_at,
    rejection_reason,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-kyc-profile-' || customer_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-kyc-profile-' || customer_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-kyc-profile-' || customer_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-kyc-profile-' || customer_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-kyc-profile-' || customer_no), 21, 12)
    )::UUID,
    customer_id,
    full_name,
    CASE customer_no % 3 WHEN 0 THEN 'PASSPORT' WHEN 1 THEN 'NATIONAL_ID' ELSE 'DRIVING_LICENSE' END,
    'KYC' || LPAD(customer_no::TEXT, 9, '0'),
    DATE '2018-01-01' + (customer_no * 13 % 1800),
    CASE customer_no % 4 WHEN 0 THEN 'Ho Chi Minh' WHEN 1 THEN 'Ha Noi' WHEN 2 THEN 'Da Nang' ELSE 'Can Tho' END,
    CASE customer_no % 14 WHEN 0 THEN 'REJECTED' WHEN 1 THEN 'PENDING' ELSE 'APPROVED' END,
    CASE customer_no % 14 WHEN 1 THEN NULL ELSE now() - (customer_no * INTERVAL '1 day') END,
    CASE customer_no % 14 WHEN 0 THEN 'Seed rejection reason for UI state testing' ELSE NULL END,
    created_at,
    updated_at
FROM __seed_customers
ON CONFLICT (customer_id) DO UPDATE SET
    legal_name = EXCLUDED.legal_name,
    document_type = EXCLUDED.document_type,
    document_number = EXCLUDED.document_number,
    issued_date = EXCLUDED.issued_date,
    issued_place = EXCLUDED.issued_place,
    status = EXCLUDED.status,
    reviewed_at = EXCLUDED.reviewed_at,
    rejection_reason = EXCLUDED.rejection_reason,
    updated_at = now();

DELETE FROM customer.kyc_documents documents
USING customer.kyc_profiles profiles
JOIN __seed_customers seeded ON seeded.customer_id = profiles.customer_id
WHERE documents.kyc_profile_id = profiles.id
  AND documents.file_url LIKE 'https://cdn.aresdrive.test/seed/kyc/%';

INSERT INTO customer.kyc_documents (
    kyc_profile_id,
    document_side,
    file_url,
    created_at
)
SELECT
    profiles.id,
    document_side,
    'https://cdn.aresdrive.test/seed/kyc/customer-' ||
        LPAD(seeded.customer_no::TEXT, 3, '0') || '-' || LOWER(document_side) || '.jpg',
    seeded.created_at
FROM __seed_customers seeded
JOIN customer.kyc_profiles profiles ON profiles.customer_id = seeded.customer_id
CROSS JOIN (VALUES ('FRONT'), ('BACK'), ('SELFIE')) AS sides(document_side);

DELETE FROM customer.addresses addresses
USING __seed_customers seeded
WHERE addresses.customer_id = seeded.customer_id
  AND addresses.label LIKE 'Seed%';

INSERT INTO customer.addresses (
    id,
    customer_id,
    label,
    line1,
    ward,
    district,
    city,
    country,
    latitude,
    longitude,
    is_default,
    created_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-address-' || customer_no || '-' || address_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-address-' || customer_no || '-' || address_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-address-' || customer_no || '-' || address_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-address-' || customer_no || '-' || address_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-address-' || customer_no || '-' || address_no), 21, 12)
    )::UUID,
    customer_id,
    CASE address_no WHEN 1 THEN 'Seed home' ELSE 'Seed office' END,
    CASE address_no WHEN 1 THEN 'Seed home address ' ELSE 'Seed office address ' END || customer_no,
    CASE customer_no % 4 WHEN 0 THEN 'Ben Nghe' WHEN 1 THEN 'Dich Vong' WHEN 2 THEN 'Hai Chau 1' ELSE 'Ninh Kieu' END,
    CASE customer_no % 4 WHEN 0 THEN 'Quan 1' WHEN 1 THEN 'Cau Giay' WHEN 2 THEN 'Hai Chau' ELSE 'Ninh Kieu' END,
    CASE customer_no % 4 WHEN 0 THEN 'Ho Chi Minh' WHEN 1 THEN 'Ha Noi' WHEN 2 THEN 'Da Nang' ELSE 'Can Tho' END,
    'Vietnam',
    10.7000000 + ((customer_no % 80) * 0.0100000),
    106.6000000 + ((customer_no % 90) * 0.0100000),
    address_no = 1,
    created_at
FROM __seed_customers
CROSS JOIN generate_series(1, 2) AS addresses(address_no)
ON CONFLICT (id) DO UPDATE SET
    label = EXCLUDED.label,
    line1 = EXCLUDED.line1,
    ward = EXCLUDED.ward,
    district = EXCLUDED.district,
    city = EXCLUDED.city,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    is_default = EXCLUDED.is_default;

CREATE TEMP TABLE __seed_admin_users ON COMMIT DROP AS
SELECT *
FROM (VALUES
    (1, 'admin.ops@aresdrive.test', '+84880000001', 'Ares Admin Ops', 'ADMIN'),
    (2, 'fleet.manager@aresdrive.test', '+84880000002', 'Ares Fleet Manager', 'FLEET_MANAGER'),
    (3, 'finance.operator@aresdrive.test', '+84880000003', 'Ares Finance Operator', 'FINANCE')
) AS admins(admin_no, email, phone, display_name, role_code);

INSERT INTO identity.users (
    id,
    external_subject,
    phone,
    email,
    password_hash,
    display_name,
    status,
    last_login_at,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-admin-user-' || admin_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-' || admin_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-' || admin_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-' || admin_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-' || admin_no), 21, 12)
    )::UUID,
    'seed-admin-' || admin_no,
    phone,
    email,
    '$2a$10$seeded.password.hash.for.local.dev.only',
    display_name,
    'ACTIVE',
    now() - (admin_no * INTERVAL '30 minutes'),
    now() - INTERVAL '90 days',
    now()
FROM __seed_admin_users
ON CONFLICT (id) DO UPDATE SET
    external_subject = EXCLUDED.external_subject,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    display_name = EXCLUDED.display_name,
    status = EXCLUDED.status,
    last_login_at = EXCLUDED.last_login_at,
    updated_at = now();

INSERT INTO identity.user_roles (user_id, role_id)
SELECT
    (
        SUBSTR(MD5('aresdrive-admin-user-' || admins.admin_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-' || admins.admin_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-' || admins.admin_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-' || admins.admin_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-' || admins.admin_no), 21, 12)
    )::UUID,
    roles.id
FROM __seed_admin_users admins
JOIN identity.roles roles ON roles.code = admins.role_code
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO fleet.branches (
    id,
    code,
    name,
    address,
    city,
    phone,
    status,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-branch-' || branch_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-branch-' || branch_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-branch-' || branch_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-branch-' || branch_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-branch-' || branch_no), 21, 12)
    )::UUID,
    code,
    name,
    address,
    city,
    phone,
    status,
    now() - INTERVAL '180 days',
    now()
FROM (VALUES
    (1, 'SGN-Q1', 'Saigon District 1 Branch', '12 Nguyen Hue, Quan 1', 'Ho Chi Minh', '+842812345001', 'ACTIVE'),
    (2, 'SGN-Q7', 'Saigon District 7 Branch', '88 Nguyen Van Linh, Quan 7', 'Ho Chi Minh', '+842812345002', 'ACTIVE'),
    (3, 'HAN-HK', 'Hanoi Hoan Kiem Branch', '18 Trang Tien, Hoan Kiem', 'Ha Noi', '+842412345003', 'ACTIVE'),
    (4, 'DAD-HC', 'Da Nang Hai Chau Branch', '30 Bach Dang, Hai Chau', 'Da Nang', '+842361234504', 'ACTIVE'),
    (5, 'CXR-LT', 'Nha Trang Loc Tho Branch', '44 Tran Phu, Loc Tho', 'Nha Trang', '+842581234505', 'ACTIVE'),
    (6, 'PQC-DD', 'Phu Quoc Duong Dong Branch', '66 Tran Hung Dao, Duong Dong', 'Phu Quoc', '+842971234506', 'INACTIVE')
) AS branches(branch_no, code, name, address, city, phone, status)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    address = EXCLUDED.address,
    city = EXCLUDED.city,
    phone = EXCLUDED.phone,
    status = EXCLUDED.status,
    updated_at = now();

CREATE TEMP TABLE __seed_drivers ON COMMIT DROP AS
SELECT
    driver_no,
    (
        SUBSTR(MD5('aresdrive-driver-' || driver_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-driver-' || driver_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-driver-' || driver_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-driver-' || driver_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-driver-' || driver_no), 21, 12)
    )::UUID AS driver_id,
    (
        SUBSTR(MD5('aresdrive-driver-user-' || driver_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-driver-user-' || driver_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-driver-user-' || driver_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-driver-user-' || driver_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-driver-user-' || driver_no), 21, 12)
    )::UUID AS user_id,
    'Seed Driver ' || LPAD(driver_no::TEXT, 3, '0') AS full_name,
    '+8477' || LPAD(driver_no::TEXT, 7, '0') AS phone,
    'B2-' || LPAD(driver_no::TEXT, 7, '0') AS license_number,
    CASE driver_no % 3 WHEN 0 THEN 'D' WHEN 1 THEN 'B2' ELSE 'C' END AS license_class,
    CASE driver_no % 10 WHEN 0 THEN 'OFF_DUTY' WHEN 1 THEN 'ON_TRIP' ELSE 'AVAILABLE' END AS status
FROM generate_series(1, 30) AS series(driver_no);

INSERT INTO identity.users (
    id,
    external_subject,
    phone,
    email,
    password_hash,
    display_name,
    status,
    last_login_at,
    created_at,
    updated_at
)
SELECT
    user_id,
    'seed-driver-' || LPAD(driver_no::TEXT, 3, '0'),
    phone,
    'driver' || LPAD(driver_no::TEXT, 3, '0') || '@aresdrive.test',
    '$2a$10$seeded.password.hash.for.local.dev.only',
    full_name,
    'ACTIVE',
    now() - (driver_no * INTERVAL '45 minutes'),
    now() - INTERVAL '120 days',
    now()
FROM __seed_drivers
ON CONFLICT (id) DO UPDATE SET
    external_subject = EXCLUDED.external_subject,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    display_name = EXCLUDED.display_name,
    status = EXCLUDED.status,
    last_login_at = EXCLUDED.last_login_at,
    updated_at = now();

INSERT INTO identity.user_roles (user_id, role_id)
SELECT drivers.user_id, roles.id
FROM __seed_drivers drivers
JOIN identity.roles roles ON roles.code = 'DRIVER'
ON CONFLICT (user_id, role_id) DO NOTHING;

INSERT INTO driver.drivers (
    id,
    user_id,
    driver_code,
    full_name,
    phone,
    license_number,
    license_class,
    license_expiry_date,
    years_of_experience,
    rating_average,
    rating_count,
    status,
    created_at,
    updated_at
)
SELECT
    driver_id,
    user_id,
    'DRV-' || LPAD(driver_no::TEXT, 4, '0'),
    full_name,
    phone,
    license_number,
    license_class,
    (CURRENT_DATE + ((driver_no % 48) + 12) * INTERVAL '1 month')::DATE,
    1 + (driver_no % 12),
    ROUND((3.9 + ((driver_no % 10) * 0.1))::NUMERIC, 2),
    8 + (driver_no * 3),
    status,
    now() - INTERVAL '120 days',
    now()
FROM __seed_drivers
ON CONFLICT (id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    driver_code = EXCLUDED.driver_code,
    full_name = EXCLUDED.full_name,
    phone = EXCLUDED.phone,
    license_number = EXCLUDED.license_number,
    license_class = EXCLUDED.license_class,
    license_expiry_date = EXCLUDED.license_expiry_date,
    years_of_experience = EXCLUDED.years_of_experience,
    rating_average = EXCLUDED.rating_average,
    rating_count = EXCLUDED.rating_count,
    status = EXCLUDED.status,
    updated_at = now();

DELETE FROM driver.driver_documents documents
USING __seed_drivers seeded
WHERE documents.driver_id = seeded.driver_id
  AND documents.file_url LIKE 'https://cdn.aresdrive.test/seed/drivers/%';

INSERT INTO driver.driver_documents (
    driver_id,
    document_type,
    file_url,
    expires_at,
    created_at
)
SELECT
    seeded.driver_id,
    document_type,
    'https://cdn.aresdrive.test/seed/drivers/driver-' ||
        LPAD(seeded.driver_no::TEXT, 3, '0') || '-' || LOWER(document_type) || '.jpg',
    (CURRENT_DATE + INTERVAL '3 years')::DATE,
    now() - INTERVAL '90 days'
FROM __seed_drivers seeded
CROSS JOIN (VALUES ('LICENSE_FRONT'), ('LICENSE_BACK'), ('PORTRAIT')) AS document_types(document_type);

DELETE FROM driver.availability_slots slots
USING __seed_drivers seeded
WHERE slots.driver_id = seeded.driver_id
  AND slots.note LIKE 'seed:%';

INSERT INTO driver.availability_slots (
    driver_id,
    start_at,
    end_at,
    status,
    note,
    created_at
)
SELECT
    seeded.driver_id,
    now() + (slot_no * INTERVAL '1 day') + INTERVAL '8 hours',
    now() + (slot_no * INTERVAL '1 day') + INTERVAL '18 hours',
    CASE (seeded.driver_no + slot_no) % 8 WHEN 0 THEN 'UNAVAILABLE' ELSE 'AVAILABLE' END,
    'seed: driver availability slot',
    now()
FROM __seed_drivers seeded
CROSS JOIN generate_series(1, 14) AS slots(slot_no);

INSERT INTO pricing.promotions (
    id,
    code,
    name,
    discount_type,
    discount_value,
    max_discount_amount,
    min_booking_amount,
    usage_limit,
    used_count,
    valid_from,
    valid_to,
    status,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-promotion-' || promotion_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-' || promotion_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-' || promotion_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-' || promotion_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-' || promotion_no), 21, 12)
    )::UUID,
    code,
    name,
    discount_type,
    discount_value,
    max_discount_amount,
    min_booking_amount,
    usage_limit,
    used_count,
    now() - INTERVAL '15 days',
    now() + INTERVAL '90 days',
    status,
    now() - INTERVAL '15 days',
    now()
FROM (VALUES
    (1, 'SEED10', 'Seed 10 percent off', 'PERCENT', 10, 150000, 500000, 1000, 42, 'ACTIVE'),
    (2, 'WEEKEND50', 'Seed weekend fixed discount', 'FIXED', 50000, 50000, 400000, 500, 18, 'ACTIVE'),
    (3, 'ELECTRIC15', 'Seed electric vehicle discount', 'PERCENT', 15, 200000, 600000, 300, 31, 'ACTIVE'),
    (4, 'HOSTNEW', 'Seed host launch promo', 'FIXED', 120000, 120000, 700000, 200, 12, 'ACTIVE'),
    (5, 'EXPIRED20', 'Seed expired promo state', 'PERCENT', 20, 250000, 800000, 100, 100, 'EXPIRED')
) AS promotions(promotion_no, code, name, discount_type, discount_value, max_discount_amount, min_booking_amount, usage_limit, used_count, status)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    discount_type = EXCLUDED.discount_type,
    discount_value = EXCLUDED.discount_value,
    max_discount_amount = EXCLUDED.max_discount_amount,
    min_booking_amount = EXCLUDED.min_booking_amount,
    usage_limit = EXCLUDED.usage_limit,
    used_count = EXCLUDED.used_count,
    valid_from = EXCLUDED.valid_from,
    valid_to = EXCLUDED.valid_to,
    status = EXCLUDED.status,
    updated_at = now();

INSERT INTO notification.templates (
    id,
    code,
    channel,
    subject_template,
    body_template,
    status,
    created_at,
    updated_at
)
VALUES
    ('00000000-0000-0000-0000-000000000301', 'BOOKING_CONFIRMED', 'EMAIL', 'Booking {{booking_code}} confirmed', 'Your booking {{booking_code}} is confirmed.', 'ACTIVE', now(), now()),
    ('00000000-0000-0000-0000-000000000302', 'VEHICLE_REVIEW_REQUIRED', 'IN_APP', 'Vehicle review required', 'A vehicle listing needs admin review.', 'ACTIVE', now(), now()),
    ('00000000-0000-0000-0000-000000000303', 'PAYMENT_SUCCEEDED', 'PUSH', 'Payment succeeded', 'Payment for {{booking_code}} succeeded.', 'ACTIVE', now(), now())
ON CONFLICT (code) DO UPDATE SET
    channel = EXCLUDED.channel,
    subject_template = EXCLUDED.subject_template,
    body_template = EXCLUDED.body_template,
    status = EXCLUDED.status,
    updated_at = now();

CREATE TEMP TABLE __seed_vehicle_data ON COMMIT DROP AS
WITH vehicle_templates AS (
    SELECT *
    FROM (VALUES
        (1, 'Toyota', 'Vios', 'E MT', 5, 'MANUAL', 'GASOLINE', 720000),
        (2, 'Toyota', 'Fortuner', '2.7V 4x2', 7, 'AUTOMATIC', 'GASOLINE', 1650000),
        (3, 'Toyota', 'Innova', '2.0G', 8, 'AUTOMATIC', 'GASOLINE', 1250000),
        (4, 'Honda', 'City', 'RS', 5, 'AUTOMATIC', 'GASOLINE', 820000),
        (5, 'Honda', 'CR-V', 'L', 7, 'AUTOMATIC', 'GASOLINE', 1550000),
        (6, 'Hyundai', 'Accent', '1.4 MT', 5, 'MANUAL', 'GASOLINE', 700000),
        (7, 'Hyundai', 'Santa Fe', 'Calligraphy', 7, 'AUTOMATIC', 'DIESEL', 1700000),
        (8, 'Kia', 'Seltos', 'Premium', 5, 'AUTOMATIC', 'GASOLINE', 980000),
        (9, 'Kia', 'Carnival', 'Signature', 7, 'AUTOMATIC', 'DIESEL', 2100000),
        (10, 'Mazda', 'Mazda 3', 'Luxury', 5, 'AUTOMATIC', 'GASOLINE', 900000),
        (11, 'Mazda', 'CX-5', 'Premium', 5, 'AUTOMATIC', 'GASOLINE', 1350000),
        (12, 'Mitsubishi', 'Xpander', 'AT', 7, 'AUTOMATIC', 'GASOLINE', 1050000),
        (13, 'Ford', 'Everest', 'Titanium', 7, 'AUTOMATIC', 'DIESEL', 1850000),
        (14, 'Ford', 'Ranger', 'XLS MT', 5, 'MANUAL', 'DIESEL', 1150000),
        (15, 'VinFast', 'VF e34', 'Plus', 5, 'AUTOMATIC', 'ELECTRIC', 950000),
        (16, 'VinFast', 'VF5', 'Plus', 5, 'AUTOMATIC', 'ELECTRIC', 780000),
        (17, 'VinFast', 'VF8', 'Plus', 5, 'AUTOMATIC', 'ELECTRIC', 1500000),
        (18, 'Mercedes-Benz', 'C-Class', 'C200 Avantgarde', 5, 'AUTOMATIC', 'GASOLINE', 2600000),
        (19, 'BMW', '3 Series', '320i Sport Line', 5, 'AUTOMATIC', 'GASOLINE', 2450000),
        (20, 'Audi', 'Q5', 'S line', 5, 'AUTOMATIC', 'HYBRID', 2800000),
        (21, 'Nissan', 'Almera', 'CVT', 5, 'AUTOMATIC', 'GASOLINE', 760000),
        (22, 'Suzuki', 'XL7', 'Hybrid', 7, 'AUTOMATIC', 'HYBRID', 960000),
        (23, 'Peugeot', '3008', 'GT', 5, 'AUTOMATIC', 'GASOLINE', 1500000),
        (24, 'MG', 'ZS', 'Lux', 5, 'AUTOMATIC', 'GASOLINE', 820000),
        (25, 'Isuzu', 'mu-X', 'Prestige', 7, 'AUTOMATIC', 'DIESEL', 1450000),
        (26, 'Lexus', 'RX', '350 Premium', 5, 'AUTOMATIC', 'HYBRID', 4200000),
        (27, 'Subaru', 'Forester', 'i-S EyeSight', 5, 'AUTOMATIC', 'GASOLINE', 1450000),
        (28, 'Volkswagen', 'Teramont', 'Luxury', 7, 'AUTOMATIC', 'GASOLINE', 2600000),
        (29, 'Porsche', 'Macan', 'Base', 5, 'AUTOMATIC', 'GASOLINE', 5200000),
        (30, 'Mini', 'Cooper', 'S', 4, 'AUTOMATIC', 'GASOLINE', 2200000)
    ) AS templates(template_no, brand, model, version, seats, transmission, fuel_type, base_rate)
),
locations AS (
    SELECT *
    FROM (VALUES
        (0, 'Ho Chi Minh', 'Quan 1', '12 Nguyen Hue, Quan 1', 10.7769000, 106.7009000, 90000),
        (1, 'Ho Chi Minh', 'Quan 7', '88 Nguyen Van Linh, Quan 7', 10.7290000, 106.7218000, 70000),
        (2, 'Ho Chi Minh', 'Thu Duc', '25 Vo Van Ngan, Thu Duc', 10.8494000, 106.7716000, 50000),
        (3, 'Ha Noi', 'Hoan Kiem', '18 Trang Tien, Hoan Kiem', 21.0245000, 105.8572000, 80000),
        (4, 'Ha Noi', 'Cau Giay', '110 Cau Giay, Cau Giay', 21.0362000, 105.7906000, 60000),
        (5, 'Da Nang', 'Hai Chau', '30 Bach Dang, Hai Chau', 16.0678000, 108.2208000, 50000),
        (6, 'Da Nang', 'Son Tra', '22 Vo Nguyen Giap, Son Tra', 16.0955000, 108.2484000, 65000),
        (7, 'Nha Trang', 'Loc Tho', '44 Tran Phu, Loc Tho', 12.2388000, 109.1967000, 70000),
        (8, 'Da Lat', 'Phuong 1', '9 Tran Quoc Toan, Phuong 1', 11.9404000, 108.4583000, 75000),
        (9, 'Can Tho', 'Ninh Kieu', '3 Hoa Binh, Ninh Kieu', 10.0452000, 105.7469000, 45000),
        (10, 'Phu Quoc', 'Duong Dong', '66 Tran Hung Dao, Duong Dong', 10.2138000, 103.9654000, 90000),
        (11, 'Hai Phong', 'Ngo Quyen', '17 Le Hong Phong, Ngo Quyen', 20.8449000, 106.6881000, 55000)
    ) AS city_data(location_no, city, district, pickup_address, latitude, longitude, location_rate_adjustment)
),
generated AS (
    SELECT
        ((batch_no - 1) * 30 + template_no) AS seed_no,
        batch_no,
        template_no,
        brand,
        model,
        version,
        seats,
        transmission,
        fuel_type,
        base_rate,
        city,
        district,
        pickup_address,
        latitude,
        longitude,
        location_rate_adjustment
    FROM vehicle_templates
    CROSS JOIN generate_series(1, 10) AS batches(batch_no)
    JOIN locations ON location_no = (((batch_no - 1) * 30 + template_no) % 12)
)
SELECT
    seed_no,
    CASE
        WHEN seed_no % 3 = 0 THEN 'COMPANY_OWNED'
        ELSE 'HOST_OWNED'
    END AS source,
    CASE
        WHEN seed_no % 3 = 0 THEN NULL::UUID
        ELSE (
            SUBSTR(MD5('aresdrive-owner-customer-' || ((seed_no % 75) + 1)), 1, 8) || '-' ||
            SUBSTR(MD5('aresdrive-owner-customer-' || ((seed_no % 75) + 1)), 9, 4) || '-' ||
            SUBSTR(MD5('aresdrive-owner-customer-' || ((seed_no % 75) + 1)), 13, 4) || '-' ||
            SUBSTR(MD5('aresdrive-owner-customer-' || ((seed_no % 75) + 1)), 17, 4) || '-' ||
            SUBSTR(MD5('aresdrive-owner-customer-' || ((seed_no % 75) + 1)), 21, 12)
        )::UUID
    END AS owner_customer_id,
    CASE
        WHEN seed_no % 3 <> 0 THEN NULL::UUID
        ELSE (
            SUBSTR(MD5('aresdrive-fleet-vehicle-' || seed_no), 1, 8) || '-' ||
            SUBSTR(MD5('aresdrive-fleet-vehicle-' || seed_no), 9, 4) || '-' ||
            SUBSTR(MD5('aresdrive-fleet-vehicle-' || seed_no), 13, 4) || '-' ||
            SUBSTR(MD5('aresdrive-fleet-vehicle-' || seed_no), 17, 4) || '-' ||
            SUBSTR(MD5('aresdrive-fleet-vehicle-' || seed_no), 21, 12)
        )::UUID
    END AS fleet_vehicle_id,
    brand,
    model,
    version,
    2018 + (seed_no % 8) AS manufacture_year,
    '51T-' || LPAD((100 + seed_no)::TEXT, 3, '0') || '.' || LPAD((seed_no % 100)::TEXT, 2, '0') AS license_plate,
    seats,
    transmission,
    fuel_type,
    CASE seed_no % 12
        WHEN 0 THEN 'SUSPENDED'
        WHEN 1 THEN 'DRAFT'
        WHEN 2 THEN 'PENDING_REVIEW'
        WHEN 3 THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END AS status,
    city,
    district,
    pickup_address,
    latitude + ((seed_no % 7) * 0.0010) AS latitude,
    longitude + ((seed_no % 5) * 0.0010) AS longitude,
    (base_rate + location_rate_adjustment + (batch_no * 35000) + ((seed_no % 5) * 25000))::NUMERIC(14, 2) AS base_daily_rate,
    (seed_no % 2 = 0) AS instant_booking_enabled,
    (seed_no % 4 IN (0, 1)) AS delivery_enabled,
    CASE
        WHEN seed_no % 12 = 0 THEN 'REJECTED'
        WHEN seed_no % 12 IN (1, 2) THEN 'DRAFT'
        WHEN seed_no % 12 = 3 THEN 'PAUSED'
        ELSE 'PUBLISHED'
    END AS listing_status,
    CASE
        WHEN seed_no % 12 IN (0, 1, 2) THEN NULL::TIMESTAMPTZ
        ELSE now() - ((seed_no * 13) * INTERVAL '1 minute')
    END AS published_at,
    now() - ((seed_no * 17) * INTERVAL '1 minute') AS created_at,
    now() - ((seed_no * 11) * INTERVAL '1 minute') AS updated_at
FROM generated;

INSERT INTO vehicle.vehicles (
    id,
    owner_customer_id,
    fleet_vehicle_id,
    source,
    brand,
    model,
    version,
    manufacture_year,
    license_plate,
    seats,
    transmission,
    fuel_type,
    status,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-vehicle-' || license_plate), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-vehicle-' || license_plate), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-vehicle-' || license_plate), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-vehicle-' || license_plate), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-vehicle-' || license_plate), 21, 12)
    )::UUID,
    owner_customer_id,
    fleet_vehicle_id,
    source,
    brand,
    model,
    version,
    manufacture_year,
    license_plate,
    seats,
    transmission,
    fuel_type,
    status,
    created_at,
    updated_at
FROM __seed_vehicle_data
ORDER BY seed_no
ON CONFLICT (license_plate) DO UPDATE SET
    owner_customer_id = EXCLUDED.owner_customer_id,
    fleet_vehicle_id = EXCLUDED.fleet_vehicle_id,
    source = EXCLUDED.source,
    brand = EXCLUDED.brand,
    model = EXCLUDED.model,
    version = EXCLUDED.version,
    manufacture_year = EXCLUDED.manufacture_year,
    seats = EXCLUDED.seats,
    transmission = EXCLUDED.transmission,
    fuel_type = EXCLUDED.fuel_type,
    status = EXCLUDED.status,
    updated_at = now();

CREATE TEMP TABLE __seed_vehicle_ids ON COMMIT DROP AS
SELECT
    data.*,
    vehicles.id AS vehicle_id
FROM __seed_vehicle_data data
JOIN vehicle.vehicles vehicles ON vehicles.license_plate = data.license_plate;

INSERT INTO fleet.company_vehicles (
    id,
    vehicle_id,
    branch_id,
    asset_code,
    vin,
    purchase_date,
    purchase_price,
    current_odometer_km,
    asset_status,
    next_maintenance_at,
    created_at,
    updated_at
)
SELECT
    seeded.fleet_vehicle_id,
    seeded.vehicle_id,
    (
        SUBSTR(MD5('aresdrive-branch-' || ((seeded.seed_no % 6) + 1)), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-branch-' || ((seeded.seed_no % 6) + 1)), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-branch-' || ((seeded.seed_no % 6) + 1)), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-branch-' || ((seeded.seed_no % 6) + 1)), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-branch-' || ((seeded.seed_no % 6) + 1)), 21, 12)
    )::UUID,
    'ASSET-' || LPAD(seeded.seed_no::TEXT, 5, '0'),
    'VINSEED' || LPAD(seeded.seed_no::TEXT, 10, '0'),
    (CURRENT_DATE - ((seeded.seed_no % 1800) * INTERVAL '1 day'))::DATE,
    (650000000 + (seeded.seed_no * 3300000))::NUMERIC(14, 2),
    5000 + (seeded.seed_no * 137 % 90000),
    CASE seeded.seed_no % 10
        WHEN 0 THEN 'MAINTENANCE'
        WHEN 1 THEN 'RENTED'
        WHEN 2 THEN 'RETIRED'
        ELSE 'AVAILABLE'
    END,
    (CURRENT_DATE + ((seeded.seed_no % 120) * INTERVAL '1 day'))::DATE,
    seeded.created_at,
    now()
FROM __seed_vehicle_ids seeded
WHERE seeded.source = 'COMPANY_OWNED'
ON CONFLICT (vehicle_id) DO UPDATE SET
    id = EXCLUDED.id,
    branch_id = EXCLUDED.branch_id,
    asset_code = EXCLUDED.asset_code,
    vin = EXCLUDED.vin,
    purchase_date = EXCLUDED.purchase_date,
    purchase_price = EXCLUDED.purchase_price,
    current_odometer_km = EXCLUDED.current_odometer_km,
    asset_status = EXCLUDED.asset_status,
    next_maintenance_at = EXCLUDED.next_maintenance_at,
    updated_at = now();

DELETE FROM fleet.maintenance_records records
USING fleet.company_vehicles company
JOIN __seed_vehicle_ids seeded ON seeded.fleet_vehicle_id = company.id
WHERE records.company_vehicle_id = company.id
  AND records.note LIKE 'seed:%';

INSERT INTO fleet.maintenance_records (
    company_vehicle_id,
    maintenance_type,
    odometer_km,
    vendor_name,
    cost_amount,
    note,
    started_at,
    completed_at,
    created_at
)
SELECT
    seeded.fleet_vehicle_id,
    maintenance_type,
    5000 + (seeded.seed_no * 137 % 90000),
    CASE maintenance_type
        WHEN 'CLEANING' THEN 'Seed Auto Spa'
        WHEN 'INSPECTION' THEN 'Seed Inspection Center'
        ELSE 'Seed Garage Partner'
    END,
    CASE maintenance_type
        WHEN 'CLEANING' THEN 250000
        WHEN 'INSPECTION' THEN 650000
        WHEN 'REPAIR' THEN 3200000
        ELSE 1200000
    END,
    'seed: fleet maintenance record',
    now() - ((seeded.seed_no % 60) * INTERVAL '1 day'),
    CASE maintenance_type WHEN 'REPAIR' THEN NULL ELSE now() - (((seeded.seed_no % 60) - 1) * INTERVAL '1 day') END,
    now()
FROM __seed_vehicle_ids seeded
CROSS JOIN LATERAL (VALUES
    ('PERIODIC', true),
    ('CLEANING', seeded.seed_no % 2 = 0),
    ('INSPECTION', seeded.seed_no % 5 = 0),
    ('REPAIR', seeded.seed_no % 11 = 0)
) AS maintenance(maintenance_type, enabled)
WHERE seeded.source = 'COMPANY_OWNED'
  AND maintenance.enabled;

DELETE FROM fleet.insurance_policies policies
USING fleet.company_vehicles company
JOIN __seed_vehicle_ids seeded ON seeded.fleet_vehicle_id = company.id
WHERE policies.company_vehicle_id = company.id
  AND policies.file_url LIKE 'https://cdn.aresdrive.test/seed/insurance/%';

INSERT INTO fleet.insurance_policies (
    company_vehicle_id,
    provider_name,
    policy_number,
    coverage_type,
    valid_from,
    valid_to,
    file_url,
    created_at
)
SELECT
    seeded.fleet_vehicle_id,
    CASE seeded.seed_no % 3 WHEN 0 THEN 'Bao Viet' WHEN 1 THEN 'PVI' ELSE 'PTI' END,
    'POL-SEED-' || LPAD(seeded.seed_no::TEXT, 6, '0'),
    CASE seeded.seed_no % 2 WHEN 0 THEN 'PHYSICAL_DAMAGE' ELSE 'THIRD_PARTY_LIABILITY' END,
    (CURRENT_DATE - INTERVAL '30 days')::DATE,
    (CURRENT_DATE + INTERVAL '335 days')::DATE,
    'https://cdn.aresdrive.test/seed/insurance/policy-' || LPAD(seeded.seed_no::TEXT, 4, '0') || '.pdf',
    now()
FROM __seed_vehicle_ids seeded
WHERE seeded.source = 'COMPANY_OWNED';

DELETE FROM fleet.inspection_records inspections
USING fleet.company_vehicles company
JOIN __seed_vehicle_ids seeded ON seeded.fleet_vehicle_id = company.id
WHERE inspections.company_vehicle_id = company.id
  AND inspections.file_url LIKE 'https://cdn.aresdrive.test/seed/inspection/%';

INSERT INTO fleet.inspection_records (
    company_vehicle_id,
    inspection_date,
    valid_until,
    result,
    file_url,
    note,
    created_at
)
SELECT
    seeded.fleet_vehicle_id,
    (CURRENT_DATE - ((seeded.seed_no % 120) * INTERVAL '1 day'))::DATE,
    (CURRENT_DATE + ((seeded.seed_no % 240) * INTERVAL '1 day') + INTERVAL '180 days')::DATE,
    CASE seeded.seed_no % 13 WHEN 0 THEN 'CONDITIONAL' WHEN 1 THEN 'FAILED' ELSE 'PASSED' END,
    'https://cdn.aresdrive.test/seed/inspection/inspection-' || LPAD(seeded.seed_no::TEXT, 4, '0') || '.pdf',
    'seed: fleet inspection record',
    now()
FROM __seed_vehicle_ids seeded
WHERE seeded.source = 'COMPANY_OWNED';

INSERT INTO pricing.price_plans (
    id,
    target_type,
    target_id,
    name,
    currency,
    base_daily_rate,
    hourly_rate,
    weekend_multiplier,
    deposit_amount,
    status,
    valid_from,
    valid_to,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-price-plan-' || seeded.vehicle_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-price-plan-' || seeded.vehicle_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-price-plan-' || seeded.vehicle_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-price-plan-' || seeded.vehicle_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-price-plan-' || seeded.vehicle_id), 21, 12)
    )::UUID,
    'VEHICLE',
    seeded.vehicle_id,
    'Seed daily plan - ' || seeded.brand || ' ' || seeded.model,
    'VND',
    seeded.base_daily_rate,
    ROUND((seeded.base_daily_rate / 10)::NUMERIC, 2),
    CASE seeded.seed_no % 4 WHEN 0 THEN 1.25 WHEN 1 THEN 1.15 ELSE 1.10 END,
    ROUND((seeded.base_daily_rate * 0.5)::NUMERIC, 2),
    CASE seeded.status WHEN 'ACTIVE' THEN 'ACTIVE' ELSE 'INACTIVE' END,
    now() - INTERVAL '30 days',
    now() + INTERVAL '365 days',
    seeded.created_at,
    now()
FROM __seed_vehicle_ids seeded
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    base_daily_rate = EXCLUDED.base_daily_rate,
    hourly_rate = EXCLUDED.hourly_rate,
    weekend_multiplier = EXCLUDED.weekend_multiplier,
    deposit_amount = EXCLUDED.deposit_amount,
    status = EXCLUDED.status,
    valid_from = EXCLUDED.valid_from,
    valid_to = EXCLUDED.valid_to,
    updated_at = now();

CREATE TEMP TABLE __seed_booking_data ON COMMIT DROP AS
WITH base AS (
    SELECT
        seeded.*,
        renter.customer_id AS renter_customer_id,
        renter.user_id AS renter_user_id,
        CASE
            WHEN seeded.source = 'COMPANY_OWNED' AND seeded.seed_no % 5 = 0 THEN (
                SUBSTR(MD5('aresdrive-driver-' || ((seeded.seed_no % 30) + 1)), 1, 8) || '-' ||
                SUBSTR(MD5('aresdrive-driver-' || ((seeded.seed_no % 30) + 1)), 9, 4) || '-' ||
                SUBSTR(MD5('aresdrive-driver-' || ((seeded.seed_no % 30) + 1)), 13, 4) || '-' ||
                SUBSTR(MD5('aresdrive-driver-' || ((seeded.seed_no % 30) + 1)), 17, 4) || '-' ||
                SUBSTR(MD5('aresdrive-driver-' || ((seeded.seed_no % 30) + 1)), 21, 12)
            )::UUID
            ELSE NULL::UUID
        END AS driver_id,
        now() + ((seeded.seed_no % 45) * INTERVAL '1 day') + INTERVAL '9 hours' AS start_at,
        now() + (((seeded.seed_no % 45) + 2 + (seeded.seed_no % 3)) * INTERVAL '1 day') + INTERVAL '9 hours' AS end_at
    FROM __seed_vehicle_ids seeded
    JOIN __seed_customers renter ON renter.customer_no = 76 + (seeded.seed_no % 45)
),
amounts AS (
    SELECT
        base.*,
        GREATEST(1, CEIL(EXTRACT(EPOCH FROM (end_at - start_at)) / 86400.0))::INT AS rental_days,
        (base_daily_rate * GREATEST(1, CEIL(EXTRACT(EPOCH FROM (end_at - start_at)) / 86400.0)))::NUMERIC(14, 2) AS subtotal_amount,
        CASE WHEN seed_no % 7 = 0 THEN 100000 ELSE 0 END::NUMERIC(14, 2) AS discount_amount,
        (
            CASE WHEN delivery_enabled THEN 80000 ELSE 0 END +
            CASE WHEN driver_id IS NOT NULL THEN 450000 ELSE 0 END
        )::NUMERIC(14, 2) AS fee_amount,
        ROUND((base_daily_rate * 0.5)::NUMERIC, 2) AS deposit_amount
    FROM base
)
SELECT
    seed_no,
    vehicle_id,
    owner_customer_id AS host_customer_id,
    renter_customer_id AS customer_id,
    renter_user_id,
    driver_id,
    (
        SUBSTR(MD5('aresdrive-quote-' || seed_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-quote-' || seed_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-quote-' || seed_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-quote-' || seed_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-quote-' || seed_no), 21, 12)
    )::UUID AS quote_id,
    (
        SUBSTR(MD5('aresdrive-booking-' || seed_no), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-booking-' || seed_no), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-booking-' || seed_no), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-booking-' || seed_no), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-booking-' || seed_no), 21, 12)
    )::UUID AS booking_id,
    'BK-SEED-' || LPAD(seed_no::TEXT, 6, '0') AS booking_code,
    CASE WHEN driver_id IS NULL THEN 'SELF_DRIVE' ELSE 'WITH_DRIVER' END AS service_type,
    source AS vehicle_source,
    pickup_address,
    pickup_address AS return_address,
    start_at,
    end_at,
    'VND' AS currency,
    subtotal_amount,
    discount_amount,
    fee_amount,
    deposit_amount,
    (subtotal_amount - discount_amount + fee_amount + deposit_amount)::NUMERIC(14, 2) AS total_amount,
    CASE seed_no % 9
        WHEN 0 THEN 'CANCELLED'
        WHEN 1 THEN 'PENDING_PAYMENT'
        WHEN 2 THEN 'PENDING_APPROVAL'
        WHEN 3 THEN 'CONFIRMED'
        WHEN 4 THEN 'IN_PROGRESS'
        WHEN 5 THEN 'COMPLETED'
        WHEN 6 THEN 'COMPLETED'
        WHEN 7 THEN 'EXPIRED'
        ELSE 'CONFIRMED'
    END AS booking_status,
    jsonb_build_object(
        'id', vehicle_id,
        'source', source,
        'brand', brand,
        'model', model,
        'version', version,
        'licensePlate', license_plate,
        'fuelType', fuel_type,
        'transmission', transmission,
        'seats', seats
    ) AS vehicle_snapshot,
    jsonb_build_object(
        'id', renter_customer_id,
        'displayName', 'Seed Customer ' || LPAD((76 + (seed_no % 45))::TEXT, 3, '0'),
        'phone', '+8490' || LPAD((76 + (seed_no % 45))::TEXT, 7, '0')
    ) AS customer_snapshot,
    CASE
        WHEN driver_id IS NULL THEN '{}'::jsonb
        ELSE jsonb_build_object('id', driver_id, 'displayName', 'Seed Driver ' || LPAD(((seed_no % 30) + 1)::TEXT, 3, '0'))
    END AS driver_snapshot,
    created_at,
    updated_at
FROM amounts;

INSERT INTO pricing.quotes (
    id,
    customer_id,
    vehicle_id,
    driver_id,
    service_type,
    start_at,
    end_at,
    currency,
    subtotal_amount,
    discount_amount,
    fee_amount,
    deposit_amount,
    total_amount,
    expires_at,
    created_at
)
SELECT
    quote_id,
    customer_id,
    vehicle_id,
    driver_id,
    service_type,
    start_at,
    end_at,
    currency,
    subtotal_amount,
    discount_amount,
    fee_amount,
    deposit_amount,
    total_amount,
    now() + INTERVAL '30 minutes',
    created_at
FROM __seed_booking_data
ON CONFLICT (id) DO UPDATE SET
    customer_id = EXCLUDED.customer_id,
    vehicle_id = EXCLUDED.vehicle_id,
    driver_id = EXCLUDED.driver_id,
    service_type = EXCLUDED.service_type,
    start_at = EXCLUDED.start_at,
    end_at = EXCLUDED.end_at,
    subtotal_amount = EXCLUDED.subtotal_amount,
    discount_amount = EXCLUDED.discount_amount,
    fee_amount = EXCLUDED.fee_amount,
    deposit_amount = EXCLUDED.deposit_amount,
    total_amount = EXCLUDED.total_amount,
    expires_at = EXCLUDED.expires_at;

DELETE FROM pricing.quote_items items
USING __seed_booking_data seeded
WHERE items.quote_id = seeded.quote_id;

INSERT INTO pricing.quote_items (
    quote_id,
    item_type,
    description,
    quantity,
    unit_amount,
    total_amount,
    created_at
)
SELECT
    quote_id,
    item_type,
    description,
    quantity,
    unit_amount,
    total_amount,
    created_at
FROM __seed_booking_data seeded
CROSS JOIN LATERAL (VALUES
    ('BASE_RENTAL', 'Vehicle rental base amount', EXTRACT(EPOCH FROM (seeded.end_at - seeded.start_at)) / 86400.0, seeded.subtotal_amount / GREATEST(1, CEIL(EXTRACT(EPOCH FROM (seeded.end_at - seeded.start_at)) / 86400.0)), seeded.subtotal_amount, true),
    ('DELIVERY_FEE', 'Vehicle delivery fee', 1, 80000, 80000, seeded.fee_amount >= 80000),
    ('DRIVER_FEE', 'Driver service fee', 1, 450000, 450000, seeded.driver_id IS NOT NULL),
    ('DISCOUNT', 'Seed promotion discount', 1, -seeded.discount_amount, -seeded.discount_amount, seeded.discount_amount > 0),
    ('DEPOSIT', 'Security deposit', 1, seeded.deposit_amount, seeded.deposit_amount, true)
) AS items(item_type, description, quantity, unit_amount, total_amount, enabled)
WHERE items.enabled;

INSERT INTO booking.bookings (
    id,
    booking_code,
    quote_id,
    customer_id,
    host_customer_id,
    vehicle_id,
    driver_id,
    service_type,
    vehicle_source,
    pickup_address,
    return_address,
    start_at,
    end_at,
    currency,
    subtotal_amount,
    discount_amount,
    fee_amount,
    deposit_amount,
    total_amount,
    status,
    vehicle_snapshot,
    customer_snapshot,
    driver_snapshot,
    note,
    created_at,
    updated_at
)
SELECT
    booking_id,
    booking_code,
    quote_id,
    customer_id,
    host_customer_id,
    vehicle_id,
    driver_id,
    service_type,
    vehicle_source,
    pickup_address,
    return_address,
    start_at,
    end_at,
    currency,
    subtotal_amount,
    discount_amount,
    fee_amount,
    deposit_amount,
    total_amount,
    booking_status,
    vehicle_snapshot,
    customer_snapshot,
    driver_snapshot,
    'seed: booking for vehicle UI testing',
    created_at,
    updated_at
FROM __seed_booking_data
ON CONFLICT (id) DO UPDATE SET
    booking_code = EXCLUDED.booking_code,
    quote_id = EXCLUDED.quote_id,
    customer_id = EXCLUDED.customer_id,
    host_customer_id = EXCLUDED.host_customer_id,
    vehicle_id = EXCLUDED.vehicle_id,
    driver_id = EXCLUDED.driver_id,
    service_type = EXCLUDED.service_type,
    vehicle_source = EXCLUDED.vehicle_source,
    pickup_address = EXCLUDED.pickup_address,
    return_address = EXCLUDED.return_address,
    start_at = EXCLUDED.start_at,
    end_at = EXCLUDED.end_at,
    subtotal_amount = EXCLUDED.subtotal_amount,
    discount_amount = EXCLUDED.discount_amount,
    fee_amount = EXCLUDED.fee_amount,
    deposit_amount = EXCLUDED.deposit_amount,
    total_amount = EXCLUDED.total_amount,
    status = EXCLUDED.status,
    vehicle_snapshot = EXCLUDED.vehicle_snapshot,
    customer_snapshot = EXCLUDED.customer_snapshot,
    driver_snapshot = EXCLUDED.driver_snapshot,
    note = EXCLUDED.note,
    updated_at = now();

DELETE FROM booking.booking_status_history history
USING __seed_booking_data seeded
WHERE history.booking_id = seeded.booking_id;

DELETE FROM booking.trip_checklists checklists
USING __seed_booking_data seeded
WHERE checklists.booking_id = seeded.booking_id;

DELETE FROM booking.booking_cancellations cancellations
USING __seed_booking_data seeded
WHERE cancellations.booking_id = seeded.booking_id;

INSERT INTO booking.booking_status_history (
    booking_id,
    from_status,
    to_status,
    changed_by,
    reason,
    created_at
)
SELECT
    booking_id,
    from_status,
    to_status,
    NULL::UUID,
    reason,
    created_at
FROM __seed_booking_data seeded
CROSS JOIN LATERAL (VALUES
    (NULL::VARCHAR, 'PENDING_PAYMENT', 'seed: booking created', seeded.created_at),
    ('PENDING_PAYMENT', seeded.booking_status, 'seed: booking reached current state', seeded.updated_at)
) AS history(from_status, to_status, reason, created_at)
WHERE history.to_status IS NOT NULL
  AND (history.from_status IS NULL OR history.to_status <> history.from_status);

INSERT INTO booking.trip_checklists (
    id,
    booking_id,
    checklist_type,
    odometer_km,
    fuel_percent,
    battery_percent,
    note,
    checked_by,
    checked_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-trip-checklist-' || seeded.booking_id || '-' || checklist_type), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-trip-checklist-' || seeded.booking_id || '-' || checklist_type), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-trip-checklist-' || seeded.booking_id || '-' || checklist_type), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-trip-checklist-' || seeded.booking_id || '-' || checklist_type), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-trip-checklist-' || seeded.booking_id || '-' || checklist_type), 21, 12)
    )::UUID,
    booking_id,
    checklist_type,
    10000 + (seed_no * 137 % 90000),
    CASE WHEN vehicle_snapshot ->> 'fuelType' = 'ELECTRIC' THEN NULL ELSE 40 + (seed_no % 55) END,
    CASE WHEN vehicle_snapshot ->> 'fuelType' = 'ELECTRIC' THEN 40 + (seed_no % 55) ELSE NULL END,
    'seed: trip checklist',
    customer_id,
    CASE checklist_type WHEN 'PICKUP' THEN start_at ELSE end_at END
FROM __seed_booking_data seeded
CROSS JOIN LATERAL (VALUES
    ('PICKUP', seeded.booking_status IN ('CONFIRMED', 'IN_PROGRESS', 'COMPLETED')),
    ('RETURN', seeded.booking_status = 'COMPLETED')
) AS checklist(checklist_type, enabled)
WHERE checklist.enabled;

INSERT INTO booking.trip_checklist_items (
    checklist_id,
    item_code,
    item_name,
    condition,
    note,
    image_url
)
SELECT
    checklists.id,
    item_code,
    item_name,
    CASE ((seeded.seed_no + item_no + CASE checklists.checklist_type WHEN 'RETURN' THEN 1 ELSE 0 END) % 12)
        WHEN 0 THEN 'DAMAGED'
        WHEN 1 THEN 'NOT_CHECKED'
        ELSE 'GOOD'
    END,
    'seed: checklist item',
    'https://cdn.aresdrive.test/seed/checklists/' || checklists.id || '-' || LOWER(item_code) || '.jpg'
FROM booking.trip_checklists checklists
JOIN __seed_booking_data seeded ON seeded.booking_id = checklists.booking_id
CROSS JOIN (VALUES
    (1, 'EXTERIOR', 'Exterior body'),
    (2, 'INTERIOR', 'Interior cabin'),
    (3, 'TIRES', 'Tires'),
    (4, 'LIGHTS', 'Lights'),
    (5, 'FUEL_OR_BATTERY', 'Fuel or battery level'),
    (6, 'DOCUMENTS', 'Vehicle documents')
) AS checklist_items(item_no, item_code, item_name);

INSERT INTO booking.booking_cancellations (
    id,
    booking_id,
    cancelled_by,
    cancelled_by_type,
    reason_code,
    reason_text,
    refund_amount,
    cancelled_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-cancellation-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-cancellation-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-cancellation-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-cancellation-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-cancellation-' || booking_id), 21, 12)
    )::UUID,
    booking_id,
    customer_id,
    CASE seed_no % 4 WHEN 0 THEN 'CUSTOMER' WHEN 1 THEN 'HOST' WHEN 2 THEN 'ADMIN' ELSE 'SYSTEM' END,
    'SEED_CANCELLED',
    'seed: cancellation for UI state testing',
    CASE WHEN total_amount > 200000 THEN 100000 ELSE 0 END,
    updated_at
FROM __seed_booking_data
WHERE booking_status = 'CANCELLED'
ON CONFLICT (booking_id) DO UPDATE SET
    cancelled_by = EXCLUDED.cancelled_by,
    cancelled_by_type = EXCLUDED.cancelled_by_type,
    reason_code = EXCLUDED.reason_code,
    reason_text = EXCLUDED.reason_text,
    refund_amount = EXCLUDED.refund_amount,
    cancelled_at = EXCLUDED.cancelled_at;

INSERT INTO driver.driver_assignments (
    id,
    driver_id,
    booking_id,
    vehicle_id,
    status,
    assigned_at,
    accepted_at,
    rejected_at,
    completed_at,
    note
)
SELECT
    (
        SUBSTR(MD5('aresdrive-driver-assignment-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-driver-assignment-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-driver-assignment-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-driver-assignment-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-driver-assignment-' || booking_id), 21, 12)
    )::UUID,
    driver_id,
    booking_id,
    vehicle_id,
    CASE booking_status
        WHEN 'COMPLETED' THEN 'COMPLETED'
        WHEN 'CANCELLED' THEN 'CANCELLED'
        WHEN 'IN_PROGRESS' THEN 'ACCEPTED'
        ELSE 'ASSIGNED'
    END,
    created_at,
    CASE WHEN booking_status IN ('CONFIRMED', 'IN_PROGRESS', 'COMPLETED') THEN created_at + INTERVAL '15 minutes' ELSE NULL END,
    CASE WHEN booking_status = 'CANCELLED' THEN updated_at ELSE NULL END,
    CASE WHEN booking_status = 'COMPLETED' THEN end_at ELSE NULL END,
    'seed: driver assignment'
FROM __seed_booking_data
WHERE driver_id IS NOT NULL
ON CONFLICT (id) DO UPDATE SET
    driver_id = EXCLUDED.driver_id,
    booking_id = EXCLUDED.booking_id,
    vehicle_id = EXCLUDED.vehicle_id,
    status = EXCLUDED.status,
    accepted_at = EXCLUDED.accepted_at,
    rejected_at = EXCLUDED.rejected_at,
    completed_at = EXCLUDED.completed_at,
    note = EXCLUDED.note;

INSERT INTO vehicle.vehicle_listings (
    vehicle_id,
    title,
    description,
    city,
    district,
    pickup_address,
    latitude,
    longitude,
    base_daily_rate,
    currency,
    instant_booking_enabled,
    delivery_enabled,
    status,
    published_at,
    created_at,
    updated_at
)
SELECT
    vehicle_id,
    brand || ' ' || model || ' ' || COALESCE(version, '') || ' - ' || city,
    'Seed listing #' || seed_no || ' for vehicle module testing. Source=' || source ||
        ', status=' || status || ', seats=' || seats || ', fuel=' || fuel_type || '.',
    city,
    district,
    pickup_address,
    latitude,
    longitude,
    base_daily_rate,
    'VND',
    instant_booking_enabled,
    delivery_enabled,
    listing_status,
    published_at,
    created_at,
    updated_at
FROM __seed_vehicle_ids
ON CONFLICT (vehicle_id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    city = EXCLUDED.city,
    district = EXCLUDED.district,
    pickup_address = EXCLUDED.pickup_address,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    base_daily_rate = EXCLUDED.base_daily_rate,
    currency = EXCLUDED.currency,
    instant_booking_enabled = EXCLUDED.instant_booking_enabled,
    delivery_enabled = EXCLUDED.delivery_enabled,
    status = EXCLUDED.status,
    published_at = EXCLUDED.published_at,
    updated_at = now();

DELETE FROM vehicle.vehicle_images images
USING __seed_vehicle_ids seeded
WHERE images.vehicle_id = seeded.vehicle_id
  AND images.file_url LIKE 'https://cdn.aresdrive.test/seed/%';

INSERT INTO vehicle.vehicle_images (
    vehicle_id,
    file_url,
    sort_order,
    is_cover,
    created_at
)
SELECT
    seeded.vehicle_id,
    'https://cdn.aresdrive.test/seed/vehicles/' ||
        LOWER(REPLACE(seeded.license_plate, '.', '-')) || '-' || image_data.slug || '.jpg',
    image_data.sort_order,
    image_data.sort_order = 1,
    seeded.created_at + (image_data.sort_order * INTERVAL '1 minute')
FROM __seed_vehicle_ids seeded
CROSS JOIN (VALUES
    (1, 'front'),
    (2, 'interior'),
    (3, 'rear'),
    (4, 'dashboard')
) AS image_data(sort_order, slug);

INSERT INTO vehicle.vehicle_features (
    vehicle_id,
    code,
    name,
    created_at
)
SELECT
    seeded.vehicle_id,
    feature.code,
    feature.name,
    seeded.created_at
FROM __seed_vehicle_ids seeded
CROSS JOIN LATERAL (VALUES
    ('AIR_CONDITIONING', 'Air conditioning', true),
    ('BLUETOOTH', 'Bluetooth', true),
    ('USB_CHARGER', 'USB charger', true),
    ('PHONE_HOLDER', 'Phone holder', seeded.seed_no % 2 = 0),
    ('REVERSE_CAMERA', 'Reverse camera', seeded.seed_no % 2 = 0),
    ('PARKING_SENSOR', 'Parking sensor', seeded.seed_no % 4 <> 1),
    ('GPS', 'GPS navigation', seeded.source = 'COMPANY_OWNED' OR seeded.seed_no % 3 = 0),
    ('CHILD_SEAT', 'Child seat', seeded.seats >= 7 OR seeded.seed_no % 5 = 0),
    ('SUNROOF', 'Sunroof', seeded.brand IN ('Mercedes-Benz', 'BMW', 'Audi', 'Lexus', 'Porsche', 'Mini')),
    ('FAST_CHARGING', 'Fast charging', seeded.fuel_type = 'ELECTRIC'),
    ('HYBRID_ECO', 'Hybrid eco mode', seeded.fuel_type = 'HYBRID'),
    ('CRUISE_CONTROL', 'Cruise control', seeded.transmission = 'AUTOMATIC'),
    ('DASH_CAMERA', 'Dash camera', seeded.seed_no % 3 <> 1),
    ('SPARE_TIRE', 'Spare tire', seeded.fuel_type <> 'ELECTRIC')
) AS feature(code, name, enabled)
WHERE feature.enabled
ON CONFLICT (vehicle_id, code) DO UPDATE SET
    name = EXCLUDED.name;

DELETE FROM vehicle.availability_blocks blocks
USING __seed_vehicle_ids seeded
WHERE blocks.vehicle_id = seeded.vehicle_id
  AND blocks.note LIKE 'seed:%';

INSERT INTO vehicle.availability_blocks (
    vehicle_id,
    start_at,
    end_at,
    reason,
    booking_id,
    note,
    created_at
)
SELECT
    seeded.vehicle_id,
    block_data.start_at,
    block_data.end_at,
    block_data.reason,
    block_data.booking_id,
    block_data.note,
    seeded.created_at
FROM __seed_vehicle_ids seeded
CROSS JOIN LATERAL (VALUES
    (
        now() + ((seeded.seed_no % 21) * INTERVAL '1 day'),
        now() + (((seeded.seed_no % 21) + 2) * INTERVAL '1 day'),
        'BOOKING',
        (
            SUBSTR(MD5('aresdrive-booking-' || seeded.seed_no), 1, 8) || '-' ||
            SUBSTR(MD5('aresdrive-booking-' || seeded.seed_no), 9, 4) || '-' ||
            SUBSTR(MD5('aresdrive-booking-' || seeded.seed_no), 13, 4) || '-' ||
            SUBSTR(MD5('aresdrive-booking-' || seeded.seed_no), 17, 4) || '-' ||
            SUBSTR(MD5('aresdrive-booking-' || seeded.seed_no), 21, 12)
        )::UUID,
        'seed: confirmed booking block',
        seeded.seed_no % 4 = 0
    ),
    (
        now() + (((seeded.seed_no % 30) + 10) * INTERVAL '1 day'),
        now() + (((seeded.seed_no % 30) + 11) * INTERVAL '1 day'),
        'MAINTENANCE',
        NULL::UUID,
        'seed: scheduled maintenance',
        seeded.seed_no % 6 = 0
    ),
    (
        now() + (((seeded.seed_no % 18) + 5) * INTERVAL '1 day'),
        now() + (((seeded.seed_no % 18) + 8) * INTERVAL '1 day'),
        'HOST_BLOCKED',
        NULL::UUID,
        'seed: host personal schedule',
        seeded.source = 'HOST_OWNED' AND seeded.seed_no % 7 = 0
    ),
    (
        now() + (((seeded.seed_no % 15) + 3) * INTERVAL '1 day'),
        now() + (((seeded.seed_no % 15) + 4) * INTERVAL '1 day'),
        'ADMIN_BLOCKED',
        NULL::UUID,
        'seed: admin quality review',
        seeded.seed_no % 13 = 0
    )
) AS block_data(start_at, end_at, reason, booking_id, note, enabled)
WHERE block_data.enabled;

INSERT INTO payment.payment_intents (
    id,
    booking_id,
    payer_customer_id,
    amount,
    currency,
    provider,
    provider_reference,
    status,
    expires_at,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-payment-intent-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-payment-intent-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-payment-intent-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-payment-intent-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-payment-intent-' || booking_id), 21, 12)
    )::UUID,
    booking_id,
    customer_id,
    total_amount,
    currency,
    CASE seed_no % 4 WHEN 0 THEN 'VNPAY' WHEN 1 THEN 'MOMO' WHEN 2 THEN 'BANK_TRANSFER' ELSE 'CASH' END,
    'PAY-SEED-' || LPAD(seed_no::TEXT, 6, '0'),
    CASE booking_status
        WHEN 'PENDING_PAYMENT' THEN 'PENDING'
        WHEN 'EXPIRED' THEN 'EXPIRED'
        ELSE 'SUCCEEDED'
    END,
    CASE booking_status WHEN 'PENDING_PAYMENT' THEN now() + INTERVAL '30 minutes' ELSE NULL END,
    created_at,
    now()
FROM __seed_booking_data
ON CONFLICT (id) DO UPDATE SET
    booking_id = EXCLUDED.booking_id,
    payer_customer_id = EXCLUDED.payer_customer_id,
    amount = EXCLUDED.amount,
    currency = EXCLUDED.currency,
    provider = EXCLUDED.provider,
    provider_reference = EXCLUDED.provider_reference,
    status = EXCLUDED.status,
    expires_at = EXCLUDED.expires_at,
    updated_at = now();

INSERT INTO payment.transactions (
    id,
    payment_intent_id,
    transaction_code,
    provider,
    provider_transaction_id,
    amount,
    currency,
    status,
    raw_payload,
    paid_at,
    created_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-transaction-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-transaction-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-transaction-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-transaction-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-transaction-' || booking_id), 21, 12)
    )::UUID,
    (
        SUBSTR(MD5('aresdrive-payment-intent-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-payment-intent-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-payment-intent-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-payment-intent-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-payment-intent-' || booking_id), 21, 12)
    )::UUID,
    'TRX-SEED-' || LPAD(seed_no::TEXT, 6, '0'),
    CASE seed_no % 4 WHEN 0 THEN 'VNPAY' WHEN 1 THEN 'MOMO' WHEN 2 THEN 'BANK_TRANSFER' ELSE 'CASH' END,
    'PROVIDER-SEED-' || LPAD(seed_no::TEXT, 6, '0'),
    total_amount,
    currency,
    CASE booking_status WHEN 'EXPIRED' THEN 'FAILED' ELSE 'SUCCESS' END,
    jsonb_build_object('seed', true, 'bookingCode', booking_code),
    CASE booking_status WHEN 'EXPIRED' THEN NULL ELSE created_at + INTERVAL '10 minutes' END,
    created_at + INTERVAL '10 minutes'
FROM __seed_booking_data
WHERE booking_status NOT IN ('PENDING_PAYMENT')
ON CONFLICT (id) DO UPDATE SET
    payment_intent_id = EXCLUDED.payment_intent_id,
    transaction_code = EXCLUDED.transaction_code,
    provider = EXCLUDED.provider,
    provider_transaction_id = EXCLUDED.provider_transaction_id,
    amount = EXCLUDED.amount,
    currency = EXCLUDED.currency,
    status = EXCLUDED.status,
    raw_payload = EXCLUDED.raw_payload,
    paid_at = EXCLUDED.paid_at;

INSERT INTO payment.refunds (
    id,
    transaction_id,
    refund_code,
    amount,
    reason,
    status,
    provider_reference,
    refunded_at,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-refund-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-refund-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-refund-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-refund-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-refund-' || booking_id), 21, 12)
    )::UUID,
    (
        SUBSTR(MD5('aresdrive-transaction-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-transaction-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-transaction-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-transaction-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-transaction-' || booking_id), 21, 12)
    )::UUID,
    'REF-SEED-' || LPAD(seed_no::TEXT, 6, '0'),
    LEAST(total_amount, 100000)::NUMERIC(14, 2),
    'seed: cancellation refund',
    CASE seed_no % 3 WHEN 0 THEN 'PROCESSING' ELSE 'SUCCESS' END,
    'REF-PROVIDER-SEED-' || LPAD(seed_no::TEXT, 6, '0'),
    CASE seed_no % 3 WHEN 0 THEN NULL ELSE updated_at + INTERVAL '2 hours' END,
    updated_at,
    now()
FROM __seed_booking_data
WHERE booking_status = 'CANCELLED'
ON CONFLICT (id) DO UPDATE SET
    transaction_id = EXCLUDED.transaction_id,
    refund_code = EXCLUDED.refund_code,
    amount = EXCLUDED.amount,
    reason = EXCLUDED.reason,
    status = EXCLUDED.status,
    provider_reference = EXCLUDED.provider_reference,
    refunded_at = EXCLUDED.refunded_at,
    updated_at = now();

INSERT INTO payment.payouts (
    id,
    recipient_type,
    recipient_id,
    booking_id,
    amount,
    currency,
    status,
    scheduled_at,
    paid_at,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-payout-' || booking_id || '-' || recipient_type), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-payout-' || booking_id || '-' || recipient_type), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-payout-' || booking_id || '-' || recipient_type), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-payout-' || booking_id || '-' || recipient_type), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-payout-' || booking_id || '-' || recipient_type), 21, 12)
    )::UUID,
    recipient_type,
    recipient_id,
    booking_id,
    amount,
    currency,
    CASE seed_no % 4 WHEN 0 THEN 'PROCESSING' ELSE 'PAID' END,
    end_at + INTERVAL '1 day',
    CASE seed_no % 4 WHEN 0 THEN NULL ELSE end_at + INTERVAL '2 days' END,
    updated_at,
    now()
FROM __seed_booking_data seeded
CROSS JOIN LATERAL (VALUES
    (
        CASE seeded.vehicle_source WHEN 'HOST_OWNED' THEN 'HOST' ELSE 'COMPANY' END,
        COALESCE(seeded.host_customer_id, '00000000-0000-0000-0000-000000000105'::UUID),
        ROUND((seeded.total_amount * 0.75)::NUMERIC, 2),
        true
    ),
    (
        'DRIVER',
        seeded.driver_id,
        300000::NUMERIC(14, 2),
        seeded.driver_id IS NOT NULL
    )
) AS payouts(recipient_type, recipient_id, amount, enabled)
WHERE seeded.booking_status = 'COMPLETED'
  AND payouts.enabled
ON CONFLICT (id) DO UPDATE SET
    recipient_type = EXCLUDED.recipient_type,
    recipient_id = EXCLUDED.recipient_id,
    booking_id = EXCLUDED.booking_id,
    amount = EXCLUDED.amount,
    currency = EXCLUDED.currency,
    status = EXCLUDED.status,
    scheduled_at = EXCLUDED.scheduled_at,
    paid_at = EXCLUDED.paid_at,
    updated_at = now();

INSERT INTO pricing.promotion_redemptions (
    id,
    promotion_id,
    customer_id,
    booking_id,
    discount_amount,
    redeemed_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-promotion-redemption-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-redemption-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-redemption-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-redemption-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-redemption-' || booking_id), 21, 12)
    )::UUID,
    (
        SUBSTR(MD5('aresdrive-promotion-' || ((seed_no % 4) + 1)), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-' || ((seed_no % 4) + 1)), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-' || ((seed_no % 4) + 1)), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-' || ((seed_no % 4) + 1)), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-promotion-' || ((seed_no % 4) + 1)), 21, 12)
    )::UUID,
    customer_id,
    booking_id,
    discount_amount,
    created_at
FROM __seed_booking_data
WHERE discount_amount > 0
ON CONFLICT (id) DO UPDATE SET
    promotion_id = EXCLUDED.promotion_id,
    customer_id = EXCLUDED.customer_id,
    booking_id = EXCLUDED.booking_id,
    discount_amount = EXCLUDED.discount_amount,
    redeemed_at = EXCLUDED.redeemed_at;

INSERT INTO review.reviews (
    id,
    booking_id,
    reviewer_customer_id,
    reviewee_type,
    reviewee_id,
    target_type,
    target_id,
    rating,
    content,
    status,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-review-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 21, 12)
    )::UUID,
    booking_id,
    customer_id,
    CASE vehicle_source WHEN 'HOST_OWNED' THEN 'HOST' ELSE 'COMPANY' END,
    COALESCE(host_customer_id, '00000000-0000-0000-0000-000000000105'::UUID),
    'VEHICLE',
    vehicle_id,
    3 + (seed_no % 3),
    'seed: review content for vehicle UI testing',
    CASE seed_no % 10 WHEN 0 THEN 'PENDING' WHEN 1 THEN 'HIDDEN' ELSE 'APPROVED' END,
    end_at + INTERVAL '1 day',
    now()
FROM __seed_booking_data
WHERE booking_status = 'COMPLETED'
ON CONFLICT (id) DO UPDATE SET
    booking_id = EXCLUDED.booking_id,
    reviewer_customer_id = EXCLUDED.reviewer_customer_id,
    reviewee_type = EXCLUDED.reviewee_type,
    reviewee_id = EXCLUDED.reviewee_id,
    target_type = EXCLUDED.target_type,
    target_id = EXCLUDED.target_id,
    rating = EXCLUDED.rating,
    content = EXCLUDED.content,
    status = EXCLUDED.status,
    updated_at = now();

INSERT INTO review.review_replies (
    id,
    review_id,
    author_type,
    author_id,
    content,
    created_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-review-reply-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-review-reply-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-reply-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-reply-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-reply-' || booking_id), 21, 12)
    )::UUID,
    (
        SUBSTR(MD5('aresdrive-review-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 21, 12)
    )::UUID,
    CASE vehicle_source WHEN 'HOST_OWNED' THEN 'HOST' ELSE 'ADMIN' END,
    COALESCE(host_customer_id, '00000000-0000-0000-0000-000000000105'::UUID),
    'seed: reply to review',
    end_at + INTERVAL '2 days'
FROM __seed_booking_data
WHERE booking_status = 'COMPLETED'
  AND seed_no % 3 = 0
ON CONFLICT (id) DO UPDATE SET
    review_id = EXCLUDED.review_id,
    author_type = EXCLUDED.author_type,
    author_id = EXCLUDED.author_id,
    content = EXCLUDED.content;

INSERT INTO review.review_reports (
    id,
    review_id,
    reporter_id,
    reason_code,
    reason_text,
    status,
    created_at,
    resolved_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-review-report-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-review-report-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-report-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-report-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-report-' || booking_id), 21, 12)
    )::UUID,
    (
        SUBSTR(MD5('aresdrive-review-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-review-' || booking_id), 21, 12)
    )::UUID,
    customer_id,
    'SEED_REPORT',
    'seed: review report for moderation UI',
    CASE seed_no % 4 WHEN 0 THEN 'RESOLVED' ELSE 'OPEN' END,
    end_at + INTERVAL '3 days',
    CASE seed_no % 4 WHEN 0 THEN end_at + INTERVAL '4 days' ELSE NULL END
FROM __seed_booking_data
WHERE booking_status = 'COMPLETED'
  AND seed_no % 8 = 0
ON CONFLICT (id) DO UPDATE SET
    review_id = EXCLUDED.review_id,
    reporter_id = EXCLUDED.reporter_id,
    reason_code = EXCLUDED.reason_code,
    reason_text = EXCLUDED.reason_text,
    status = EXCLUDED.status,
    resolved_at = EXCLUDED.resolved_at;

INSERT INTO notification.devices (
    id,
    user_id,
    device_token,
    platform,
    status,
    last_seen_at,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-device-' || user_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-device-' || user_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-device-' || user_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-device-' || user_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-device-' || user_id), 21, 12)
    )::UUID,
    user_id,
    'seed-device-token-' || customer_no,
    CASE customer_no % 3 WHEN 0 THEN 'IOS' WHEN 1 THEN 'ANDROID' ELSE 'WEB' END,
    CASE customer_no % 17 WHEN 0 THEN 'INACTIVE' ELSE 'ACTIVE' END,
    now() - (customer_no * INTERVAL '1 hour'),
    created_at,
    now()
FROM __seed_customers
ON CONFLICT (device_token) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    platform = EXCLUDED.platform,
    status = EXCLUDED.status,
    last_seen_at = EXCLUDED.last_seen_at,
    updated_at = now();

INSERT INTO notification.notifications (
    id,
    recipient_user_id,
    recipient_customer_id,
    channel,
    template_code,
    title,
    content,
    data,
    status,
    scheduled_at,
    sent_at,
    read_at,
    created_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-notification-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-notification-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-notification-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-notification-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-notification-' || booking_id), 21, 12)
    )::UUID,
    renter_user_id,
    customer_id,
    CASE seed_no % 3 WHEN 0 THEN 'EMAIL' WHEN 1 THEN 'PUSH' ELSE 'IN_APP' END,
    CASE booking_status WHEN 'CONFIRMED' THEN 'BOOKING_CONFIRMED' ELSE 'PAYMENT_SUCCEEDED' END,
    'Seed notification for ' || booking_code,
    'seed: notification content for UI testing',
    jsonb_build_object('bookingId', booking_id, 'vehicleId', vehicle_id, 'seed', true),
    CASE seed_no % 5 WHEN 0 THEN 'READ' WHEN 1 THEN 'PENDING' ELSE 'SENT' END,
    CASE seed_no % 5 WHEN 1 THEN now() + INTERVAL '1 hour' ELSE NULL END,
    CASE seed_no % 5 WHEN 1 THEN NULL ELSE created_at + INTERVAL '20 minutes' END,
    CASE seed_no % 5 WHEN 0 THEN created_at + INTERVAL '1 hour' ELSE NULL END,
    created_at
FROM __seed_booking_data
ON CONFLICT (id) DO UPDATE SET
    recipient_user_id = EXCLUDED.recipient_user_id,
    recipient_customer_id = EXCLUDED.recipient_customer_id,
    channel = EXCLUDED.channel,
    template_code = EXCLUDED.template_code,
    title = EXCLUDED.title,
    content = EXCLUDED.content,
    data = EXCLUDED.data,
    status = EXCLUDED.status,
    scheduled_at = EXCLUDED.scheduled_at,
    sent_at = EXCLUDED.sent_at,
    read_at = EXCLUDED.read_at;

INSERT INTO notification.delivery_logs (
    id,
    notification_id,
    provider,
    provider_message_id,
    status,
    error_message,
    raw_payload,
    created_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-delivery-log-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-delivery-log-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-delivery-log-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-delivery-log-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-delivery-log-' || booking_id), 21, 12)
    )::UUID,
    (
        SUBSTR(MD5('aresdrive-notification-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-notification-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-notification-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-notification-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-notification-' || booking_id), 21, 12)
    )::UUID,
    CASE seed_no % 3 WHEN 0 THEN 'SendGrid' WHEN 1 THEN 'Firebase' ELSE 'Ares InApp' END,
    'MSG-SEED-' || LPAD(seed_no::TEXT, 6, '0'),
    CASE seed_no % 11 WHEN 0 THEN 'FAILED' ELSE 'SENT' END,
    CASE seed_no % 11 WHEN 0 THEN 'seed: provider failure example' ELSE NULL END,
    jsonb_build_object('seed', true, 'bookingCode', booking_code),
    created_at + INTERVAL '25 minutes'
FROM __seed_booking_data
WHERE seed_no % 5 <> 1
ON CONFLICT (id) DO UPDATE SET
    notification_id = EXCLUDED.notification_id,
    provider = EXCLUDED.provider,
    provider_message_id = EXCLUDED.provider_message_id,
    status = EXCLUDED.status,
    error_message = EXCLUDED.error_message,
    raw_payload = EXCLUDED.raw_payload;

INSERT INTO admin.audit_logs (
    id,
    actor_user_id,
    actor_role,
    action,
    target_type,
    target_id,
    before_data,
    after_data,
    ip_address,
    user_agent,
    created_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-audit-vehicle-' || vehicle_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-audit-vehicle-' || vehicle_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-audit-vehicle-' || vehicle_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-audit-vehicle-' || vehicle_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-audit-vehicle-' || vehicle_id), 21, 12)
    )::UUID,
    (
        SUBSTR(MD5('aresdrive-admin-user-1'), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-1'), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-1'), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-1'), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-1'), 21, 12)
    )::UUID,
    'ADMIN',
    'VEHICLE_SEED_REVIEW',
    'VEHICLE',
    vehicle_id,
    jsonb_build_object('status', 'DRAFT'),
    jsonb_build_object('status', status, 'licensePlate', license_plate),
    '127.0.0.1',
    'seed-script',
    updated_at
FROM __seed_vehicle_ids
ON CONFLICT (id) DO UPDATE SET
    actor_user_id = EXCLUDED.actor_user_id,
    actor_role = EXCLUDED.actor_role,
    action = EXCLUDED.action,
    target_type = EXCLUDED.target_type,
    target_id = EXCLUDED.target_id,
    before_data = EXCLUDED.before_data,
    after_data = EXCLUDED.after_data,
    ip_address = EXCLUDED.ip_address,
    user_agent = EXCLUDED.user_agent;

INSERT INTO admin.backoffice_tasks (
    id,
    task_type,
    target_type,
    target_id,
    title,
    description,
    priority,
    status,
    assigned_to,
    due_at,
    resolved_at,
    created_at,
    updated_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-backoffice-task-' || vehicle_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-backoffice-task-' || vehicle_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-backoffice-task-' || vehicle_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-backoffice-task-' || vehicle_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-backoffice-task-' || vehicle_id), 21, 12)
    )::UUID,
    CASE status WHEN 'PENDING_REVIEW' THEN 'VEHICLE_REVIEW' WHEN 'SUSPENDED' THEN 'VEHICLE_COMPLIANCE' ELSE 'LISTING_QA' END,
    'VEHICLE',
    vehicle_id,
    'Review seed vehicle ' || license_plate,
    'seed: task for admin UI testing',
    CASE seed_no % 4 WHEN 0 THEN 'URGENT' WHEN 1 THEN 'HIGH' WHEN 2 THEN 'NORMAL' ELSE 'LOW' END,
    CASE status WHEN 'ACTIVE' THEN 'RESOLVED' ELSE 'OPEN' END,
    (
        SUBSTR(MD5('aresdrive-admin-user-2'), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-2'), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-2'), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-2'), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-admin-user-2'), 21, 12)
    )::UUID,
    now() + ((seed_no % 14) * INTERVAL '1 day'),
    CASE status WHEN 'ACTIVE' THEN now() - INTERVAL '1 day' ELSE NULL END,
    created_at,
    now()
FROM __seed_vehicle_ids
WHERE status IN ('PENDING_REVIEW', 'SUSPENDED', 'ACTIVE')
ON CONFLICT (id) DO UPDATE SET
    task_type = EXCLUDED.task_type,
    target_type = EXCLUDED.target_type,
    target_id = EXCLUDED.target_id,
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    priority = EXCLUDED.priority,
    status = EXCLUDED.status,
    assigned_to = EXCLUDED.assigned_to,
    due_at = EXCLUDED.due_at,
    resolved_at = EXCLUDED.resolved_at,
    updated_at = now();

INSERT INTO admin.dashboard_snapshots (
    id,
    snapshot_date,
    metric_code,
    metric_value,
    dimensions,
    created_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-dashboard-' || metric_code || '-' || snapshot_date || '-' || dimension_key), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-dashboard-' || metric_code || '-' || snapshot_date || '-' || dimension_key), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-dashboard-' || metric_code || '-' || snapshot_date || '-' || dimension_key), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-dashboard-' || metric_code || '-' || snapshot_date || '-' || dimension_key), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-dashboard-' || metric_code || '-' || snapshot_date || '-' || dimension_key), 21, 12)
    )::UUID,
    snapshot_date,
    metric_code,
    metric_value,
    dimensions,
    now()
FROM (
    SELECT CURRENT_DATE AS snapshot_date, 'VEHICLES_TOTAL' AS metric_code, COUNT(*)::NUMERIC AS metric_value, '{}'::jsonb AS dimensions, 'all' AS dimension_key FROM __seed_vehicle_ids
    UNION ALL
    SELECT CURRENT_DATE, 'BOOKINGS_TOTAL', COUNT(*)::NUMERIC, '{}'::jsonb, 'all' FROM __seed_booking_data
    UNION ALL
    SELECT CURRENT_DATE, 'ACTIVE_LISTINGS', COUNT(*)::NUMERIC, '{}'::jsonb, 'all' FROM __seed_vehicle_ids WHERE status = 'ACTIVE'
    UNION ALL
    SELECT CURRENT_DATE, 'REVENUE_CONFIRMED', COALESCE(SUM(total_amount), 0)::NUMERIC, '{}'::jsonb, 'all' FROM __seed_booking_data WHERE booking_status IN ('CONFIRMED', 'IN_PROGRESS', 'COMPLETED')
    UNION ALL
    SELECT CURRENT_DATE, 'VEHICLES_BY_SOURCE', COUNT(*)::NUMERIC, jsonb_build_object('source', source), source FROM __seed_vehicle_ids GROUP BY source
) AS metrics
ON CONFLICT (snapshot_date, metric_code, dimensions) DO UPDATE SET
    metric_value = EXCLUDED.metric_value,
    created_at = now();

INSERT INTO common.outbox_events (
    id,
    aggregate_type,
    aggregate_id,
    event_type,
    payload,
    headers,
    status,
    retry_count,
    next_retry_at,
    published_at,
    created_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-outbox-booking-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-outbox-booking-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-outbox-booking-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-outbox-booking-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-outbox-booking-' || booking_id), 21, 12)
    )::UUID,
    'Booking',
    booking_id,
    'BookingStatusChanged',
    jsonb_build_object('bookingCode', booking_code, 'status', booking_status, 'seed', true),
    jsonb_build_object('source', 'seed_vehicles.sql'),
    CASE seed_no % 4 WHEN 0 THEN 'PUBLISHED' ELSE 'PENDING' END,
    0,
    CASE seed_no % 4 WHEN 0 THEN NULL ELSE created_at + INTERVAL '10 minutes' END,
    CASE seed_no % 4 WHEN 0 THEN created_at + INTERVAL '5 minutes' ELSE NULL END,
    created_at
FROM __seed_booking_data
WHERE seed_no <= 120
ON CONFLICT (id) DO UPDATE SET
    aggregate_type = EXCLUDED.aggregate_type,
    aggregate_id = EXCLUDED.aggregate_id,
    event_type = EXCLUDED.event_type,
    payload = EXCLUDED.payload,
    headers = EXCLUDED.headers,
    status = EXCLUDED.status,
    retry_count = EXCLUDED.retry_count,
    next_retry_at = EXCLUDED.next_retry_at,
    published_at = EXCLUDED.published_at;

INSERT INTO common.idempotency_keys (
    id,
    key_value,
    owner_type,
    owner_id,
    request_hash,
    response_payload,
    expires_at,
    created_at
)
SELECT
    (
        SUBSTR(MD5('aresdrive-idempotency-' || booking_id), 1, 8) || '-' ||
        SUBSTR(MD5('aresdrive-idempotency-' || booking_id), 9, 4) || '-' ||
        SUBSTR(MD5('aresdrive-idempotency-' || booking_id), 13, 4) || '-' ||
        SUBSTR(MD5('aresdrive-idempotency-' || booking_id), 17, 4) || '-' ||
        SUBSTR(MD5('aresdrive-idempotency-' || booking_id), 21, 12)
    )::UUID,
    'seed-booking-' || booking_code,
    'CUSTOMER',
    customer_id,
    MD5('seed-request-' || booking_id),
    jsonb_build_object('bookingId', booking_id, 'seed', true),
    now() + INTERVAL '7 days',
    created_at
FROM __seed_booking_data
WHERE seed_no <= 120
ON CONFLICT (key_value) DO UPDATE SET
    owner_type = EXCLUDED.owner_type,
    owner_id = EXCLUDED.owner_id,
    request_hash = EXCLUDED.request_hash,
    response_payload = EXCLUDED.response_payload,
    expires_at = EXCLUDED.expires_at;

SELECT
    'seed_full_vehicle_related_test_data' AS seed_name,
    (SELECT COUNT(*) FROM __seed_customers) AS customers_seeded,
    (SELECT COUNT(*) FROM __seed_drivers) AS drivers_seeded,
    (SELECT COUNT(*) FROM __seed_vehicle_ids) AS vehicles_seeded,
    (SELECT COUNT(*) FROM vehicle.vehicle_listings listings
        JOIN __seed_vehicle_ids seeded ON seeded.vehicle_id = listings.vehicle_id) AS listings_seeded,
    (SELECT COUNT(*) FROM fleet.company_vehicles company
        JOIN __seed_vehicle_ids seeded ON seeded.fleet_vehicle_id = company.id) AS company_vehicles_seeded,
    (SELECT COUNT(*) FROM pricing.price_plans plans
        JOIN __seed_vehicle_ids seeded ON seeded.vehicle_id = plans.target_id
        WHERE plans.target_type = 'VEHICLE') AS price_plans_seeded,
    (SELECT COUNT(*) FROM __seed_booking_data) AS bookings_seeded,
    (SELECT COUNT(*) FROM payment.payment_intents intents
        JOIN __seed_booking_data seeded ON seeded.booking_id = intents.booking_id) AS payment_intents_seeded,
    (SELECT COUNT(*) FROM review.reviews reviews
        JOIN __seed_booking_data seeded ON seeded.booking_id = reviews.booking_id) AS reviews_seeded,
    (SELECT COUNT(*) FROM notification.notifications notifications
        JOIN __seed_booking_data seeded ON notifications.id = (
            SUBSTR(MD5('aresdrive-notification-' || seeded.booking_id), 1, 8) || '-' ||
            SUBSTR(MD5('aresdrive-notification-' || seeded.booking_id), 9, 4) || '-' ||
            SUBSTR(MD5('aresdrive-notification-' || seeded.booking_id), 13, 4) || '-' ||
            SUBSTR(MD5('aresdrive-notification-' || seeded.booking_id), 17, 4) || '-' ||
            SUBSTR(MD5('aresdrive-notification-' || seeded.booking_id), 21, 12)
        )::UUID) AS notifications_seeded,
    (SELECT COUNT(*) FROM vehicle.vehicle_images images
        JOIN __seed_vehicle_ids seeded ON seeded.vehicle_id = images.vehicle_id
        WHERE images.file_url LIKE 'https://cdn.aresdrive.test/seed/%') AS images_seeded,
    (SELECT COUNT(*) FROM vehicle.vehicle_features features
        JOIN __seed_vehicle_ids seeded ON seeded.vehicle_id = features.vehicle_id) AS features_seeded,
    (SELECT COUNT(*) FROM vehicle.availability_blocks blocks
        JOIN __seed_vehicle_ids seeded ON seeded.vehicle_id = blocks.vehicle_id
        WHERE blocks.note LIKE 'seed:%') AS availability_blocks_seeded;

COMMIT;
