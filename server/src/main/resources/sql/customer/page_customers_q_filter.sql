 AND (c.full_name ILIKE :q
      OR COALESCE(c.phone, '') ILIKE :q
      OR COALESCE(c.email, '') ILIKE :q
      OR COALESCE(hp.host_code, '') ILIKE :q)
