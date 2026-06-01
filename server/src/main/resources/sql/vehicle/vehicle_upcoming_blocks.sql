SELECT id, start_at, end_at, reason, booking_id, note
FROM vehicle.availability_blocks
WHERE vehicle_id = :id
  AND end_at > NOW()
ORDER BY start_at ASC
LIMIT 20
