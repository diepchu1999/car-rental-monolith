import { RotateCcw, Search } from "lucide-react";
import type { BranchFiltersState, BranchStatusFilter } from "../types";

type BranchFiltersProps = {
  filters: BranchFiltersState;
  onChange: (filters: BranchFiltersState) => void;
  onReset: () => void;
};

const statusOptions: { value: BranchStatusFilter; label: string }[] = [
  { value: "all", label: "Tất cả trạng thái" },
  { value: "active", label: "Đang hoạt động" },
  { value: "inactive", label: "Ngừng hoạt động" },
];

export function BranchFilters({ filters, onChange, onReset }: BranchFiltersProps) {
  function patch(partial: Partial<BranchFiltersState>) {
    onChange({ ...filters, ...partial });
  }

  return (
    <div className="card mb-24 animate-fade-up">
      <div className="branch-filter-bar">
        <div className="search-box branch-search-box">
          <Search size={14} />
          <input
            type="text"
            placeholder="Tìm theo mã, tên, thành phố, SĐT..."
            value={filters.q ?? ""}
            onChange={(event) => patch({ q: event.target.value })}
          />
        </div>

        <select
          className="form-select branch-filter-select"
          value={filters.status ?? "all"}
          onChange={(event) =>
            patch({ status: event.target.value as BranchStatusFilter })
          }
          aria-label="Lọc theo trạng thái"
        >
          {statusOptions.map((option) => (
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
