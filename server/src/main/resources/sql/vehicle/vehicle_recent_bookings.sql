SELECT id, booking_code, customer_id, start_at, end_at,
       total_amount, currency, status, created_at
FROM booking.bookings
WHERE vehicle_id = :id
ORDER BY created_at DESC
LIMIT 10
