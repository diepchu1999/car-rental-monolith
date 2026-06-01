SELECT
    v.id, v.owner_customer_id, v.fleet_vehicle_id, v.source,
    v.brand, v.model, v.version, v.manufacture_year, v.license_plate,
    v.seats, v.transmission, v.fuel_type, v.status,
    v.created_at, v.updated_at,
    c.full_name             AS owner_full_name,
    c.phone                 AS owner_phone,
    c.email                 AS owner_email,
    hp.host_code            AS host_code,
    hp.display_name         AS host_display_name,
    cv.asset_code           AS asset_code,
    cv.asset_status         AS asset_status,
    br.id                   AS branch_id,
    br.name                 AS branch_name,
    br.city                 AS branch_city,
    vl.id                   AS listing_id,
    vl.title                AS listing_title,
    vl.description          AS listing_description,
    vl.city                 AS listing_city,
    vl.district             AS listing_district,
    vl.province_code        AS listing_province_code,
    vl.commune_code         AS listing_commune_code,
    pu.name                 AS listing_province_name,
    cu.name                 AS listing_commune_name,
    vl.pickup_address       AS listing_pickup_address,
    vl.base_daily_rate      AS listing_base_daily_rate,
    vl.currency             AS listing_currency,
    vl.instant_booking_enabled AS listing_instant_booking,
    vl.delivery_enabled     AS listing_delivery_enabled,
    vl.status               AS listing_status,
    vl.published_at         AS listing_published_at
FROM vehicle.vehicles v
LEFT JOIN customer.customers c ON c.id = v.owner_customer_id
LEFT JOIN customer.host_profiles hp ON hp.customer_id = v.owner_customer_id
LEFT JOIN fleet.company_vehicles cv ON cv.vehicle_id = v.id
LEFT JOIN fleet.branches br ON br.id = cv.branch_id
LEFT JOIN vehicle.vehicle_listings vl ON vl.vehicle_id = v.id
LEFT JOIN location.administrative_units pu ON pu.code = vl.province_code
LEFT JOIN location.administrative_units cu ON cu.code = vl.commune_code
WHERE v.id = :id
