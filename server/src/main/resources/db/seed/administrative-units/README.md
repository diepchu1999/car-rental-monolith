# Danh mục đơn vị hành chính Việt Nam (mô hình 2 cấp, hiệu lực 01/07/2025)

Hệ thống lưu địa chỉ theo **mã hành chính chính thức** (province_code, commune_code),
KHÔNG lưu text tự do. Danh mục gốc dựa trên **Quyết định 19/2025/QĐ-TTg** (hiệu lực
01/07/2025): **34 đơn vị cấp tỉnh** và **3.321 đơn vị cấp xã**, mô hình 2 cấp, không
còn cấp huyện.

> **Đã seed sẵn:** migration `V122__seed_administrative_units.sql` đã nạp 34 tỉnh +
> 3.321 xã (sinh từ `server/docs/datalocation___26_05_2026.xls`), tự chạy qua Flyway.
> Bạn **không cần** importer trừ khi muốn cập nhật/nạp nguồn khác — khi đó dùng hướng
> dẫn dưới đây (importer upsert theo `code` nên an toàn chạy chồng lên dữ liệu đã seed).

## 1. Đặt file dữ liệu

Tải danh mục chính thức (ví dụ từ Cục Thống kê / cổng dữ liệu hành chính) và lưu thành
JSON tại:

```
server/src/main/resources/db/seed/administrative-units/administrative-units.json
```

(Đường dẫn tùy ý — khi chạy importer bạn trỏ tới đúng file.)

## 2. Định dạng JSON

Một mảng các object. Mỗi object:

| Trường          | Bắt buộc | Ghi chú                                                        |
|-----------------|----------|----------------------------------------------------------------|
| `code`          | ✅       | Mã chính thức, duy nhất (khóa upsert)                          |
| `name`          | ✅       | Tên ngắn, ví dụ "Hà Nội", "Phường Ba Đình"                     |
| `fullName`      | ❌       | Tên đầy đủ, ví dụ "Thành phố Hà Nội"                           |
| `level`         | ✅       | `PROVINCE` hoặc `COMMUNE`                                       |
| `type`          | ✅       | `PROVINCE`, `CITY`, `COMMUNE`, `WARD`, `SPECIAL_ZONE`          |
| `parentCode`    | ⚠️       | Bắt buộc với cấp xã (trỏ về mã tỉnh); bỏ trống với cấp tỉnh    |
| `effectiveFrom` | ❌       | `yyyy-MM-dd`, mặc định `2025-07-01`                            |
| `effectiveTo`   | ❌       | `yyyy-MM-dd`, null nếu còn hiệu lực                            |
| `status`        | ❌       | `ACTIVE` (mặc định) hoặc `INACTIVE`                            |

Ví dụ:

```json
[
  {
    "code": "01",
    "name": "Hà Nội",
    "fullName": "Thành phố Hà Nội",
    "level": "PROVINCE",
    "type": "CITY",
    "effectiveFrom": "2025-07-01"
  },
  {
    "code": "00004",
    "name": "Phường Ba Đình",
    "fullName": "Phường Ba Đình",
    "level": "COMMUNE",
    "type": "WARD",
    "parentCode": "01",
    "effectiveFrom": "2025-07-01"
  }
]
```

Xem `administrative-units.sample.json` trong thư mục này để biết cấu trúc (chỉ là mẫu
minh họa định dạng — **không phải** dữ liệu chính thức đầy đủ, đừng dùng để seed thật).

## 3. Chạy import

Importer là một `ApplicationRunner` chỉ kích hoạt khi có property
`app.location.import.file`. Bảng được tạo bởi Flyway (`V120__location.sql`); dữ liệu chỉ
được nạp khi bạn chạy lệnh dưới đây. Import là **upsert theo `code`** nên chạy lại nhiều
lần an toàn (cập nhật, không nhân bản).

Dev (Maven):

```bash
cd server
mvn spring-boot:run -Dspring-boot.run.arguments=--app.location.import.file=src/main/resources/db/seed/administrative-units/administrative-units.json
```

Jar đã đóng gói:

```bash
java -jar target/car-rental-monolith.jar --app.location.import.file=/duong/dan/administrative-units.json
```

Sau khi import xong, có thể tắt app rồi chạy lại bình thường (không kèm property) — danh
mục đã nằm trong DB.

## 4. Kiểm tra nhanh

```sql
SELECT level, COUNT(*) FROM location.administrative_units GROUP BY level;
-- kỳ vọng: PROVINCE = 34, COMMUNE = 3321
```
