SELECT id, code, name, city, status
FROM fleet.branches
WHERE status = 'ACTIVE'
ORDER BY name ASC
