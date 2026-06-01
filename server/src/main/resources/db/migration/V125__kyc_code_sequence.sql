-- Tương tự host_code (V124): kyc_code trước đây sinh bằng UUID cắt 8 ký tự hex
-- (32-bit), đụng ràng buộc UNIQUE customer.kyc_profiles.kyc_code (V123) theo
-- birthday paradox. Chuyển sang sequence: duy nhất tuyệt đối, dễ đọc (KYC-000001).
CREATE SEQUENCE IF NOT EXISTS customer.kyc_code_seq
    START WITH 1
    INCREMENT BY 1;
