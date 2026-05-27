import { useState } from "react";
import { Download, RefreshCw, UserPlus } from "lucide-react";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import {
  getAdminCustomersPage,
  getCustomerStats,
} from "../api/customerAPI";
import { CustomerDetailModal } from "../components/CustomerDetailModal";
import { CustomerFilters } from "../components/CustomerFilters";
import { CustomerFormModal } from "../components/CustomerFormModal";
import {
  CustomerEmptyState,
  CustomerErrorState,
  CustomerLoadingState,
} from "../components/CustomerListStates";
import { CustomerPagination } from "../components/CustomerPagination";
import { CustomerStats } from "../components/CustomerStats";
import { CustomerTable } from "../components/CustomerTable";
import { showToast } from "../notify";
import type { CustomerFiltersState } from "../types";
import "./customers.css";

const DEFAULT_PAGE_SIZE = 20;

export function CustomerListPage() {
  const [filters, setFilters] = useState<CustomerFiltersState>({});
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(DEFAULT_PAGE_SIZE);
  const [detailCustomerId, setDetailCustomerId] = useState<string | null>(null);
  const [createOpen, setCreateOpen] = useState(false);

  const statsQuery = useQuery({
    queryKey: ["customers", "stats"],
    queryFn: getCustomerStats,
  });

  const customersQuery = useQuery({
    queryKey: ["customers", "paged", filters, page, size],
    queryFn: () => getAdminCustomersPage({ ...filters, page, size }),
    placeholderData: keepPreviousData,
  });

  function handleFiltersChange(next: CustomerFiltersState) {
    setFilters(next);
    setPage(1);
  }

  function handleReset() {
    setFilters({});
    setPage(1);
  }

  function handleSizeChange(nextSize: number) {
    setSize(nextSize);
    setPage(1);
  }

  const pageData = customersQuery.data;
  const customers = pageData?.items ?? [];
  const showEmptyState = customersQuery.isSuccess && customers.length === 0;
  const showLoadingState = customersQuery.isLoading && !pageData;
  const showPagination = pageData && pageData.total > 0;
  const errorMessage =
    customersQuery.error instanceof Error
      ? customersQuery.error.message
      : undefined;

  return (
    <>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Quản lý khách hàng</h2>
          <p>{statsQuery.data?.total ?? pageData?.total ?? 0} khách hàng đã đăng ký</p>
        </div>
        <div className="flex gap-8">
          <button
            className="btn btn-secondary btn-sm"
            type="button"
            disabled={customersQuery.isFetching}
            onClick={() => {
              customersQuery.refetch();
              statsQuery.refetch();
            }}
          >
            <RefreshCw size={14} />
            {customersQuery.isFetching ? "Đang tải" : "Làm mới"}
          </button>
          <button
            className="btn btn-secondary btn-sm"
            type="button"
            onClick={() => showToast("Đã xuất danh sách (demo)", "success")}
          >
            <Download size={14} />
            Xuất CSV
          </button>
          <button
            className="btn btn-primary btn-sm"
            type="button"
            onClick={() => setCreateOpen(true)}
          >
            <UserPlus size={16} />
            Thêm KH
          </button>
        </div>
      </div>

      <CustomerStats stats={statsQuery.data} loading={statsQuery.isLoading} />

      <CustomerFilters
        filters={filters}
        onChange={handleFiltersChange}
        onReset={handleReset}
      />

      {showLoadingState ? <CustomerLoadingState /> : null}
      {customersQuery.isError ? (
        <CustomerErrorState message={errorMessage} onRetry={customersQuery.refetch} />
      ) : null}
      {showEmptyState ? <CustomerEmptyState /> : null}

      {customers.length > 0 ? (
        <CustomerTable
          customers={customers}
          fetching={customersQuery.isFetching}
          onViewDetail={setDetailCustomerId}
        />
      ) : null}

      {showPagination ? (
        <CustomerPagination
          page={pageData.page}
          size={pageData.size}
          total={pageData.total}
          totalPages={pageData.totalPages}
          hasNext={pageData.hasNext}
          hasPrevious={pageData.hasPrevious}
          disabled={customersQuery.isFetching}
          onPageChange={setPage}
          onSizeChange={handleSizeChange}
        />
      ) : null}

      <CustomerDetailModal
        customerId={detailCustomerId}
        onClose={() => setDetailCustomerId(null)}
      />

      <CustomerFormModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={(id) => setDetailCustomerId(id)}
      />
    </>
  );
}
