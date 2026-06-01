UPDATE customer.host_profiles
SET status = :status, updated_at = :now
WHERE customer_id = :cid
