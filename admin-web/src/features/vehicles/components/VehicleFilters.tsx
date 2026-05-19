import { RotateCcw, Search } from "lucide-react";
import type {
  VehicleFiltersState,
  VehicleSource,
  VehicleStatus,
} from "../types";
import { vehicleSourceOptions, vehicleStatusOptions } from "../constants";

type VehicleFiltersProps = {
  filters: VehicleFiltersState;
  onChange: (filters: VehicleFiltersState) => void;
  onReset: () => void;
};

export function VehicleFilters({
  filters,
  onChange,
  onReset,
}: VehicleFiltersProps) {
  return (
    <div className="card mb-24 animate-fade-up">
      <div className="vehicle-filter-bar">
        <div className="search-box vehicle-search-box">
          <Search size={14} />
          <input
            type="text"
            placeholder="Tìm xe, biển số..."
            value={filters.q ?? ""}
            onChange={(event) =>
              onChange({ ...filters, q: event.target.value })
            }
          />
        </div>

        <select
          className="form-select vehicle-filter-select"
          value={filters.source ?? ""}
          onChange={(event) =>
            onChange({
              ...filters,
              source: event.target.value as VehicleSource | "",
            })
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
            onChange({
              ...filters,
              status: event.target.value as VehicleStatus | "",
            })
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
          onClick={onReset}
        >
          <RotateCcw size={14} />
          Reset
        </button>
      </div>
    </div>
  );
}
