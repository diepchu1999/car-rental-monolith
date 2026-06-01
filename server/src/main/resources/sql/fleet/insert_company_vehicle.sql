INSERT INTO fleet.company_vehicles (
    id, vehicle_id, branch_id, asset_code,
    current_odometer_km, asset_status, created_at, updated_at
) VALUES (
    :id, :vehicleId, :branchId, :assetCode,
    0, 'AVAILABLE', :now, :now
)
