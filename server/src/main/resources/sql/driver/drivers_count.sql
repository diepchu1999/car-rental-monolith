SELECT COUNT(*) FROM driver.drivers
WHERE (:q = ''
    OR full_name ILIKE CONCAT('%', :q, '%')
    OR driver_code ILIKE CONCAT('%', :q, '%')
    OR phone ILIKE CONCAT('%', :q, '%')
    OR license_number ILIKE CONCAT('%', :q, '%'))
AND (CAST(:status AS TEXT) IS NULL OR status = :status)
