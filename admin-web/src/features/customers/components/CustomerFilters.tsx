import { RotateCcw, Search } from "lucide-react";
import {
  kycFilterOptions,
  roleFilterOptions,
  statusFilterOptions,
} from "../constants";
import type {
  CustomerFiltersState,
  CustomerKycFilter,
  CustomerRoleFilter,
  CustomerStatusFilter,
} from "../types";

type CustomerFiltersProps = {
  filters: CustomerFiltersState;
  onChange: (filters: CustomerFiltersState) => void;
  onReset: () => void;
};

export function CustomerFilters({
  filters,
  onChange,
  onReset,
}: CustomerFiltersProps) {
  function patch(partial: Partial<CustomerFiltersState>) {
    onChange({ ...filters, ...partial });
  }

  return (
    <div className="card mb-24 animate-fade-up">
      <div className="customer-filter-bar">
        <div className="search-box customer-search-box">
          <Search size={14} />
          <input
            type="text"
            placeholder="Tìm theo tên, SĐT, email, mã host..."
            value={filters.q ?? ""}
            onChange={(event) => patch({ q: event.target.value })}
          />
        </div>

        <select
          className="form-select customer-filter-select"
          value={filters.role ?? "all"}
          onChange={(event) =>
            patch({ role: event.target.value as CustomerRoleFilter })
          }
          aria-label="Lọc theo vai trò"
        >
          {roleFilterOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>

        <select
          className="form-select customer-filter-select"
          value={filters.status ?? "all"}
          onChange={(event) =>
            patch({ status: event.target.value as CustomerStatusFilter })
          }
          aria-label="Lọc theo trạng thái"
        >
          {statusFilterOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>

        <select
          className="form-select customer-filter-select"
          value={filters.kyc ?? "all"}
          onChange={(event) =>
            patch({ kyc: event.target.value as CustomerKycFilter })
          }
          aria-label="Lọc theo KYC"
        >
          {kycFilterOptions.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>

        <button className="btn btn-secondary btn-sm" type="button" onClick={onReset}>
          <RotateCcw size={14} />
          Reset
        </button>
      </div>
    </div>
  );
}
