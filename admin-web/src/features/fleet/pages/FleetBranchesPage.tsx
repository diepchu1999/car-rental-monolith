import { useState } from "react";
import { Building2, Plus, RefreshCw } from "lucide-react";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { changeBranchStatus, getBranchesPage } from "../api/fleetAPI";
import { BranchDetailModal } from "../components/BranchDetailModal";
import { BranchFilters } from "../components/BranchFilters";
import { BranchFormModal } from "../components/BranchFormModal";
import { BranchPagination } from "../components/BranchPagination";
import { BranchTable } from "../components/BranchTable";
import { showToast } from "../notify";
import type { Branch, BranchFiltersState } from "../types";
import { FleetLayoutPage } from "./FleetLayoutPage";

const DEFAULT_PAGE_SIZE = 20;

export function FleetBranchesPage() {
  const queryClient = useQueryClient();
  const [filters, setFilters] = useState<BranchFiltersState>({});
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(DEFAULT_PAGE_SIZE);
  const [detailId, setDetailId] = useState<string | null>(null);
  // form: { open, branch } — branch=null là tạo mới, có giá trị là sửa.
  const [form, setForm] = useState<{ open: boolean; branch: Branch | null }>({
    open: false,
    branch: null,
  });

  const branchesQuery = useQuery({
    queryKey: ["fleet", "branches", "paged", filters, page, size],
    queryFn: () => getBranchesPage({ ...filters, page, size }),
    placeholderData: keepPreviousData,
  });

  const statusMutation = useMutation({
    mutationFn: (branch: Branch) =>
      changeBranchStatus(
        branch.id,
        branch.status === "ACTIVE" ? "INACTIVE" : "ACTIVE",
      ),
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ["fleet", "branches"] });
      showToast(
        updated.status === "ACTIVE"
          ? "Đã kích hoạt chi nhánh"
          : "Đã ngừng hoạt động chi nhánh",
        updated.status === "ACTIVE" ? "success" : "warning",
      );
    },
    onError: () => showToast("Không thể đổi trạng thái chi nhánh", "error"),
  });

  function handleFiltersChange(next: BranchFiltersState) {
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

  function openEdit(branch: Branch) {
    setDetailId(null);
    setForm({ open: true, branch });
  }

  const pageData = branchesQuery.data;
  const branches = pageData?.items ?? [];
  const showEmptyState = branchesQuery.isSuccess && branches.length === 0;
  const showLoadingState = branchesQuery.isLoading && !pageData;
  const showPagination = pageData && pageData.total > 0;
  const errorMessage =
    branchesQuery.error instanceof Error ? branchesQuery.error.message : undefined;
  const togglingId = statusMutation.isPending
    ? statusMutation.variables?.id ?? null
    : null;

  return (
    <FleetLayoutPage>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Chi nhánh</h2>
          <p>{pageData?.total ?? 0} chi nhánh trong hệ thống</p>
        </div>
        <div className="flex gap-8">
          <button
            className="btn btn-secondary btn-sm"
            type="button"
            disabled={branchesQuery.isFetching}
            onClick={() => branchesQuery.refetch()}
          >
            <RefreshCw size={14} />
            {branchesQuery.isFetching ? "Đang tải" : "Làm mới"}
          </button>
          <button
            className="btn btn-primary btn-sm"
            type="button"
            onClick={() => setForm({ open: true, branch: null })}
          >
            <Plus size={16} />
            Thêm chi nhánh
          </button>
        </div>
      </div>

      <BranchFilters
        filters={filters}
        onChange={handleFiltersChange}
        onReset={handleReset}
      />

      {showLoadingState ? (
        <div className="card">
          <div className="card-body branch-state">Đang tải danh sách chi nhánh...</div>
        </div>
      ) : null}

      {branchesQuery.isError ? (
        <div className="card">
          <div className="card-body branch-state">
            <div>
              <div className="branch-state-title">Không tải được danh sách chi nhánh</div>
              <div className="branch-state-description">
                {errorMessage ?? "Đã có lỗi xảy ra khi tải dữ liệu. Vui lòng thử lại."}
              </div>
            </div>
            <button
              className="btn btn-secondary btn-sm"
              type="button"
              onClick={() => branchesQuery.refetch()}
            >
              Thử lại
            </button>
          </div>
        </div>
      ) : null}

      {showEmptyState ? (
        <div className="card">
          <div className="card-body empty-state">
            <Building2 size={40} strokeWidth={1.2} />
            <h3>Không có chi nhánh phù hợp</h3>
            <p>Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc, hoặc tạo chi nhánh mới.</p>
          </div>
        </div>
      ) : null}

      {branches.length > 0 ? (
        <BranchTable
          branches={branches}
          fetching={branchesQuery.isFetching}
          togglingId={togglingId}
          onViewDetail={setDetailId}
          onEdit={openEdit}
          onToggleStatus={(branch) => statusMutation.mutate(branch)}
        />
      ) : null}

      {showPagination ? (
        <BranchPagination
          page={pageData.page}
          size={pageData.size}
          total={pageData.total}
          totalPages={pageData.totalPages}
          hasNext={pageData.hasNext}
          hasPrevious={pageData.hasPrevious}
          disabled={branchesQuery.isFetching}
          onPageChange={setPage}
          onSizeChange={handleSizeChange}
        />
      ) : null}

      <BranchDetailModal
        branchId={detailId}
        toggling={statusMutation.isPending}
        onClose={() => setDetailId(null)}
        onEdit={openEdit}
        onToggleStatus={(branch) => statusMutation.mutate(branch)}
      />

      <BranchFormModal
        open={form.open}
        branch={form.branch}
        onClose={() => setForm({ open: false, branch: null })}
      />
    </FleetLayoutPage>
  );
}
