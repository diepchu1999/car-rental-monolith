import { useState } from "react";
import { Plus, RefreshCw } from "lucide-react";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { getVehiclesPage } from "../api/vehicleAPI";
import { VehicleCard } from "../components/VehicleCard";
import { VehicleDetailModal } from "../components/VehicleDetailModal";
import { VehicleFilters } from "../components/VehicleFilters";
import { VehicleFormModal } from "../components/VehicleFormModal";
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
import type {
  SortDirection,
  VehicleFiltersState,
  VehicleSort,
} from "../types";
import "./vehicles.css";

const VEHICLE_VIEW_MODE_STORAGE_KEY = "vehicle-view-mode";
const DEFAULT_PAGE_SIZE = 20;
const DEFAULT_SORT_BY: VehicleSort = "CREATED_AT";
const DEFAULT_SORT_DIR: SortDirection = "DESC";

function loadViewMode(): VehicleViewMode {
  return localStorage.getItem(VEHICLE_VIEW_MODE_STORAGE_KEY) === "list"
    ? "list"
    : "grid";
}

export function VehicleListPage() {
  const [filters, setFilters] = useState<VehicleFiltersState>({});
  const [sortBy, setSortBy] = useState<VehicleSort>(DEFAULT_SORT_BY);
  const [sortDir, setSortDir] = useState<SortDirection>(DEFAULT_SORT_DIR);
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(DEFAULT_PAGE_SIZE);
  const [viewMode, setViewMode] = useState<VehicleViewMode>(loadViewMode);
  const [detailVehicleId, setDetailVehicleId] = useState<string | null>(null);
  const [createOpen, setCreateOpen] = useState(false);

  const vehiclesQuery = useQuery({
    queryKey: ["vehicles", "paged", filters, sortBy, sortDir, page, size],
    queryFn: () =>
      getVehiclesPage({
        ...filters,
        page,
        size,
        sortBy,
        sortDir,
      }),
    placeholderData: keepPreviousData,
  });

  function handleFiltersChange(nextFilters: VehicleFiltersState) {
    setFilters(nextFilters);
    setPage(1);
  }

  function handleSortChange(nextSortBy: VehicleSort, nextSortDir: SortDirection) {
    setSortBy(nextSortBy);
    setSortDir(nextSortDir);
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
    setSortBy(DEFAULT_SORT_BY);
    setSortDir(DEFAULT_SORT_DIR);
    setPage(1);
  }

  const pageData = vehiclesQuery.data;
  const vehicles = pageData?.items ?? [];
  const showEmptyState = vehiclesQuery.isSuccess && vehicles.length === 0;
  const showLoadingState = vehiclesQuery.isLoading && !pageData;
  const showPagination = pageData && pageData.total > 0;
  const errorMessage =
    vehiclesQuery.error instanceof Error
      ? vehiclesQuery.error.message
      : undefined;

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
          <button
            className="btn btn-primary btn-sm"
            type="button"
            onClick={() => setCreateOpen(true)}
          >
            <Plus size={16} />
            Thêm xe
          </button>
        </div>
      </div>

      <VehicleFilters
        filters={filters}
        sortBy={sortBy}
        sortDir={sortDir}
        onChange={handleFiltersChange}
        onSortChange={handleSortChange}
        onReset={handleReset}
      />

      {showLoadingState ? <VehicleLoadingState /> : null}
      {vehiclesQuery.isError ? (
        <VehicleErrorState
          message={errorMessage}
          onRetry={vehiclesQuery.refetch}
        />
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
                key={vehicle.id}
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

      <VehicleFormModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={(id) => setDetailVehicleId(id)}
      />
    </>
  );
}


