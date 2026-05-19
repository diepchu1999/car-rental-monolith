# Giải thích source backend dự án car-rental-monolith

Tài liệu này giải thích phần backend hiện có trong workspace tại thời điểm đọc source. Phần backend chính nằm trong `pom.xml`, `src/main`, `src/test`, `infra/docker` và các tài liệu kiến trúc trong `docs/architecture`.

Các thư mục `apps/admin-web` và `templateui` là frontend/admin UI nên chỉ được nhắc ở mức bối cảnh, không đi sâu trong tài liệu backend này.

## 1. Tổng quan dự án

Dự án là một Spring Boot monolith cho hệ thống thuê xe. "Monolith" nghĩa là backend được build và chạy như một ứng dụng duy nhất, nhưng source lại được chia theo module nghiệp vụ như `vehicle`, `booking`, `payment`, `driver`, `customer`.

Cách chia này thường gọi là modular monolith:

- Chỉ deploy một app nên đơn giản hơn microservice.
- Code vẫn tách theo nghiệp vụ để dễ bảo trì.
- Database dùng schema riêng theo module, ví dụ `vehicle.vehicles`, `booking.bookings`, `payment.transactions`.
- Khi sau này cần tách service, ranh giới module và schema đã rõ hơn.

Hiện tại module Java triển khai đầy đủ nhất là `vehicle`. Các module khác đã có schema database qua Flyway, nhưng thư mục Java của chúng mới là khung rỗng.

## 2. Luồng request backend hiện tại

API đang triển khai cho admin xem danh sách xe:

```text
HTTP request
  -> AdminVehicleController
  -> ListVehiclesUseCase / PageVehiclesUseCase
  -> VehicleQueryService
  -> LoadVehiclePort
  -> VehiclePersistenceAdapter
  -> VehicleJpaRepository
  -> PostgreSQL table vehicle.vehicles
  -> VehicleJpaEntity
  -> Vehicle domain
  -> VehicleResponse
  -> ApiResponse JSON
```

Vì sao không để controller gọi thẳng repository:

- Controller chỉ nên xử lý HTTP, query param, response.
- Application service chứa logic use case.
- Repository/JPA là chi tiết hạ tầng, không nên lan vào domain.
- Port/interface giúp sau này đổi cách lưu dữ liệu hoặc test mock dễ hơn.

## 3. Thư mục gốc

| Đường dẫn | Tác dụng | Vì sao cần |
| --- | --- | --- |
| `.gitattributes` | Cấu hình line ending cho `mvnw` và file `.cmd`. | Tránh lỗi chạy script giữa Windows/Linux do khác kiểu xuống dòng. |
| `.gitignore` | Bỏ qua file build, IDE, Maven wrapper jar, `target/`. | Giữ repository sạch, không commit file sinh ra từ máy cá nhân hoặc build. |
| `.mvn/wrapper/maven-wrapper.properties` | Cấu hình Maven Wrapper dùng Maven `3.9.15`. | Người khác có thể chạy Maven bằng `mvnw` mà không cần cài Maven sẵn. |
| `mvnw` | Maven Wrapper script cho Linux/macOS. | Chạy build/test nhất quán trên Unix-like environment. |
| `mvnw.cmd` | Maven Wrapper script cho Windows. | Chạy build/test nhất quán trên Windows. |
| `pom.xml` | File Maven chính: khai báo Spring Boot, Java version, dependencies, plugins. | Đây là trung tâm build backend Java. |
| `README.md` | README hiện chỉ có tên dự án. | Nơi nên bổ sung hướng dẫn chạy dự án. |
| `HELP.md` | File Spring Initializr tạo ra, chứa link tài liệu Spring. | Hữu ích lúc khởi tạo, nhưng hiện bị `.gitignore` bỏ qua nên không nên xem là tài liệu chính của team. |
| `target/` | Output build Maven. | File sinh ra, không phải source. Không cần đọc/sửa thủ công. |
| `.idea/`, `.claude/`, `.git/` | Cấu hình local IDE/tool/git. | Không phải source backend. |

## 4. `pom.xml`

`pom.xml` dùng Spring Boot parent `4.0.6`, group `com.ares`, artifact `car-rental-monolith`, Java `26`.

Các dependency chính:

| Dependency | Tác dụng | Vì sao dùng |
| --- | --- | --- |
| `spring-boot-starter-webmvc` | Tạo REST API bằng Spring MVC. | Controller như `AdminVehicleController` cần nhận HTTP request. |
| `spring-boot-starter-data-jpa` | Tích hợp JPA/Hibernate và Spring Data Repository. | Module `vehicle` đang đọc DB bằng `JpaRepository`. |
| `spring-boot-starter-validation` | Hỗ trợ validation request. | Dự án đã có `RestExceptionHandler` cho lỗi validation, dù hiện API vehicle chưa dùng DTO validation nhiều. |
| `spring-boot-starter-actuator` | Health check, metrics, operational endpoints. | Cần cho monitoring app khi chạy thật. |
| `micrometer-registry-prometheus` | Export metrics cho Prometheus. | Kết hợp với Actuator để giám sát. |
| `flyway-core` | Chạy database migration. | DB schema được quản lý bằng SQL versioned migration thay vì Hibernate tự tạo. |
| `flyway-database-postgresql` | Hỗ trợ Flyway cho PostgreSQL. | Vì DB chính là PostgreSQL. |
| `postgresql` | JDBC driver PostgreSQL. | App cần driver để kết nối DB. |
| `p6spy` | Log SQL đi qua JDBC. | Giúp debug SQL thực tế Hibernate/JPA gửi xuống DB. |
| `lombok` | Sinh getter/setter/no-args constructor ở compile time. | `VehicleJpaEntity` dùng Lombok để giảm boilerplate. |
| `spring-boot-devtools` | Tiện ích dev runtime. | Hỗ trợ restart nhanh khi code trong môi trường local. |

Phần plugin:

- `spring-boot-maven-plugin`: đóng gói/chạy app Spring Boot.
- `maven-compiler-plugin`: cấu hình Lombok annotation processor cho compile và test-compile.

Vì sao `lombok` bị exclude khỏi Spring Boot plugin: Lombok chỉ cần lúc compile, không cần đóng vào runtime artifact.

## 5. `infra/docker`

### `infra/docker/docker-compose.yaml`

File này tạo môi trường local gồm:

- `postgres`: PostgreSQL 16 Alpine, DB mặc định `car_rental`, user `car_rental`.
- `pgadmin`: giao diện quản trị PostgreSQL ở port mặc định `5050`.
- `postgres-data`, `pgadmin-data`: volume giữ dữ liệu qua các lần restart container.
- `car-rental-net`: bridge network để các service nói chuyện với nhau.

Vì sao cần:

- Dev không phải cài PostgreSQL thủ công.
- DB local giống cấu hình `application.yaml`.
- Có healthcheck để `pgadmin` chỉ start sau khi Postgres sẵn sàng.

## 6. `src/main/resources`

### `src/main/resources/application.yaml`

Cấu hình runtime chính của Spring Boot.

Các phần quan trọng:

- `spring.application.name`: đặt tên app là `car-rental-monolith`.
- `spring.datasource`: kết nối PostgreSQL qua P6Spy driver.
- `hikari`: cấu hình connection pool.
- `spring.flyway.enabled: true`: bật Flyway.
- `spring.flyway.locations: classpath:db/migration`: chỉ định nơi chứa migration SQL.
- `spring.jpa.hibernate.ddl-auto: none`: không cho Hibernate tự tạo/sửa schema.
- `spring.jpa.open-in-view: false`: không giữ Hibernate session mở xuyên suốt view/request.
- `hibernate.default_schema: public`: default schema nếu entity không chỉ định schema.
- `hibernate.jdbc.batch_size`, `order_inserts`, `order_updates`: tối ưu batch SQL.
- `logging.level`: tắt log SQL mặc định của Hibernate, dùng P6Spy thay thế.

Vì sao `ddl-auto: none`: database production cần thay đổi có kiểm soát. Flyway lưu version, checksum và lịch sử chạy trong bảng `flyway_schema_history`, tránh việc app tự ý thay đổi schema khi start.

### `src/main/resources/spy.properties`

Cấu hình P6Spy:

- Dùng `Slf4JLogger`.
- Dùng formatter custom `P6SpySqlFormatter`.
- Loại bớt category log như `result`, `resultset`, `batch`.

Vì sao cần: log SQL rõ ràng hơn khi debug, nhưng không làm log quá nhiễu.

### `src/main/resources/static`

Thư mục rỗng theo convention Spring Boot để chứa static file nếu backend phục vụ asset trực tiếp.

Hiện tại frontend nằm ở `apps/admin-web`, nên backend chưa dùng thư mục này.

### `src/main/resources/templates`

Thư mục rỗng theo convention Spring MVC/Thymeleaf để chứa server-rendered template.

Hiện tại backend là REST API, không render HTML server-side.

## 7. Flyway migration

Các migration nằm trong `src/main/resources/db/migration`.

Flyway chạy file theo version trong tên file:

```text
V001__...
V005__...
V010__...
...
V110__...
```

Mỗi migration đã chạy sẽ được ghi vào bảng `flyway_schema_history`. Lần sau app start, Flyway chỉ chạy file mới chưa có trong lịch sử, không chạy lại toàn bộ SQL.

### `V001__create_extensions_and_schemas.sql`

Tạo extension `pgcrypto` và các schema:

- `common`
- `identity`
- `customer`
- `vehicle`
- `fleet`
- `driver`
- `pricing`
- `booking`
- `payment`
- `review`
- `notification`
- `admin`

Vì sao làm vậy:

- `pgcrypto` cung cấp `gen_random_uuid()` để DB tự tạo UUID.
- Schema-per-module giúp database cũng có ranh giới giống code module.

### `V005__common.sql`

Tạo:

- `common.outbox_events`: lưu event cần publish ra ngoài.
- `common.idempotency_keys`: chống xử lý trùng request.

Vì sao cần:

- Outbox pattern giúp lưu event cùng transaction với nghiệp vụ, sau đó worker publish sang Kafka/RabbitMQ/email... mà không mất event.
- Idempotency giúp API thanh toán/booking không tạo dữ liệu trùng khi client retry.

### `V010__identity.sql`

Tạo:

- `identity.users`
- `identity.roles`
- `identity.permissions`
- `identity.user_roles`
- `identity.role_permissions`
- `identity.login_sessions`

Vì sao cần:

- Tách identity khỏi customer. Một user đăng nhập có thể liên kết customer profile, admin role, session.
- Unique index cho phone/email/external subject tránh trùng định danh.
- `login_sessions` lưu refresh token hash và trạng thái revoke/expire.

### `V020__customer.sql`

Tạo:

- `customer.customers`
- `customer.customer_roles`
- `customer.host_profiles`
- `customer.kyc_profiles`
- `customer.kyc_documents`
- `customer.addresses`

Vì sao cần:

- `customers` là profile người dùng trong domain thuê xe.
- `customer_roles` cho phép một customer vừa là renter vừa là host.
- `host_profiles` chứa thông tin riêng của chủ xe.
- `kyc_profiles` và `kyc_documents` phục vụ xác minh danh tính.
- `addresses` phục vụ địa chỉ giao/nhận xe hoặc hồ sơ.

### `V030__vehicle.sql`

Tạo:

- `vehicle.vehicles`
- `vehicle.vehicle_listings`
- `vehicle.vehicle_images`
- `vehicle.vehicle_features`
- `vehicle.availability_blocks`

Vì sao cần:

- `vehicles` là bảng gốc của xe, chứa brand/model/license/status.
- `source` phân biệt `HOST_OWNED` và `COMPANY_OWNED`.
- `vehicle_listings` chứa dữ liệu đăng bán/cho thuê: title, city, rate, status.
- `vehicle_images` lưu ảnh.
- `vehicle_features` lưu tiện ích xe.
- `availability_blocks` chặn lịch xe do booking, bảo trì, host/admin block.

File Java hiện tại map trực tiếp vào bảng `vehicle.vehicles`.

### `V031__vehicle_search_indexes.sql`

Tạo extension `pg_trgm` và GIN trigram indexes cho:

- `brand`
- `model`
- `version`
- `license_plate`

Vì sao cần:

- API phân trang có search bằng `ILIKE '%keyword%'`.
- Nếu không có trigram index, Postgres dễ phải scan toàn bảng.
- Với dữ liệu xe tăng lớn, index này giúp search nhanh hơn.

### `V040__fleet.sql`

Tạo:

- `fleet.branches`
- `fleet.company_vehicles`
- `fleet.maintenance_records`
- `fleet.insurance_policies`
- `fleet.inspection_records`

Vì sao cần:

- Fleet quản lý xe công ty, khác với xe host đăng lên marketplace.
- `company_vehicles.vehicle_id` liên kết logic tới `vehicle.vehicles` bằng UUID, nhưng không đặt foreign key cross-schema.
- Maintenance, insurance, inspection là dữ liệu vận hành đội xe.

### `V050__driver.sql`

Tạo:

- `driver.drivers`
- `driver.driver_documents`
- `driver.availability_slots`
- `driver.driver_assignments`

Vì sao cần:

- Hỗ trợ booking có tài xế.
- Lưu hồ sơ tài xế, giấy tờ, lịch rảnh và phân công vào booking.

### `V060__pricing.sql`

Tạo:

- `pricing.price_plans`
- `pricing.promotions`
- `pricing.quotes`
- `pricing.quote_items`
- `pricing.promotion_redemptions`

Vì sao cần:

- Pricing tách riêng khỏi booking để tính giá trước khi khách đặt.
- `quotes` là báo giá có thời hạn.
- `quote_items` lưu từng dòng tiền như giá thuê, phí tài xế, giảm giá, đặt cọc.
- Promotions và redemption giúp kiểm soát mã giảm giá.

### `V070__booking.sql`

Tạo:

- `booking.bookings`
- `booking.booking_status_history`
- `booking.trip_checklists`
- `booking.trip_checklist_items`
- `booking.booking_cancellations`

Vì sao cần:

- `bookings` là trung tâm vận hành thuê xe.
- Lưu snapshot xe/khách/tài xế bằng JSONB để booking cũ không đổi khi profile hiện tại thay đổi.
- Status history giúp audit luồng trạng thái.
- Checklist pickup/return phục vụ bàn giao xe.
- Cancellations lưu lý do và refund.

### `V080__payment.sql`

Tạo:

- `payment.payment_intents`
- `payment.transactions`
- `payment.refunds`
- `payment.payouts`

Vì sao cần:

- `payment_intents` biểu diễn ý định thanh toán cho booking.
- `transactions` lưu giao dịch thực tế từ provider.
- `refunds` xử lý hoàn tiền.
- `payouts` xử lý chi trả cho host/driver/company.

### `V090__review.sql`

Tạo:

- `review.reviews`
- `review.review_replies`
- `review.review_reports`

Vì sao cần:

- Lưu đánh giá sau booking.
- Cho phép reply và report nội dung review.
- Index theo target giúp lấy review của xe/tài xế/host nhanh.

### `V100__notification.sql`

Tạo:

- `notification.templates`
- `notification.notifications`
- `notification.delivery_logs`
- `notification.devices`

Vì sao cần:

- Template giúp chuẩn hóa nội dung email/SMS/push/in-app.
- Notifications lưu từng thông báo gửi cho user/customer.
- Delivery logs lưu kết quả provider.
- Devices lưu device token cho push notification.

### `V110__admin.sql`

Tạo:

- `admin.audit_logs`
- `admin.backoffice_tasks`
- `admin.dashboard_snapshots`

Vì sao cần:

- Audit logs lưu hành động admin.
- Backoffice tasks hỗ trợ đội vận hành xử lý hồ sơ, booking, khiếu nại.
- Dashboard snapshots lưu số liệu tổng hợp theo ngày/metric.

### Các thư mục con trong `db/migration`

Trong workspace có các thư mục rỗng như `db/migration/vehicle`, `db/migration/booking`, `db/migration/payment`.

Hiện `application.yaml` chỉ trỏ Flyway tới `classpath:db/migration`, nên các SQL versioned đang đặt trực tiếp trong `db/migration`. Nếu muốn tách migration theo module bằng thư mục con, cần cấu hình thêm Flyway locations hoặc dùng pattern phù hợp.

## 8. Seed data

### `src/main/resources/db/seed/dev/seed_vehicles.sql`

File này tạo dữ liệu mẫu 100 xe từ 25 template x 4 batch.

Đặc điểm:

- Sinh brand/model/version/seats/transmission/fuel type.
- Xen kẽ `HOST_OWNED` và `COMPANY_OWNED`.
- Sinh license plate dạng `51T-...`.
- Phân bố status gồm `ACTIVE`, `DRAFT`, `PENDING_REVIEW`, `INACTIVE`, `SUSPENDED`.
- Dùng `ON CONFLICT (license_plate) DO NOTHING` để chạy lại không insert trùng.

Vì sao không nằm trong Flyway migration chính:

- Đây là data dev/test, không nên tự động đẩy vào mọi môi trường.
- Migration chính nên tạo schema ổn định; seed dev nên chạy có chủ đích.

## 9. Java package tổng quan

Backend package gốc là:

```text
com.ares.car_rental_monolith
```

Tên package dùng underscore vì Java package không hợp lệ nếu chứa dấu gạch ngang. `HELP.md` cũng ghi nhận điều này.

### `CarRentalMonolithApplication.java`

Class main của Spring Boot:

- Có `@SpringBootApplication`.
- Gọi `SpringApplication.run(...)`.

Vì sao cần: đây là entry point để boot toàn bộ Spring context, auto-configuration, component scan, controller, service, repository.

## 10. `shared`

`shared` chứa code dùng chung cho nhiều module.

### `shared/api/ApiResponse.java`

Record response envelope:

- `success`
- `code`
- `message`
- `data`
- `timestamp`

Có factory:

- `success(...)`
- `error(...)`

Vì sao cần: API trả JSON thống nhất, frontend dễ xử lý thành công/lỗi.

Ví dụ response thành công:

```json
{
  "success": true,
  "code": "VEHICLE_LIST_FETCHED",
  "message": "Vehicle list fetched successfully",
  "data": {},
  "timestamp": "..."
}
```

### `shared/api/ListResponse.java`

Record cho danh sách không phân trang:

- `items`
- `total`

Factory `of(items)` tự tính `total = items.size()`.

Vì sao cần: response list luôn có tổng số item, frontend không phải tự suy luận.

### `shared/api/PageResponse.java`

Record cho danh sách phân trang:

- `items`
- `total`
- `page`
- `size`
- `totalPages`
- `hasNext`
- `hasPrevious`

Có method `map(...)` để đổi kiểu item mà giữ metadata phân trang.

Vì sao cần: service có thể trả `PageResponse<Vehicle>`, controller map sang `PageResponse<VehicleResponse>` mà không mất thông tin page.

### `shared/config/CorsConfig.java`

Cho phép CORS với:

- `http://localhost:5173`
- `http://127.0.0.1:5173`

Áp dụng cho `/api/**`, method `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`.

Vì sao cần: frontend Vite dev server chạy port `5173`, khác origin với backend, nên browser cần CORS.

### `shared/config/P6SpySqlFormatter.java`

Formatter custom cho SQL log của P6Spy:

- Bỏ qua SQL rỗng.
- In elapsed time và category.
- Format `select`, `insert`, `update`, `delete` dễ đọc hơn.

Vì sao cần: khi debug JPA, log SQL gốc thường khó đọc. Formatter giúp đọc query nhanh hơn.

### `shared/error/ErrorCode.java`

Enum lỗi chung:

- `VALIDATION_ERROR`
- `INVALID_PARAMETER`
- `NOT_FOUND`
- `CONFLICT`
- `FORBIDDEN`
- `UNAUTHORIZED`
- `INTERNAL_ERROR`

Mỗi code map với một `HttpStatus`.

Vì sao cần: domain/service chỉ cần nói lỗi loại gì, handler sẽ quyết định HTTP status.

### `shared/error/DomainException.java`

Runtime exception cho lỗi nghiệp vụ.

Có factory:

- `notFound`
- `validation`
- `conflict`
- `forbidden`
- `unauthorized`

Vì sao cần: service ném lỗi nghiệp vụ thống nhất, không phụ thuộc trực tiếp vào HTTP.

### `shared/error/RestExceptionHandler.java`

Global exception handler với `@RestControllerAdvice`.

Xử lý:

- `DomainException`
- `MethodArgumentNotValidException`
- `MethodArgumentTypeMismatchException`
- `Exception`

Vì sao cần: controller không phải try/catch lặp lại. Mọi lỗi REST trả về `ApiResponse.error(...)` cùng format.

## 11. Module `vehicle`

Module `vehicle` đang dùng phong cách ports/adapters.

```text
vehicle
  domain
  application
    query
    port/in
    port/out
    service
  adapter
    in/rest
    out/persistence
```

Ý nghĩa:

- `domain`: mô hình nghiệp vụ thuần.
- `application`: use case và logic điều phối.
- `adapter/in`: cách bên ngoài gọi vào app, hiện là REST.
- `adapter/out`: cách app gọi ra ngoài, hiện là PostgreSQL/JPA.

### `vehicle/domain/Vehicle.java`

Record domain biểu diễn xe:

- ID, owner/fleet reference.
- Source: host-owned/company-owned.
- Brand/model/version/year/license/seats.
- Transmission/fuel/status.
- Created/updated timestamp.

Method tiện ích:

- `isActive()`
- `isHostOwned()`
- `isCompanyOwned()`

Vì sao cần: domain model không phụ thuộc JPA annotation hay HTTP response, nên sạch và dễ test.

### `vehicle/domain/VehicleSource.java`

Enum:

- `HOST_OWNED`
- `COMPANY_OWNED`

Vì sao cần: phân biệt xe của host với xe công ty, ảnh hưởng booking/fleet/pricing.

### `vehicle/domain/VehicleStatus.java`

Enum:

- `DRAFT`
- `PENDING_REVIEW`
- `ACTIVE`
- `INACTIVE`
- `SUSPENDED`

Vì sao cần: quản lý vòng đời xe từ nháp, chờ duyệt, hoạt động, tạm dừng.

### `vehicle/domain/VehicleTransmission.java`

Enum:

- `MANUAL`
- `AUTOMATIC`

Vì sao cần: dữ liệu kỹ thuật xe dùng để filter/search/display.

### `vehicle/domain/VehicleFuelType.java`

Enum:

- `GASOLINE`
- `DIESEL`
- `ELECTRIC`
- `HYBRID`

Vì sao cần: dữ liệu kỹ thuật xe và filter.

### `vehicle/application/query/ListVehiclesQuery.java`

Query object cho API list không phân trang.

Nhận string `source`, `status` từ request rồi parse sang enum qua `Enums.parse`.

Vì sao cần: gom input của use case vào một object rõ nghĩa, tránh truyền nhiều tham số rời.

Lưu ý hành vi hiện tại: nếu enum không hợp lệ, `Enums.parse` trả `null`, nghĩa là bỏ filter đó thay vì báo lỗi.

### `vehicle/application/query/PageVehiclesQuery.java`

Query object cho API phân trang.

Chứa:

- `q`
- `source`
- `status`
- `page`
- `size`

Tự chuẩn hóa:

- `page` mặc định `1`, nhỏ hơn 1 thì đưa về `1`.
- `size` mặc định `20`, nhỏ hơn 1 thì về `20`, tối đa `100`.
- `q` trim và lowercase, blank thành chuỗi rỗng.

Vì sao cần: bảo vệ repository khỏi input xấu và thống nhất quy tắc paging.

### `vehicle/application/query/Enums.java`

Utility package-private để parse string sang enum.

Vì sao package-private: chỉ dùng trong package query, không mở API thừa ra ngoài.

### `vehicle/application/port/in/ListVehiclesUseCase.java`

Inbound port cho use case lấy danh sách xe không phân trang.

```java
List<Vehicle> handle(ListVehiclesQuery query);
```

Vì sao cần: controller phụ thuộc vào interface use case, không phụ thuộc class service cụ thể.

### `vehicle/application/port/in/PageVehiclesUseCase.java`

Inbound port cho use case lấy danh sách xe phân trang.

```java
PageResponse<Vehicle> handle(PageVehiclesQuery query);
```

Vì sao cần: tách hợp đồng use case phân trang khỏi REST/JPA.

### `vehicle/application/port/out/LoadVehiclePort.java`

Outbound port cho việc load vehicle từ storage.

Có:

- `loadVehicles(ListVehiclesQuery query)`
- `loadVehiclePage(PageVehiclesQuery query)`

Vì sao cần: application service không biết dữ liệu đến từ JPA, API khác, cache hay file. Nó chỉ biết cần load vehicle.

### `vehicle/application/service/VehicleQueryService.java`

Service implement cả:

- `ListVehiclesUseCase`
- `PageVehiclesUseCase`

Nó inject `LoadVehiclePort` và delegate việc load dữ liệu.

Có `@Transactional(readOnly = true)`.

Vì sao cần:

- Đây là lớp điều phối use case.
- `readOnly = true` báo transaction chỉ đọc, giúp Hibernate/DB tối ưu và tránh ghi ngoài ý muốn.
- Class package-private, controller chỉ thấy interface public.

### `vehicle/adapter/in/rest/AdminVehicleController.java`

REST controller cho endpoint:

- `GET /api/v1/admin/vehicles`
- `GET /api/v1/admin/vehicles/paged`

Endpoint list nhận:

- `source`
- `status`

Endpoint paged nhận:

- `q`
- `source`
- `status`
- `page`
- `size`

Response được wrap bằng `ApiResponse.success(...)`.

Vì sao cần: đây là adapter HTTP cho admin UI. Nó chuyển HTTP query param thành query object, gọi use case, rồi map domain sang response DTO.

### `vehicle/adapter/in/rest/VehicleApiMapper.java`

Mapper từ domain `Vehicle` sang `VehicleResponse`.

Enum được convert sang string bằng `name()`.

Vì sao cần: không trả domain object trực tiếp ra API. DTO giúp API contract ổn định hơn khi domain thay đổi.

### `vehicle/adapter/in/rest/VehicleResponse.java`

Record DTO trả về frontend.

Chứa các field xe ở dạng dễ serialize JSON, enum thành string.

Vì sao cần: định nghĩa rõ shape JSON của API.

### `vehicle/adapter/out/persistence/VehicleJpaEntity.java`

JPA entity map tới bảng:

```text
vehicle.vehicles
```

Dùng:

- `@Entity`
- `@Table(name = "vehicles", schema = "vehicle")`
- `@Id`
- `@Column`
- `@Enumerated(EnumType.STRING)`
- Lombok `@Getter`, `@Setter`, `@NoArgsConstructor`

Vì sao cần:

- JPA cần entity để map row DB thành object.
- `EnumType.STRING` lưu enum bằng text như `ACTIVE`, tránh lỗi khi đổi thứ tự enum.
- Schema chỉ rõ `vehicle` vì database dùng schema-per-module.

### `vehicle/adapter/out/persistence/VehicleJpaRepository.java`

Spring Data repository:

- Extend `JpaRepository<VehicleJpaEntity, UUID>`.
- Extend `JpaSpecificationExecutor<VehicleJpaEntity>`.
- Có native query `search(...)` cho phân trang/search.

Native query dùng:

- `ILIKE CONCAT('%', :q, '%')` trên brand/model/version/license_plate.
- Filter `source`, `status`.
- `ORDER BY v.created_at DESC`.
- `countQuery` riêng cho pagination.

Vì sao cần:

- `JpaRepository` cho CRUD mặc định.
- `JpaSpecificationExecutor` hỗ trợ filter động ở API list.
- Native query dùng PostgreSQL `ILIKE` để tận dụng trigram index trong `V031`.

### `vehicle/adapter/out/persistence/VehicleJpaSpecifications.java`

Tạo JPA `Specification` từ `ListVehiclesQuery`.

Nếu `source` khác null thì thêm predicate source.
Nếu `status` khác null thì thêm predicate status.

Vì sao cần: filter động sạch hơn việc viết nhiều method repository như `findBySource`, `findByStatus`, `findBySourceAndStatus`.

### `vehicle/adapter/out/persistence/VehiclePersistenceAdapter.java`

Adapter implement `LoadVehiclePort`.

Method:

- `loadVehicles`: dùng specification + sort `createdAt DESC`.
- `loadVehiclePage`: gọi native query `repository.search(...)`, rồi convert Spring `Page` sang `PageResponse`.

Vì sao cần: đây là lớp nối application port với JPA infrastructure. Nếu sau này dùng QueryDSL, JDBC, Elasticsearch, cache..., chỉ cần đổi adapter thay vì đổi use case/controller.

### `vehicle/adapter/out/persistence/VehiclePersistenceMapper.java`

Mapper từ `VehicleJpaEntity` sang domain `Vehicle`.

Vì sao cần: tách entity persistence khỏi domain. Domain không bị annotation JPA hoặc getter/setter Lombok chi phối.

## 12. Các module Java đang là khung rỗng

Trong `src/main/java/com/ares/car_rental_monolith/modules`, các module sau có thư mục nhưng chưa có file Java:

- `admin`
- `booking`
- `customer`
- `driver`
- `fleet`
- `identity`
- `notification`
- `payment`
- `pricing`
- `review`

Mỗi module có các thư mục con kiểu:

- `api`
- `application`
- `domain`
- `infrastructure`

Vì sao có khung này:

- Dự án đã định hướng chia module theo nghiệp vụ.
- `api` sẽ chứa REST controller/request/response.
- `application` sẽ chứa use case/service/port.
- `domain` sẽ chứa entity/value object/business rule thuần.
- `infrastructure` sẽ chứa repository, integration, persistence adapter.

Lưu ý: module `vehicle` đang dùng tên `adapter/in`, `adapter/out` thay vì `api/infrastructure`. Khi phát triển tiếp, nên thống nhất một style để codebase dễ đọc.

## 13. Test

### `src/test/java/com/ares/car_rental_monolith/CarRentalMonolithApplicationTests.java`

Test mặc định:

```java
@SpringBootTest
class CarRentalMonolithApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

Vì sao cần: kiểm tra Spring context có start được không. Đây là smoke test cơ bản, chưa kiểm tra nghiệp vụ vehicle.

Các thư mục test module như `modules/vehicle`, `modules/booking`, ... hiện đang rỗng.

## 14. `docs/architecture`

### `docs/architecture/database.md`

Tài liệu text mô tả database:

- Dùng PostgreSQL.
- Schema-per-module.
- Không tạo foreign key cross business schema.
- Cross-module reference lưu UUID và snapshot.
- `booking.bookings` là trung tâm vận hành.

Vì sao cần: ghi lại rule kiến trúc DB để migration và code sau này không phá ranh giới module.

### `docs/architecture/database-diagram.md`

Tài liệu chi tiết hơn về database diagram.

Vì sao cần: giúp đọc quan hệ bảng nhanh hơn SQL thô.

### `docs/architecture/database-diagram.doc` và `database-diagram.docx`

Bản Word của database diagram.

Vì sao cần: dễ chia sẻ cho người không đọc Markdown hoặc cần tài liệu bàn giao.

### Các file `.png`

- `car_rental_architecture.png`
- `database-booking-flow.png`
- `database-overview.png`
- `database-table-map.png`

Vì sao cần: hình minh họa kiến trúc, flow booking và bản đồ bảng, giúp onboarding nhanh hơn.

## 15. Bối cảnh frontend trong repo

Không thuộc backend, nhưng có liên quan khi chạy local:

- `apps/admin-web`: React/Vite admin web đang gọi API backend.
- `templateui/aresdrive-admin`: template HTML/CSS/JS tĩnh.

`CorsConfig` đang mở CORS cho Vite dev server port `5173`, nên backend đã chuẩn bị để admin web local gọi API.

## 16. Vì sao dự án làm theo cách này

### Tách module theo nghiệp vụ

Mỗi module như vehicle, booking, payment có boundary riêng. Điều này giúp:

- Tránh code booking phụ thuộc lung tung vào payment/vehicle internals.
- Dễ giao việc theo module.
- Dễ tách service trong tương lai nếu cần.

### Tách schema database theo module

Ví dụ:

- Bảng xe nằm trong `vehicle`.
- Bảng booking nằm trong `booking`.
- Bảng thanh toán nằm trong `payment`.

Lợi ích:

- Nhìn DB biết bảng thuộc domain nào.
- Giảm khả năng module này sửa nhầm bảng module khác.
- Chuẩn bị cho khả năng tách database/service sau này.

### Không dùng foreign key cross-schema cho business modules

Theo `docs/architecture/database.md`, cross-module reference lưu bằng UUID và snapshot.

Lợi ích:

- Booking vẫn ổn định dù thông tin xe/khách/tài xế thay đổi sau này.
- Module ít bị khóa chặt vào nhau ở tầng database.
- Dễ migrate/tách module hơn.

Đổi lại:

- Code application phải tự đảm bảo consistency.
- Cần test nghiệp vụ kỹ hơn vì DB không enforce mọi quan hệ.

### Flyway quản lý schema

Lợi ích:

- Mỗi thay đổi DB có version.
- Môi trường dev/test/prod chạy cùng migration.
- Tránh Hibernate tự sửa DB ngoài kiểm soát.

Quy tắc nên theo:

- Không sửa migration cũ đã chạy trên DB.
- Tạo file mới như `V120__add_xxx.sql` cho thay đổi mới.
- Kiểm tra `flyway_schema_history` khi cần biết DB đang ở version nào.

### Ports/adapters trong module `vehicle`

Lợi ích:

- Domain và use case không phụ thuộc Spring MVC/JPA quá sâu.
- Controller, persistence có thể thay đổi độc lập hơn.
- Test use case dễ hơn vì mock được port.

Đổi lại:

- Nhiều file hơn so với CRUD đơn giản.
- Cần kỷ luật đặt tên/thư mục thống nhất.

## 17. Các điểm cần lưu ý khi phát triển tiếp

- Module `vehicle` đã có API đọc danh sách, nhưng chưa có create/update/delete.
- Các module khác mới có schema, chưa có Java implementation.
- Query param enum sai hiện bị parse thành `null`, tức là bỏ filter. Nếu muốn API chặt hơn, nên trả `INVALID_PARAMETER`.
- `VehicleJpaRepository` có comment bị lỗi encoding ở dấu gạch ngang. Không ảnh hưởng runtime, nhưng nên sửa để code dễ đọc.
- `seed_vehicles.sql` không tự chạy qua Flyway vì nằm ngoài `db/migration`; muốn seed dev phải chạy thủ công hoặc cấu hình riêng.
- `docs/api` và `infra/k8s` đang là thư mục rỗng trong workspace; nếu muốn version control giữ lại thư mục rỗng, cần thêm `.gitkeep`.

## 18. Tóm tắt nhanh từng nhóm

| Nhóm | Hiện trạng | Ý nghĩa |
| --- | --- | --- |
| Build | Maven + Spring Boot 4.0.6 + Java 26 | Build/chạy backend Java. |
| Runtime config | `application.yaml`, `spy.properties` | Cấu hình DB, Flyway, JPA, SQL log. |
| Database | Flyway SQL từ `V001` đến `V110` | Tạo schema/table cho toàn domain thuê xe. |
| API chung | `ApiResponse`, `ListResponse`, `PageResponse` | Chuẩn hóa response frontend nhận. |
| Error chung | `DomainException`, `ErrorCode`, `RestExceptionHandler` | Chuẩn hóa lỗi REST. |
| Vehicle domain | `Vehicle` + enum | Mô hình xe thuần nghiệp vụ. |
| Vehicle application | query/usecase/service/port | Điều phối use case đọc xe. |
| Vehicle REST | controller/response/mapper | Expose API admin. |
| Vehicle persistence | JPA entity/repository/specification/adapter/mapper | Đọc dữ liệu từ PostgreSQL. |
| Infra local | Docker Compose Postgres + pgAdmin | Môi trường dev local. |
| Tests | Smoke test context loads | Mới kiểm tra app start, chưa có test nghiệp vụ. |
