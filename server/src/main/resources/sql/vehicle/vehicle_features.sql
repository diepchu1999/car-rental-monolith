SELECT id, code, name
FROM vehicle.vehicle_features
WHERE vehicle_id = :id
ORDER BY name ASC
