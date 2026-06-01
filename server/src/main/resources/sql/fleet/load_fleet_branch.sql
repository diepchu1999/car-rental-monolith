SELECT id, code, name, address, city, phone, status,
       province_code,
       commune_code,
       created_at,
       updated_at
FROM fleet.branches
where id = :id