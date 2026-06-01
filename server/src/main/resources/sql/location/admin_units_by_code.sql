SELECT code, name, full_name, level, type, parent_code
FROM location.administrative_units
WHERE status = 'ACTIVE' AND code = :code
