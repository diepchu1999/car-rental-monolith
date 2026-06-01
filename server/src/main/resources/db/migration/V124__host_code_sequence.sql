-- Mã host trước đây sinh bằng UUID.randomUUID() cắt 8 ký tự hex (32-bit) → đụng
-- ràng buộc UNIQUE customer.host_profiles.host_code theo birthday paradox (xác
-- suất trùng ~50% ở mốc ~77k host) khiến tạo customer thỉnh thoảng fail.
-- Chuyển sang sequence: duy nhất tuyệt đối, tuần tự, dễ đọc (HOST-000001, ...).
CREATE SEQUENCE IF NOT EXISTS customer.host_code_seq
    START WITH 1
    INCREMENT BY 1;
