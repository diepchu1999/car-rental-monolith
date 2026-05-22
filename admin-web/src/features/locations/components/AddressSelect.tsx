import { useQuery } from "@tanstack/react-query";
import { listCommunes, listProvinces } from "../api/locationAPI";

type AddressSelectValue = {
  provinceCode: string;
  communeCode: string;
};

type AddressSelectProps = AddressSelectValue & {
  onChange: (next: AddressSelectValue) => void;
  // Tên đơn vị cấp tỉnh: nhãn hiển thị thêm
  required?: boolean;
  // Quận/Huyện cũ (legacy) — chỉ hiển thị read-only để bảo toàn lịch sử.
  legacyDistrict?: string | null;
  includeAllOption?: boolean;
};

// Form chọn địa chỉ 2 cấp theo mô hình hành chính 2025: Tỉnh/Thành phố ->
// Xã/Phường/Đặc khu. Đổi tỉnh sẽ reset xã đã chọn.
export function AddressSelect({
  provinceCode,
  communeCode,
  onChange,
  required,
  legacyDistrict,
  includeAllOption,
}: AddressSelectProps) {
  const provincesQuery = useQuery({
    queryKey: ["locations", "provinces"],
    queryFn: listProvinces,
  });
  const communesQuery = useQuery({
    queryKey: ["locations", "communes", provinceCode],
    queryFn: () => listCommunes(provinceCode),
    enabled: Boolean(provinceCode),
  });

  const provinces = provincesQuery.data?.items ?? [];
  const communes = communesQuery.data?.items ?? [];

  const provincePlaceholder = includeAllOption ? "— Tất cả tỉnh/thành —" : "— Chọn tỉnh/thành —";
  const communePlaceholder = includeAllOption ? "— Tất cả xã/phường —" : "— Chọn xã/phường —";

  return (
    <>
      <div className="vehicle-form-grid">
        <label className="vehicle-form-field">
          <span>Tỉnh / Thành phố{required ? " *" : ""}</span>
          <select
            className="form-select"
            value={provinceCode}
            required={required}
            onChange={(event) =>
              onChange({ provinceCode: event.target.value, communeCode: "" })
            }
          >
            <option value="">
              {provincesQuery.isLoading ? "Đang tải..." : provincePlaceholder}
            </option>
            {provinces.map((p) => (
              <option key={p.code} value={p.code}>
                {p.fullName ?? p.name}
              </option>
            ))}
          </select>
        </label>
        <label className="vehicle-form-field">
          <span>Xã / Phường / Đặc khu{required ? " *" : ""}</span>
          <select
            className="form-select"
            value={communeCode}
            required={required}
            disabled={!provinceCode}
            onChange={(event) =>
              onChange({ provinceCode, communeCode: event.target.value })
            }
          >
            <option value="">
              {!provinceCode
                ? "— Chọn tỉnh/thành trước —"
                : communesQuery.isLoading
                  ? "Đang tải..."
                  : communePlaceholder}
            </option>
            {communes.map((c) => (
              <option key={c.code} value={c.code}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
      </div>
      {legacyDistrict ? (
        <div className="vehicle-form-legacy">
          Quận / Huyện (dữ liệu cũ, chỉ đọc): {legacyDistrict}
        </div>
      ) : null}
    </>
  );
}
