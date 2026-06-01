SELECT COUNT(*)
FROM customer.customers c
LEFT JOIN customer.host_profiles hp ON hp.customer_id = c.id
WHERE (:q = ''
    OR c.full_name ILIKE CONCAT('%', :q, '%')
    OR COALESCE(c.phone, '') ILIKE CONCAT('%', :q, '%')
    OR COALESCE(c.email, '') ILIKE CONCAT('%', :q, '%')
    OR COALESCE(hp.host_code, '') ILIKE CONCAT('%', :q, '%'))
AND (:hostOnly = FALSE OR hp.status = 'ACTIVE')
