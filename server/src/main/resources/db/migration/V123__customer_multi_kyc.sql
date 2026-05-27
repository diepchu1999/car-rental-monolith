-- Multi-KYC: cho phép 1 customer nộp nhiều hồ sơ KYC.
-- Trước đó V020 đặt UNIQUE(customer_id) ngăn nộp >1 hồ sơ; bỏ ràng buộc đó.

ALTER TABLE customer.kyc_profiles
    DROP CONSTRAINT IF EXISTS kyc_profiles_customer_id_key;

-- Composite index (customer_id, status) phục vụ 2 truy vấn nóng:
--   1) load list KYC theo customer_id (FK lookup — dùng left prefix).
--   2) GROUP BY status để compute aggregate KYC status mỗi row trên list page.
CREATE INDEX IF NOT EXISTS idx_kyc_profiles_customer_status
    ON customer.kyc_profiles (customer_id, status);

-- Mã hiển thị từng hồ sơ KYC trên UI ("KYC-XXXXXXXX"). Cần UNIQUE để tránh
-- trùng giữa các customer khi tra cứu nhanh theo mã.
ALTER TABLE customer.kyc_profiles
    ADD COLUMN IF NOT EXISTS kyc_code VARCHAR(40);

UPDATE customer.kyc_profiles
SET kyc_code = 'KYC-' || UPPER(SUBSTRING(id::text, 1, 8))
WHERE kyc_code IS NULL;

ALTER TABLE customer.kyc_profiles
    ALTER COLUMN kyc_code SET NOT NULL;

ALTER TABLE customer.kyc_profiles
    ADD CONSTRAINT uq_kyc_profiles_kyc_code UNIQUE (kyc_code);
