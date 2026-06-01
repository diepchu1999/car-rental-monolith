SELECT a.id, a.label, a.line1, a.province_code, a.commune_code,
       pu.name AS province_name, cu.name AS commune_name,
       a.district AS legacy_district, a.is_default
FROM customer.addresses a
LEFT JOIN location.administrative_units pu ON pu.code = a.province_code
LEFT JOIN location.administrative_units cu ON cu.code = a.commune_code
WHERE a.customer_id = :id
ORDER BY a.is_default DESC, a.created_at ASC
