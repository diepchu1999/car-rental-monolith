SELECT
    (SELECT COUNT(*) FROM booking.bookings WHERE customer_id = :id) AS booking_count,
    (SELECT COUNT(*) FROM vehicle.vehicles WHERE owner_customer_id = :id) AS vehicle_count,
    (SELECT COALESCE(SUM(total_amount), 0) FROM booking.bookings
     WHERE host_customer_id = :id AND status = 'COMPLETED') AS total_revenue
