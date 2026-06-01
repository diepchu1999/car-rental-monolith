SELECT code, name, full_name, level, type, parent_code
FROM location.administrative_units
WHERE status = 'ACTIVE'
  AND (:q = '' OR name ILIKE CONCAT('%', :q, '%') OR full_name ILIKE CONCAT('%', :q, '%'))
  AND (CAST(:level AS TEXT) IS NULL OR level = :level)
  AND (CAST(:provinceCode AS TEXT) IS NULL OR parent_code = :provinceCode)
ORDER BY level ASC, name ASC
LIMIT :lim
