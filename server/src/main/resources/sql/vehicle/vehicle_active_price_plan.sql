SELECT id, name, base_daily_rate, hourly_rate, weekend_multiplier,
       deposit_amount, currency, status, valid_from, valid_to
FROM pricing.price_plans
WHERE target_type = 'VEHICLE'
  AND target_id = :id
  AND status = 'ACTIVE'
ORDER BY valid_from DESC NULLS LAST
LIMIT 1
