SELECT COUNT(*) FROM fleet.branches br
WHERE (:q = ''
    OR br.code ILIKE CONCAT('%', :q, '%')
    OR br.name ILIKE CONCAT('%', :q, '%')
    OR br.address ILIKE CONCAT('%', :q, '%')
    OR br.city ILIKE CONCAT('%', :q, '%')
    OR COALESCE(br.phone, '') ILIKE CONCAT('%', :q, '%'))
AND (CAST(:status AS TEXT) IS NULL OR br.status = :status)
