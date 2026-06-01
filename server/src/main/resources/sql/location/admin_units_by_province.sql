SELECT code, name, full_name, level, type, parent_code
FROM location.administrative_units
WHERE status = 'ACTIVE' AND level = 'COMMUNE' AND parent_code = :provinceCode
ORDER BY name ASC
