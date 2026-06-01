INSERT INTO customer.host_profiles (
    id, customer_id, host_code, display_name, bio,
    rating_average, rating_count, status, created_at, updated_at
) VALUES (
    :id, :cid, :hostCode, :displayName, CAST(:bio AS text),
    :ratingAverage, :ratingCount, :status, :now, :now
)
