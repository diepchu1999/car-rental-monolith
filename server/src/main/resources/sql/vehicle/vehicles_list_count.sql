SELECT COUNT(*) FROM vehicle.vehicles v
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
