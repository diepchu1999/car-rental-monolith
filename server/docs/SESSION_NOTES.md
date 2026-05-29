# Session Notes — trạng thái việc đang dở

> File này để **mang context sang máy khác**. Cập nhật trước khi dừng việc, commit + push.
> Máy mới: `git pull` → mở file này → đọc mục "Đang làm dở" để biết tiếp ở đâu.
> Quy ước: ghi ngày tuyệt đối (YYYY-MM-DD), không dùng "hôm qua/tuần trước".

---

## Cập nhật mới nhất: 2026-05-29

Tất cả thay đổi dưới đây **chưa commit** (working tree). Gồm 5 mảng.

### 1. SQL caller logging — ✅ XONG & ĐÃ VERIFY
- `RepositoryLoggingAspect.java`: pointcut `execution(* ...modules..adapter.out.persistence..*(..))`
  tag SQL theo `Class#method` (vd `CustomerSearchAdapter#search`). Có `@EnableAspectJAutoProxy`
  (Spring Boot 4 BOM không quản version `spring-boot-starter-aop`).
- `RepositoryQueryContext.java`: **đổi sang lưu qua SLF4J `MDC`** thay vì ThreadLocal static.
  Lý do: spring-boot-devtools chạy 2 classloader → aspect (restart loader) set 1 bản, p6spy
  formatter (base loader) đọc bản khác → luôn null. MDC nằm ở slf4j-api (base loader) nên
  dùng chung. **Đã verify log ra đúng caller tag.**

### 2. Dev logging click-được (`%F:%L`) — prod không dính
- `application-dev.yaml` (mới): `logging.pattern.console` có `(%F:%L)` → IntelliJ click vào
  log nhảy tới class+dòng; + `logging.level.com.ares...=DEBUG`.
- `pom.xml`: spring-boot-maven-plugin bật profile `dev` khi `spring-boot:run`. Prod (`java -jar`)
  không qua goal này nên không dính pattern nặng.
- ⚠️ Nếu chạy bằng **IntelliJ run config** (không qua maven): phải tự set Active profiles=`dev`
  hoặc env `SPRING_PROFILES_ACTIVE=dev`.

### 3. Dark theme native control — `admin-web/src/assets/styles.css`
- `color-scheme: dark` cho `input, select, textarea` + theme `select option` → mọi dropdown/date
  picker toàn admin render nền tối.

### 4. Vehicle host/renter — ✅ XONG & VERIFY (BE compile + FE build)
Bug: chủ xe HOST_OWNED không bắt buộc là host; picker chọn được cả renter.
- **BE chốt chặn**: thêm `isActiveHost(UUID)` vào `CustomerDirectory` + `LoadCustomerPort` +
  `CustomerLoadAdapter` (SQL customers ACTIVE JOIN host_profiles ACTIVE) + `CustomerDirectoryService`.
  `VehicleCommandService` nhánh HOST_OWNED đổi `isActiveCustomer` → `isActiveHost`.
- **FE lọc host**: `SearchCustomersQuery` + `CustomerSearchAdapter` thêm `hostOnly`;
  `AdminCustomerController` thêm `@RequestParam hostOnly`; `customerAPI.ts` + `CustomerPicker.tsx`
  (prop `hostOnly`) + `VehicleFormModal` truyền hostOnly.
- **Popup chọn chủ xe**: `HostLookupModal.tsx` (list host + phân trang) + `HostDetailModal.tsx`
  (**read-only**, CSS tự chứa — KHÔNG tái dùng CustomerDetailModal vì CSS nằm chunk customers.css
  không load ở trang Vehicle). Nút ≡ lookup trong VehicleFormModal.
- ⚠️ Dữ liệu cũ: có thể có xe HOST_OWNED owner không phải host (query rà trong lịch sử chat).

### 5. Fleet — Quản lý chi nhánh
**FE (admin-web) — ✅ build pass:**
- Sub-nav Fleet nằm ở **sidebar trái** (lồng dưới "Fleet"): `adminNavigation.ts` (children),
  `Sidebar.tsx` (nút Fleet = toggle CHỈ sổ/thu, không điều hướng, không chọn mặc định), CSS
  `.nav-subnav`/`.nav-subitem`/`.nav-item-toggle` trong `styles.css`.
- Route `/fleet/branches` + `/fleet/vehicles` (adminRoutes.tsx), `/fleet` redirect (App.tsx).
- `features/fleet/`: types.ts, notify.ts, api/fleetAPI.ts (thêm getBranchesPage/getBranchDetail/
  createBranch/updateBranch/changeBranchStatus), pages/{FleetLayoutPage,FleetBranchesPage,
  FleetVehiclesPage(placeholder),fleet.css}, components/{BranchFilters,BranchTable,BranchStatusBadge,
  BranchPagination,BranchFormModal,BranchDetailModal}.

**BE (server) — mới có LIST paged:**
- `FleetBranchPagedAdapter.java` (đã rename từ FleetBranchPersitenceAdapter) implement
  `LoadFleetBranchPort.loadFleetBranchesList` → query `fleet.branches`.
- `AdminFleetController` sửa path `/fleet/branches/paged` → `/branches/paged` (khớp FE).
- `FleetBranchDetail.provinceCode/communeCode` map **null** (bảng fleet.branches V040 chưa có cột).

### Việc tiếp theo (CHƯA làm)
- [ ] **BE Fleet còn thiếu** (FE đã gọi nhưng BE chưa có): `GET /branches/{id}`, `POST /branches`,
      `PATCH /branches/{id}`, `PATCH /branches/{id}/status`. Cần thêm port + write adapter +
      service + controller. → Hiện các nút detail/create/update/status ở FE sẽ 404.
- [ ] Restart server verify màn Chi nhánh list ra data thật.
- [ ] Commit toàn bộ (gom theo nhóm: sql-logging / dev-logging / dark-css / vehicle-host-fix /
      fleet-branches-fe / fleet-branches-be).

### Lưu ý dọn dẹp
- 2 file artifact transcript ở working tree (KHÔNG commit): `2026-05-29-100705-...txt` (root)
  và `server/2026-05-29-100705-...txt`. Nên xoá hoặc gitignore.
- `.claude/settings.json` (mới) có Stop hook nhắc cập nhật file này — cần mở `/hooks` hoặc
  restart Claude Code 1 lần để hook được nạp.
