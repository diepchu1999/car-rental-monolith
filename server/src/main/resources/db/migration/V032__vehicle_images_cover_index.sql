-- Composite index for the cover-image lookup used by the enriched vehicle list query:
--   SELECT file_url FROM vehicle.vehicle_images
--   WHERE vehicle_id = ?
--   ORDER BY is_cover DESC, sort_order ASC
--   LIMIT 1
-- The leading `vehicle_id` makes the WHERE selective; the trailing sort columns let
-- PostgreSQL return the first row directly without an in-memory sort.
CREATE INDEX idx_vehicle_images_cover_lookup
    ON vehicle.vehicle_images (vehicle_id, is_cover DESC, sort_order ASC);
