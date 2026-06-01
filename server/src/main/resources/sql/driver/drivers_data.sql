SELECT id, driver_code, full_name, phone, license_number, license_class,
       license_expiry_date, years_of_experience, rating_average,
       rating_count, status
FROM driver.drivers
WHERE (:q = ''
    OR full_name ILIKE CONCAT('%', :q, '%')
    OR driver_code ILIKE CONCAT('%', :q, '%')
    OR phone ILIKE CONCAT('%', :q, '%')
    OR license_number ILIKE CONCAT('%', :q, '%'))
AND (CAST(:status AS TEXT) IS NULL OR status = :status)
ORDER BY full_name ASC
LIMIT :lim OFFSET :off
