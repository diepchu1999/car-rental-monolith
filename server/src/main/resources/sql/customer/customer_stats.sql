SELECT
    (SELECT COUNT(*) FROM customer.customers) AS total,
    (SELECT COUNT(DISTINCT cr.customer_id) FROM customer.customer_roles cr
     WHERE cr.role = 'RENTER') AS renters,
    (SELECT COUNT(DISTINCT cr.customer_id) FROM customer.customer_roles cr
     WHERE cr.role = 'HOST') AS hosts,
    (SELECT COUNT(*) FROM customer.customers
     WHERE status IN ('PENDING_KYC', 'BLOCKED')) AS pending_or_blocked
