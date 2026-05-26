import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { ChevronDown, ChevronUp, RotateCcw, Search } from "lucide-react";
import { listCommunes, listProvinces } from "../../locations/api/locationAPI";
import type {
  ListingStatus,
  SortDirection,
  VehicleFiltersState,
  VehicleSort,
  VehicleSource,
  VehicleStatus,
} from "../types";
import {
  fuelTypeOptions,
  listingStatusOptions,
  transmissionOptions,
  vehicleSortOptions,
  vehicleSourceOptions,
  vehicleStatusOptions,
} from "../constants";

type VehicleFiltersProps = {
  filters: VehicleFiltersState;
  sortBy: VehicleSort;
  sortDir: SortDirection;
  onChange: (filters: VehicleFiltersState) => void;
  onSortChange: (sortBy: VehicleSort, sortDir: SortDirection) => void;
  onReset: () => void;
};

export function VehicleFilters({
  filters,
  sortBy,
  sortDir,
  onChange,
  onSortChange,
  onReset,
}: VehicleFiltersProps) {
  const [advancedOpen, setAdvancedOpen] = useState(false);

  const provincesQuery = useQuery({
    queryKey: ["locations", "provinces"],
    queryFn: listProvinces,
    enabled: advancedOpen,
  });
  const communesQuery = useQuery({
    queryKey: ["locations", "communes", filters.provinceCode ?? ""],
    queryFn: () => listCommunes(filters.provinceCode ?? ""),
    enabled: advancedOpen && Boolean(filters.provinceCode),
  });
  const provinces = provincesQuery.data?.items ?? [];
  const communes = communesQuery.data?.items ?? [];

  function patch(partial: Partial<VehicleFiltersState>) {
    onChange({ ...filters, ...partial });
  }

  return (
    <div className="card mb-24 animate-fade-up">
      <div className="vehicle-filter-bar">
        <div className="search-box vehicle-search-box">
          <Search size={14} />
          <input
            type="text"
            placeholder="Tìm xe, biển số..."
            value={filters.q ?? ""}
            onChange={(event) => patch({ q: event.target.value })}
          />
        </div>

        <select
          className="form-select vehicle-filter-select"
          value={filters.source ?? ""}
          onChange={(event) =>
            patch({ source: event.target.value as VehicleSource | "" })
          }
        >
          {vehicleSourceOptions.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>

        <select
          className="form-select vehicle-filter-select"
          value={filters.status ?? ""}
          onChange={(event) =>
            patch({ status: event.target.value as VehicleStatus | "" })
          }
        >
          {vehicleStatusOptions.map((option) => (
            <option key={option.value || "all"} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>

        <button
          className="btn btn-secondary btn-sm"
          type="button"
          onClick={() => setAdvancedOpen((open) => !open)}
        >
          {advancedOpen ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
          Bộ lọc nâng cao
        </button>

        <select
          className="form-select vehicle-filter-select"
          value={sortBy}
          onChange={(event) =>
            onSortChange(event.target.value as VehicleSort, sortDir)
          }
          aria-label="Sắp xếp theo"
        >
          {vehicleSortOptions.map((option) => (
            <option key={option.value} value={option.value}>
              Sắp xếp: {option.label}
            </option>
          ))}
        </select>

        <button
          className="btn btn-secondary btn-sm btn-icon"
          type="button"
          title={sortDir === "desc" ? "Giảm dần" : "Tăng dần"}
          onClick={() =>
            onSortChange(sortBy, sortDir === "desc" ? "asc" : "desc")
          }
        >
          {sortDir === "desc" ? (
            <ChevronDown size={14} />
          ) : (
            <ChevronUp size={14} />
          )}
        </button>

        <button
          className="btn btn-secondary btn-sm"
          type="button"
          onClick={onReset}
        >
          <RotateCcw size={14} />
          Reset
        </button>
      </div>

      {advancedOpen ? (
        <div className="vehicle-advanced-filters">
          <select
            className="form-select"
            value={filters.listingStatus ?? ""}
            onChange={(event) =>
              patch({
                listingStatus: event.target.value as ListingStatus | "",
              })
            }
          >
            {listingStatusOptions.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>

          <select
            className="form-select"
            value={filters.provinceCode ?? ""}
            onChange={(event) =>
              patch({ provinceCode: event.target.value, communeCode: "" })
            }
          >
            <option value="">
              {provincesQuery.isLoading ? "Đang tải..." : "Tỉnh / Thành phố"}
            </option>
            {provinces.map((p) => (
              <option key={p.code} value={p.code}>
                {p.fullName ?? p.name}
              </option>
            ))}
          </select>

          <select
            className="form-select"
            value={filters.communeCode ?? ""}
            disabled={!filters.provinceCode}
            onChange={(event) => patch({ communeCode: event.target.value })}
          >
            <option value="">
              {!filters.provinceCode
                ? "Xã / Phường (chọn tỉnh trước)"
                : communesQuery.isLoading
                  ? "Đang tải..."
                  : "Xã / Phường / Đặc khu"}
            </option>
            {communes.map((c) => (
              <option key={c.code} value={c.code}>
                {c.name}
              </option>
            ))}
          </select>

          <select
            className="form-select"
            value={filters.fuelType ?? ""}
            onChange={(event) => patch({ fuelType: event.target.value })}
          >
            {fuelTypeOptions.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>

          <select
            className="form-select"
            value={filters.transmission ?? ""}
            onChange={(event) => patch({ transmission: event.target.value })}
          >
            {transmissionOptions.map((option) => (
              <option key={option.value || "all"} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>

          <input
            className="form-input"
            type="number"
            placeholder="Số chỗ"
            min={1}
            value={filters.seats ?? ""}
            onChange={(event) =>
              patch({
                seats: event.target.value === "" ? "" : Number(event.target.value),
              })
            }
          />

          <input
            className="form-input"
            type="number"
            placeholder="Giá tối thiểu (VND/ngày)"
            min={0}
            step={50_000}
            value={filters.minRate ?? ""}
            onChange={(event) =>
              patch({
                minRate: event.target.value === "" ? "" : Number(event.target.value),
              })
            }
          />

          <input
            className="form-input"
            type="number"
            placeholder="Giá tối đa (VND/ngày)"
            min={0}
            step={50_000}
            value={filters.maxRate ?? ""}
            onChange={(event) =>
              patch({
                maxRate: event.target.value === "" ? "" : Number(event.target.value),
              })
            }
          />

          <label className="vehicle-checkbox">
            <input
              type="checkbox"
              checked={filters.hasBookings === true}
              onChange={(event) =>
                patch({
                  hasBookings: event.target.checked ? true : undefined,
                })
              }
            />
            Chỉ xe đã có booking
          </label>
        </div>
      ) : null}
    </div>
  );
}
