# Khung kiến trúc module (Hexagonal / Clean Architecture)

Tài liệu này là **chuẩn bắt buộc** cho mọi module trong `modules/`. Khi tạo module
mới, copy bộ khung trong `docs/architecture/module-template/` rồi đổi tên `Sample` →
tên module của bạn. Module tham chiếu hoàn chỉnh nhất: **`vehicle`** (và `customer`).

---

## 1. Nguyên tắc cốt lõi

- **Modular monolith**: mỗi module là một "bounded context", có schema Postgres riêng
  (`vehicle`, `customer`, `fleet`, ...). **Không** đặt FK chéo schema — tham chiếu giữa
  module chỉ bằng giá trị (UUID/code).
- **Hexagonal**: phần `domain` + `application` (lõi nghiệp vụ) **không** phụ thuộc
  framework. Mọi thứ "bẩn" (web, JPA, file, module khác) nằm ở `adapter`.
- **Dependency rule** — phụ thuộc chỉ hướng vào trong:

  ```
  adapter ──> application ──> domain
  adapter ──> application.port (in/out)
  ```

  `domain` không import gì từ `application`/`adapter`. `application` không import
  `adapter`. Adapter mới được phép import Spring/JPA và `api` của module khác.

---

## 2. Cây thư mục chuẩn

```
modules/<module>/
├── api/                     # (tùy chọn) cổng PUBLIC cho module khác dùng
│   ├── <X>Directory.java        # interface tra cứu/kiểm tra cross-module
│   └── <X>Ref.java              # DTO chia sẻ cross-module (record)
├── domain/                  # lõi thuần: aggregate + enum + value object (KHÔNG framework)
│   ├── <X>.java                 # aggregate / entity nghiệp vụ
│   ├── <X>Status.java           # enum trạng thái
│   └── <X>StatusAction.java     # enum hành động (nếu có máy trạng thái)
├── application/
│   ├── command/             # input GHI đã validate:   <Verb><X>Command
│   ├── query/               # input ĐỌC + enum sắp xếp: <X>Query, SortDirection
│   ├── view/                # read model TRẢ RA:        <X>Detail, <X>ListItem, <X>Summary
│   ├── port/in/             # use case (cổng vào):      <Verb><X>UseCase
│   ├── port/out/            # SPI app cần (cổng ra):    Load<X>Port, Write<X>Port, Page<X>Port
│   └── service/             # impl use case:            <X>CommandService, <X>QueryService
└── adapter/
    ├── in/rest/             # REST: Admin<X>Controller, <X>ApiMapper
    │   ├── request/         # body request:  <Verb><X>Request
    │   └── response/        # body response: <X>...Response (+ static fromDomain)
    ├── in/cli/              # (tùy chọn) ApplicationRunner cho import/seed
    └── out/
        ├── persistence/     # impl port: <X>WriteAdapter, <X>LoadAdapter, <X>...Query, <X>...Mapper
        │   ├── entity/      # <X>JpaEntity (tách riêng)
        │   └── repository/  # <X>JpaRepository, <X>JpaSpecifications (tách riêng)
        └── storage/         # (tùy chọn) adapter lưu file/đối tượng
```

> Layer nào không cần thì **bỏ luôn thư mục**, đừng để rỗng. KHÔNG dùng package
> `infrastructure` (đã loại bỏ — thay bằng `adapter/out`).

> **Tách entity/repository:** `*JpaEntity` ở `persistence/entity/`, `*JpaRepository`
> (+ `*JpaSpecifications`) ở `persistence/repository/`. Vì khác package với adapter,
> entity + repository **phải `public`**. Nếu adapter tạo entity bằng `new ...()` thì
> no-arg constructor cũng phải `public` (`@NoArgsConstructor`); nếu chỉ ghi bằng
> native SQL thì có thể để `protected`.

---

## 3. Trách nhiệm & quy tắc đặt tên

| Layer | Trách nhiệm | Quy ước tên | Ví dụ thật |
|---|---|---|---|
| `domain` | Quy tắc nghiệp vụ thuần, không framework | `<X>`, `<X>Status` (enum) | `Vehicle`, `VehicleStatus` |
| `application/command` | Input ghi, **tự validate** trong `from(...)` | `<Verb><X>Command` | `CreateCustomerCommand` |
| `application/query` | Tham số đọc/phân trang | `<X>Query`, `SortDirection` | `ListCustomersQuery` |
| `application/view` | Read model trả ra ngoài | `<X>Detail`, `<X>ListItem`, `<X>Summary` | `CustomerDetail` |
| `application/port/in` | Hợp đồng use case | `<Verb><X>UseCase` | `CreateCustomerUseCase` |
| `application/port/out` | Hợp đồng SPI (DB, module khác) | `Load<X>Port`, `Write<X>Port`, `Page<X>Port` | `WriteCustomerPort` |
| `application/service` | Impl use case, điều phối port | `<X>CommandService`, `<X>QueryService` | `CustomerQueryService` |
| `adapter/in/rest` | Controller + mapper | `Admin<X>Controller`, `<X>ApiMapper` | `AdminCustomerController` |
| `adapter/in/rest/request` | DTO request | `<Verb><X>Request` | `CreateCustomerRequest` |
| `adapter/in/rest/response` | DTO response + `fromDomain` | `<X>...Response` | `AdminCustomerDetailResponse` |
| `adapter/out/persistence` | JPA + native SQL | `<X>JpaEntity`, `<X>JpaRepository`, `<X>WriteAdapter`, `<X>LoadAdapter` | `CustomerWriteAdapter` |
| `api` | Cổng public cross-module | `<X>Directory`, `<X>Ref` | `CustomerDirectory` |

**Quy ước chung:**
- Service & adapter để **package-private** (không `public`) — Spring vẫn quét được; ngăn
  module khác import nhầm nội bộ. Chỉ `port`, `view`, `command`, `api`, request/response để `public`.
- Read model luôn nằm ở `application/view` — **không** để ở `domain`.
- Command tự validate và ném `DomainException.validation(...)` để trả 400 sạch.
- Response có `static fromDomain(view)` để map; field đặt trùng tên JSON mà frontend cần.

---

## 4. Luồng một request (đọc từ ngoài vào trong)

```
HTTP → Admin<X>Controller (adapter/in/rest)
     → <Verb><X>UseCase (port/in)            ← interface
     → <X>Query/CommandService (service)     ← impl, điều phối
     → Load/Write<X>Port (port/out)          ← interface
     → <X>LoadAdapter/<X>WriteAdapter (adapter/out/persistence) → DB
return ← <X>Detail (view) → <X>Response (adapter/in/rest/response) → JSON
```

Controller chỉ phụ thuộc `port/in` + DTO request/response. Service chỉ phụ thuộc
`port/out` + view/command/query. Không tầng nào "nhảy cóc" xuống adapter.

---

## 5. Cross-module (khi module A cần dữ liệu module B)

- Module B expose **`api/<B>Directory`** (interface) + **`api/<B>Ref`** (record).
- Module A import **chỉ** `B.api.*`, không bao giờ import `B.domain`/`B.adapter`/`B.application`.
- Ví dụ: `vehicle` kiểm tra customer qua `customer.api.CustomerDirectory`; resolve
  địa chỉ qua `location.api.AdministrativeUnitDirectory`.

---

## 6. Checklist tạo module mới

1. Copy `docs/architecture/module-template/` vào `modules/<module>/`, đặt trong
   `src/main/java/.../modules/<module>/`.
2. Đổi tên `Sample` → `<X>` và `sample` → `<module>` trong package + tên file/class.
3. Tạo migration Flyway `Vxxx__<module>.sql` (schema riêng cho module).
4. Bỏ các layer không dùng (vd chưa có `api`, `cli`, `storage` thì xóa thư mục).
5. Service & adapter để **package-private**.
6. Tái dùng tiện ích chung ở mục 8 (`SqlLoader` + file `.sql`, `Tuples`,
   `PageResponse.ofPageIndex`, `PageParams`) — không tự viết lại.
7. `mvn compile` để chắc wiring đúng.

---

## 7. Quy trình thêm 1 API mới vào module có sẵn

Áp dụng khi thêm endpoint mới (không tạo module mới). Đi từ **trong ra ngoài**:
viết hợp đồng (port + command + view) trước, ráp adapter sau. Lý do: phần lõi
biên dịch độc lập với Spring → có thể compile sớm để bắt sai contract trước khi
chạm DB/HTTP.

Ví dụ tham chiếu: API `PATCH /admin/customers/{id}/host-status` (suspend / activate
host profile) — xem các file `ChangeHostStatus*` trong `modules/customer/`.

### Thứ tự file cần đụng tới

| # | Tầng | File / vị trí | Việc cần làm |
|---|---|---|---|
| 1 | **FE contract** | `admin-web/src/features/<module>/api/<feature>API.ts` | Khai báo hàm gọi API (URL, method, body, kiểu trả). Đây là **contract** mà BE phải khớp. *(Theo quy tắc FE: mỗi feature có gọi BE bắt buộc có file API riêng.)* |
| 2 | `adapter/in/rest/request` | `<Verb><X>Request.java` | Record DTO cho body. Chỉ kiểu nguyên thuỷ / String. Không validate ở đây. |
| 3 | `application/command` | `<Verb><X>Command.java` | Input đã validate. `from(...)` ném `DomainException.validation(...)` cho mọi giá trị sai. Map action string → enum nội bộ. |
| 4 | `application/port/in` | `<Verb><X>UseCase.java` | Interface 1 phương thức `handle(command)`. Khai báo `@FunctionalInterface`. |
| 5 | `application/view` *(nếu cần)* | `<X>Detail.java` | Thêm method `with<Field>(...)` trả về copy nếu cần biến đổi read model trước khi save. |
| 6 | `application/port/out` *(nếu cần)* | `Write<X>Port.java` / `Load<X>Port.java` | Thêm phương thức nếu DB operation chưa tồn tại (vd `saveHostStatus`). **Bỏ qua** nếu tái dùng được port có sẵn. |
| 7 | `adapter/out/persistence` *(nếu cần)* | `<X>WriteAdapter.java` / `<X>LoadAdapter.java` | Implement phương thức port out vừa thêm. JPA cho update đơn giản; native SQL (SQL ở file `.sql` load qua `SqlLoader`, map `Tuple` bằng `shared.persistence.Tuples`, phân trang bằng `PageResponse.ofPageIndex`) nếu chưa có entity / join chéo schema. Xem mục 8. |
| 8 | `application/service` | `<X>CommandService.java` / `<X>QueryService.java` | Thêm `implements <Verb><X>UseCase`. Implement `handle(command)`: load → validate domain → save → reload + return view. |
| 9 | `adapter/in/rest` | `Admin<X>Controller.java` | Inject use case mới vào constructor. Thêm method `@PatchMapping`/`@PostMapping`/... gọi `command.from(...)` rồi `usecase.handle(...)`, bọc bằng `ApiResponse.success(...)`. |
| 10 | **Verify** | terminal | `./mvnw -q -DskipTests compile`. Sau đó smoke test bằng `curl` hoặc trực tiếp trên FE. |

### Quy tắc rút ra

- **Validate ở command, không ở controller/request**. Request chỉ là cái túi
  chứa dữ liệu thô.
- **Service luôn reload sau khi ghi** để response phản ánh state hiện tại
  (kể cả các field do DB trigger/sub-query suy ra như `activity`).
- **Domain check trong service**, không trong adapter: vd "customer chưa có
  host profile" → `DomainException.validation(...)` ở service, không phải để
  UPDATE chạy ra 0 row rồi đoán lỗi ở adapter.
- **Tái dùng port out trước khi tạo mới**. Nếu chỉ đổi 1 field thì có thể
  thêm `with<Field>(...)` vào view rồi reuse `save<X>Status` có sẵn — nhưng
  nếu ghi vào bảng khác (vd `host_profiles` thay vì `customers`) thì phải
  thêm method port out mới để không phá ngữ nghĩa của method cũ.
- **Đường URL đi theo resource**, không theo entity riêng: host status vẫn ở
  `/admin/customers/{id}/host-status` vì host profile là sub-resource của
  customer. Khi nào host nở thành module riêng mới đổi sang `/admin/hosts/...`.

---

## 8. Tiện ích dùng chung (`shared/`)

Dùng các tiện ích này thay vì tự viết lại trong từng module — giữ pattern thống
nhất, tránh lệch logic.

| Tiện ích | Vị trí | Khi nào dùng |
|---|---|---|
| `SqlLoader` (interface) + `ClasspathSqlQueryLoader` | `shared/sql/` | Native SQL phải để ở file `resources/sql/<module>/*.sql`, **không** viết inline trong Java. Adapter inject `SqlLoader`, gọi `sql.load(<Module>SqlPaths.X)`. Mỗi module có 1 class `<Module>SqlPaths` (package-private) giữ hằng số path. Adapter vẫn tự execute + bind param + map. |
| `Tuples` | `shared/persistence/` | Map giá trị cột từ native-query `Tuple`: `Tuples.uuid(t,"col")`, `dateTime`, `localDate`, `intValue/longValue/bigDecimal(t.get("col"))`. **Không** tự viết lại helper map trong adapter. |
| `PageResponse.ofPageIndex(items, total, pageIndex, size)` | `shared/api/` | Build kết quả phân trang từ pageIndex 0-based — tự tính page/totalPages/hasNext/hasPrevious. Convention: rỗng → totalPages = 1. |
| `PageParams.normalize(page, size, defaultSize, maxSize)` | `shared/api/` | Chuẩn hóa page/size trong `*Query.from(...)`. Mỗi endpoint tự truyền `defaultSize`/`maxSize` (giới hạn có chủ đích khác nhau). |
| `ApiResponse` / `DomainException` / `ErrorCode` | `shared/api`, `shared/error` | Bọc response & ném lỗi nghiệp vụ (đã chuẩn, dùng như mẫu hiện có). |

## 9. Tham chiếu

- Module mẫu đầy đủ: `modules/vehicle` (CRUD + listing + ảnh + giá + phân trang).
- Bộ khung copy nhanh: `docs/architecture/module-template/` (file `.java` mẫu, **không**
  nằm trên classpath nên không bị build — chỉ để copy).
