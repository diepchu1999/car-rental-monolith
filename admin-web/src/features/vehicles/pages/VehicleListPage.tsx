import { useState } from "react";
import { Plus, RefreshCw } from "lucide-react";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { getVehiclesPage } from "../api/vehicleAPI";
import { VehicleCard } from "../components/VehicleCard";
import { VehicleDetailModal } from "../components/VehicleDetailModal";
import { VehicleFilters } from "../components/VehicleFilters";
import { VehiclePagination } from "../components/VehiclePagination";
import {
  VehicleEmptyState,
  VehicleErrorState,
  VehicleLoadingState,
} from "../components/VehicleListStates";
import {
  VehicleViewToggle,
  type VehicleViewMode,
} from "../components/VehicleViewToggle";
import type { VehicleFiltersState } from "../types";
import "./vehicles.css";

const VEHICLE_VIEW_MODE_STORAGE_KEY = "vehicle-view-mode";
const DEFAULT_PAGE_SIZE = 20;

function loadViewMode(): VehicleViewMode {
  return localStorage.getItem(VEHICLE_VIEW_MODE_STORAGE_KEY) === "list"
    ? "list"
    : "grid";
}

export function VehicleListPage() {
  const [filters, setFilters] = useState<VehicleFiltersState>({});
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(DEFAULT_PAGE_SIZE);
  const [viewMode, setViewMode] = useState<VehicleViewMode>(loadViewMode);
  const [detailVehicleId, setDetailVehicleId] = useState<string | null>(null);

  const vehiclesQuery = useQuery({
    queryKey: ["vehicles", "paged", filters, page, size],
    queryFn: () => getVehiclesPage({ ...filters, page, size }),
    placeholderData: keepPreviousData,
  });

  function handleFiltersChange(nextFilters: VehicleFiltersState) {
    setFilters(nextFilters);
    setPage(1);
  }

  function handleViewModeChange(nextViewMode: VehicleViewMode) {
    setViewMode(nextViewMode);
    localStorage.setItem(VEHICLE_VIEW_MODE_STORAGE_KEY, nextViewMode);
  }

  function handleSizeChange(nextSize: number) {
    setSize(nextSize);
    setPage(1);
  }

  function handleReset() {
    setFilters({});
    setPage(1);
  }

  const pageData = vehiclesQuery.data;
  const vehicles = pageData?.items ?? [];
  const showEmptyState = vehiclesQuery.isSuccess && vehicles.length === 0;
  const showLoadingState = vehiclesQuery.isLoading && !pageData;
  const showPagination = pageData && pageData.total > 0;

  return (
    <>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Quản lý Xe</h2>
          <p>{pageData?.total ?? 0} xe trong hệ thống</p>
        </div>
        <div className="flex gap-8">
          <VehicleViewToggle viewMode={viewMode} onChange={handleViewModeChange} />
          <button
            className="btn btn-secondary btn-sm"
            type="button"
            disabled={vehiclesQuery.isFetching}
            onClick={() => vehiclesQuery.refetch()}
          >
            <RefreshCw size={14} />
            {vehiclesQuery.isFetching ? "Đang tải" : "Làm mới"}
          </button>
          <button className="btn btn-primary btn-sm" type="button">
            <Plus size={16} />
            Thêm xe
          </button>
        </div>
      </div>

      <VehicleFilters
        filters={filters}
        onChange={handleFiltersChange}
        onReset={handleReset}
      />

      {showLoadingState ? <VehicleLoadingState /> : null}
      {vehiclesQuery.isError ? (
        <VehicleErrorState onRetry={vehiclesQuery.refetch} />
      ) : null}
      {showEmptyState ? <VehicleEmptyState /> : null}

      {vehicles.length > 0 ? (
        <div className="vehicle-list-shell">
          {vehiclesQuery.isFetching ? (
            <div className="vehicle-grid-loading">Đang cập nhật...</div>
          ) : null}
          <div className={`vehicles-grid vehicles-${viewMode}-view`}>
            {vehicles.map((vehicle) => (
              <VehicleCard
                key={vehicle.id ?? vehicle.licensePlate}
                vehicle={vehicle}
                viewMode={viewMode}
                onViewDetail={setDetailVehicleId}
              />
            ))}
          </div>
        </div>
      ) : null}

      {showPagination ? (
        <VehiclePagination
          page={pageData.page}
          size={pageData.size}
          total={pageData.total}
          totalPages={pageData.totalPages}
          hasNext={pageData.hasNext}
          hasPrevious={pageData.hasPrevious}
          disabled={vehiclesQuery.isFetching}
          onPageChange={setPage}
          onSizeChange={handleSizeChange}
        />
      ) : null}

      <VehicleDetailModal
        vehicleId={detailVehicleId}
        onClose={() => setDetailVehicleId(null)}
      />
    </>
  );
}
