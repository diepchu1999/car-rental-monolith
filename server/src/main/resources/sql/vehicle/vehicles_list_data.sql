SELECT
    v.id,
    v.owner_customer_id,
    c.full_name             AS owner_customer_name,
    hp.host_code,
    v.fleet_vehicle_id,
    cv.asset_code,
    br.name                 AS branch_name,
    v.source,
    v.brand,
    v.model,
    v.version,
    v.manufacture_year,
    v.license_plate,
    v.seats,
    v.transmission,
    v.fuel_type,
    v.status,
    vl.status               AS listing_status,
    vl.city,
    vl.district,
    vl.base_daily_rate,
    (SELECT vi.file_url
     FROM vehicle.vehicle_images vi
     WHERE vi.vehicle_id = v.id
     ORDER BY vi.is_cover DESC, vi.sort_order ASC
     LIMIT 1)               AS cover_image_url,
    (SELECT COUNT(*)
     FROM vehicle.vehicle_features vf
     WHERE vf.vehicle_id = v.id) AS feature_count,
    (SELECT COUNT(*)
     FROM vehicle.availability_blocks ab
     WHERE ab.vehicle_id = v.id
       AND ab.end_at > NOW()) AS active_availability_block_count,
    (SELECT COUNT(*)
     FROM booking.bookings bk
     WHERE bk.vehicle_id = v.id) AS booking_count,
    v.created_at,
    v.updated_at
FROM vehicle.vehicles v
LEFT JOIN customer.customers c ON c.id = v.owner_customer_id
LEFT JOIN customer.host_profiles hp ON hp.customer_id = v.owner_customer_id
LEFT JOIN fleet.company_vehicles cv ON cv.vehicle_id = v.id
LEFT JOIN fleet.branches br ON br.id = cv.branch_id
LEFT JOIN vehicle.vehicle_listings vl ON vl.vehicle_id = v.id
WHERE (:q = '' OR v.brand ILIKE CONCAT('%', :q, '%')
    OR v.model ILIKE CONCAT('%', :q, '%')
    OR v.version ILIKE CONCAT('%', :q, '%')
    OR v.license_plate ILIKE CONCAT('%', :q, '%'))
AND (CAST(:source AS TEXT) IS NULL OR v.source = :source)
AND (CAST(:status AS TEXT) IS NULL OR v.status = :status)
AND (CAST(:listingStatus AS TEXT) IS NULL OR vl.status = :listingStatus)
AND (CAST(:provinceCode AS TEXT) IS NULL OR vl.province_code = :provinceCode)
AND (CAST(:communeCode AS TEXT) IS NULL OR vl.commune_code = :communeCode)
AND (CAST(:fuelType AS TEXT) IS NULL OR v.fuel_type = :fuelType)
AND (CAST(:transmission AS TEXT) IS NULL OR v.transmission = :transmission)
AND (CAST(:seats AS INTEGER) IS NULL OR v.seats = :seats)
AND (CAST(:minRate AS NUMERIC) IS NULL OR vl.base_daily_rate >= :minRate)
AND (CAST(:maxRate AS NUMERIC) IS NULL OR vl.base_daily_rate <= :maxRate)
AND (CAST(:hasBookings AS BOOLEAN) IS NULL
    OR (:hasBookings = TRUE  AND EXISTS (SELECT 1 FROM booking.bookings bk
                                        WHERE bk.vehicle_id = v.id))
    OR (:hasBookings = FALSE AND NOT EXISTS (SELECT 1 FROM booking.bookings bk
                                            WHERE bk.vehicle_id = v.id)))
