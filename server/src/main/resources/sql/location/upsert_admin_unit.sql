INSERT INTO location.administrative_units
    (code, name, full_name, level, type, parent_code,
     effective_from, effective_to, status, created_at, updated_at)
VALUES
    (:code, :name, :fullName, :level, :type, :parentCode,
     :effectiveFrom, :effectiveTo, :status, now(), now())
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    full_name = EXCLUDED.full_name,
    level = EXCLUDED.level,
    type = EXCLUDED.type,
    parent_code = EXCLUDED.parent_code,
    effective_from = EXCLUDED.effective_from,
    effective_to = EXCLUDED.effective_to,
    status = EXCLUDED.status,
    updated_at = now()
