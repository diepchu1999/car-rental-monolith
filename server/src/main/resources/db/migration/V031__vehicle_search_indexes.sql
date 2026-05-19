CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- GIN trigram indexes for ILIKE '%keyword%' search on vehicle table.
-- Eliminates full table scans; query planner uses these indexes automatically
-- when the search pattern contains 3+ characters.
CREATE INDEX idx_vehicles_brand_trgm
    ON vehicle.vehicles USING GIN (brand gin_trgm_ops);

CREATE INDEX idx_vehicles_model_trgm
    ON vehicle.vehicles USING GIN (model gin_trgm_ops);

-- Partial index: version is nullable, NULLs never match ILIKE so no point indexing them.
CREATE INDEX idx_vehicles_version_trgm
    ON vehicle.vehicles USING GIN (version gin_trgm_ops)
    WHERE version IS NOT NULL;

CREATE INDEX idx_vehicles_license_plate_trgm
    ON vehicle.vehicles USING GIN (license_plate gin_trgm_ops);
