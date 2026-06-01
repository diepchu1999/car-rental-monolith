    AS kyc_aggregate_status,
    (SELECT COUNT(*) FROM booking.bookings b WHERE b.customer_id = c.id) AS booking_count,
    (SELECT COUNT(*) FROM vehicle.vehicles v WHERE v.owner_customer_id = c.id) AS vehicle_count,
    (SELECT COALESCE(SUM(b.total_amount), 0) FROM booking.bookings b
     WHERE b.host_customer_id = c.id AND b.status = 'COMPLETED') AS total_revenue
