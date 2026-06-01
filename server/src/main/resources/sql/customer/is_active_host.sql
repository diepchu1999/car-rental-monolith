SELECT COUNT(*)
FROM customer.customers c
JOIN customer.host_profiles hp ON hp.customer_id = c.id
WHERE c.id = :id AND c.status = 'ACTIVE' AND hp.status = 'ACTIVE'
