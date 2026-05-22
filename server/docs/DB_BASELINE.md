# Baseline Flyway cho DB khôi phục từ dump

## Vấn đề

Trước đây pom **thiếu** module `org.springframework.boot:spring-boot-flyway` (Spring
Boot 4 tách auto-configuration của Flyway ra module riêng), nên `flyway-core` có trên
classpath nhưng **Flyway không hề chạy** khi khởi động. Schema cũ (V001–V110) đi vào DB
qua **dump** (`backupdata/postgres-data.tar.gz`), không qua Flyway → DB không có bảng
`flyway_schema_history`.

Sau khi thêm `spring-boot-flyway`, Flyway chạy. Nhưng baseline tự động
(`baseline-on-migrate`) chỉ kích hoạt khi **schema mặc định (`public`) không rỗng**. Với
DB từ dump, bảng cũ nằm ở schema theo module (`vehicle`, `customer`, ...) còn `public`
trống → Flyway tưởng đây là DB mới và cố chạy lại V001 → lỗi "table already exists".

## Cách xử lý (chạy 1 lần cho mỗi DB khôi phục từ dump)

Tạo `flyway_schema_history` và đánh dấu baseline ở version 110 (migration cũ cuối cùng).
Sau đó Flyway chỉ áp V120 trở đi.

```sql
-- chạy trong DB car_rental
DROP TABLE IF EXISTS public.flyway_schema_history;
CREATE TABLE public.flyway_schema_history (
  installed_rank INT NOT NULL,
  version VARCHAR(50),
  description VARCHAR(200) NOT NULL,
  type VARCHAR(20) NOT NULL,
  script VARCHAR(1000) NOT NULL,
  checksum INTEGER,
  installed_by VARCHAR(100) NOT NULL,
  installed_on TIMESTAMP NOT NULL DEFAULT now(),
  execution_time INTEGER NOT NULL,
  success BOOLEAN NOT NULL,
  CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank)
);
CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history (success);
INSERT INTO public.flyway_schema_history
  (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
VALUES
  (1, '110', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL, 'car_rental', 0, true);
```

Ví dụ chạy qua Docker:

```bash
docker exec -i car-rental-postgres psql -U car_rental -d car_rental < server/docs/baseline.sql
```

Khởi động app sau đó → Flyway áp V120 (location) + V121 (province/commune codes) +
V122 (seed 34 tỉnh + 3321 xã).

## DB rỗng mới

Không cần làm gì: `public` trống, không có bảng cũ → Flyway chạy đầy đủ V001 → V122.
