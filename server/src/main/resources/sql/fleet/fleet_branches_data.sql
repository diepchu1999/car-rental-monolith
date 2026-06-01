SELECT br.id, br.code, br.name, br.address, br.city, br.phone, br.status,
       br.province_code, br.commune_code, br.created_at, br.updated_at
FROM fleet.branches br
WHERE (:q = ''
    OR br.code ILIKE CONCAT('%', :q, '%')
    OR br.name ILIKE CONCAT('%', :q, '%')
    OR br.address ILIKE CONCAT('%', :q, '%')
    OR br.city ILIKE CONCAT('%', :q, '%')
    OR COALESCE(br.phone, '') ILIKE CONCAT('%', :q, '%'))
AND (CAST(:status AS TEXT) IS NULL OR br.status = :status)
ORDER BY br.created_at DESC, br.id ASC
LIMIT :lim OFFSET :off
