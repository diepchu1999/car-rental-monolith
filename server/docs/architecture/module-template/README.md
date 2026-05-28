# module-template

Bộ khung mẫu cho một module mới, theo chuẩn trong
[`../MODULE_ARCHITECTURE.md`](../MODULE_ARCHITECTURE.md).

> ⚠️ Thư mục này nằm **ngoài** `src/main/java` nên **không** được biên dịch và
> **không** bị Spring quét. Đây chỉ là file `.java` mẫu để **copy**.

## Cách dùng

1. Copy toàn bộ cây thư mục này vào
   `src/main/java/com/ares/car_rental_monolith/modules/<module>/`.
2. Thay `sample` → `<module>` và `Sample` → `<X>` (tên module viết hoa) ở package,
   tên file và tên class.
3. Xóa layer không dùng (vd chưa cần `api`, `cli`, `storage`).
4. Tạo migration Flyway `Vxxx__<module>.sql` cho schema riêng của module.
5. Đặt service & adapter ở mức **package-private** (như trong mẫu).
6. `mvn compile`.

Module tham chiếu đầy đủ khi cần ví dụ thật: `modules/vehicle`, `modules/customer`.
