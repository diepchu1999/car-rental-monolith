INSERT INTO pricing.price_plans (
    id, target_type, target_id, name, currency,
    base_daily_rate, hourly_rate, weekend_multiplier, deposit_amount,
    status, valid_from, created_at, updated_at
) VALUES (
    :id, 'VEHICLE', :vid, :name, :currency,
    :baseDailyRate, :hourlyRate, :weekendMultiplier, :depositAmount,
    'ACTIVE', :now, :now, :now
)
