SELECT cv.id, cv.vehicle_id, cv.asset_code, cv.asset_status,
       br.id AS branch_id, br.name AS branch_name, br.city AS branch_city,
       v.license_plate
FROM fleet.company_vehicles cv
LEFT JOIN fleet.branches br ON br.id = cv.branch_id
LEFT JOIN vehicle.vehicles v ON v.id = cv.vehicle_id
WHERE (:q = ''
    OR cv.asset_code ILIKE CONCAT('%', :q, '%')
    OR COALESCE(v.license_plate, '') ILIKE CONCAT('%', :q, '%'))
AND (CAST(:branchId AS UUID) IS NULL OR cv.branch_id = :branchId)
ORDER BY cv.asset_code ASC
LIMIT :lim OFFSET :off
