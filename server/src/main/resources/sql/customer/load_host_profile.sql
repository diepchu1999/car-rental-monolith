SELECT host_code, display_name, bio, rating_average, rating_count, status, created_at
FROM customer.host_profiles
WHERE customer_id = :id
