FROM customer.customers c
LEFT JOIN customer.host_profiles hp ON hp.customer_id = c.id
LEFT JOIN LATERAL (
    SELECT
        COUNT(*) AS total,
        COUNT(*) FILTER (WHERE status = 'APPROVED') AS approved,
        COUNT(*) FILTER (WHERE status = 'REJECTED') AS rejected,
        COUNT(*) FILTER (WHERE status IN ('PENDING', 'EXPIRED')) AS pending
    FROM customer.kyc_profiles
    WHERE customer_id = c.id
) ka ON TRUE
