INSERT INTO customer.addresses (
    id, customer_id, label, line1, ward, district, city, country,
    province_code, commune_code, is_default, created_at
) VALUES (
    :id, :cid, CAST(:label AS varchar), :line1, CAST(:ward AS varchar),
    CAST(:district AS varchar), :city, :country,
    CAST(:provinceCode AS varchar), CAST(:communeCode AS varchar), :isDefault, :now
)
