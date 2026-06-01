UPDATE pricing.price_plans
SET status = 'INACTIVE', updated_at = :now
WHERE target_type = 'VEHICLE' AND target_id = :vid AND status = 'ACTIVE'
