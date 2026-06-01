SELECT
    c.id, c.full_name, c.phone, c.email, c.date_of_birth, c.gender,
    c.status, c.created_at,
    (SELECT string_agg(cr.role, ',' ORDER BY cr.role)
     FROM customer.customer_roles cr WHERE cr.customer_id = c.id) AS roles,
    hp.host_code, hp.display_name, hp.bio, hp.rating_average,
    hp.rating_count, hp.status AS host_status, hp.created_at AS host_created_at,
