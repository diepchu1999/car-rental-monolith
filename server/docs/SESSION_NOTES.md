# Session Notes — trạng thái việc đang dở

> File này để **mang context sang máy khác**. Cập nhật trước khi dừng việc, commit + push.
> Máy mới: `git pull` → mở file này → đọc mục "Đang làm dở" để biết tiếp ở đâu.
> Quy ước: ghi ngày tuyệt đối (YYYY-MM-DD), không dùng "hôm qua/tuần trước".

---

## Cập nhật mới nhất: 2026-06-01

### ĐÃ COMMIT (branch CAR-04, tới commit "Update pattern")

1. **Mã định danh từ DB sequence** — bỏ `UUID.randomUUID().substring(0,8)` (rủi ro
   trùng theo birthday paradox + UNIQUE constraint).
   - `host_code`: `V124__host_code_sequence.sql` + `WriteCustomerPort.nextHostCode()`
     → `HOST-%06d`.
   - `kyc_code`: `V125__kyc_code_sequence.sql` + `nextKycCode()` → `KYC-%06d`
     (sửa cả `CustomerCommandService`, `KycCommandService`, fallback `insertKyc`).
   - ⚠️ Cần restart để Flyway apply V124/V125 trước khi tạo host/kyc mới.

2. **SqlLoader — quản lý Native SQL bằng file `.sql`** (`shared/sql/SqlLoader` +
   `ClasspathSqlQueryLoader`, cache bean-instance). SQL ở `resources/sql/<module>/*.sql`,
   mỗi module có class `<Module>SqlPaths` (package-private). Đã rollout **toàn bộ**
   12 adapter / 5 module (customer, fleet, vehicle, location, driver), 58 file `.sql`.
   SQL động (CustomerPageAdapter / VehicleEnrichedListQuery) externalize khối tĩnh,
   giữ phần ráp WHERE/ORDER BY ở Java.

3. **Chuẩn hóa pattern dùng chung** (`shared/`):
   - `persistence/Tuples` — gộp helper map Tuple (uuid/dateTime/localDate/intValue/
     longValue/bigDecimal), bản superset; thay ~9 adapter.
   - `api/PageResponse.ofPageIndex(items,total,pageIndex,size)` — gom phép tính phân
     trang; thay 6 adapter. Convention rỗng → totalPages=1.
   - `api/PageParams.normalize(page,size,defaultSize,maxSize)` — chuẩn hóa input;
     thay 6 query record (giữ maxSize riêng từng endpoint: 50 vs 100).
   - Tài liệu: thêm mục 8 "Tiện ích dùng chung" vào `MODULE_ARCHITECTURE.md` + cập nhật
     template.

### ĐANG LÀM DỞ — CHƯA COMMIT (Fleet > Chi nhánh: detail endpoint)

Mục tiêu: bật xem chi tiết chi nhánh ở FE (đang gọi `GET /admin/fleet/branches/{id}`).

- **Bug đã fix**: `AdminFleetBranchDetailResponse` trước serialize field `uuid` (sai)
  → FE đọc `branch.id` = undefined → bấm "Chi tiết" gọi `/branches/undefined`.
  Đã đổi `uuid` → `id` (khớp convention toàn hệ thống).
- **Thêm `createdAt`/`updatedAt`** vào response (FE modal cần hiển thị "Tạo lúc/Cập nhật"):
  - `FleetBranchDetail` (domain) thêm 2 field `OffsetDateTime`.
  - `load_fleet_branch.sql` + `fleet_branches_data.sql` SELECT thêm `created_at`,
    `updated_at` (và list bổ sung `province_code`/`commune_code` — V121 đã có cột).
  - `FleetPersistenceAdapter.getBranch` + `FleetBranchPagedAdapter.toBranch` map qua
    `Tuples.dateTime(...)`.
- Endpoint detail: `GetFleetBranchUseCase` + `LoadFleetPort.getBranch` +
  `FleetQueryService` + `AdminFleetController.getByFleetId`.
- ✅ `mvn compile` pass.

### Việc tiếp theo
- [ ] **Restart server** → verify: (1) màn Chi nhánh mở được detail (id ok),
      (2) "Tạo lúc/Cập nhật" hiển thị đúng, (3) host/kyc tạo mới ra mã sequence.
- [ ] **Commit** nhóm Fleet branch-detail đang dở (11 file fleet).
- [ ] (Tùy chọn) chạy test online (bỏ `-o`) xác nhận không hồi quy — integration test cần Postgres.
- [ ] Dữ liệu cũ: rà xe HOST_OWNED có owner không phải host (đã bàn trước đó).

### Lưu ý
- FE **không** cần đổi: `Branch` type đã có sẵn `id`/`createdAt`/`updatedAt`.
- `.claude/settings.json` Stop hook nhắc cập nhật file này (cần `/hooks` reload 1 lần nếu chưa).
