SELECT id, file_url, sort_order, is_cover
FROM vehicle.vehicle_images
WHERE vehicle_id = :id
ORDER BY is_cover DESC, sort_order ASC
