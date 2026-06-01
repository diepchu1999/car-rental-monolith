SELECT COUNT(*) FROM fleet.company_vehicles cv
LEFT JOIN vehicle.vehicles v ON v.id = cv.vehicle_id
WHERE (:q = ''
    OR cv.asset_code ILIKE CONCAT('%', :q, '%')
    OR COALESCE(v.license_plate, '') ILIKE CONCAT('%', :q, '%'))
AND (CAST(:branchId AS UUID) IS NULL OR cv.branch_id = :branchId)
