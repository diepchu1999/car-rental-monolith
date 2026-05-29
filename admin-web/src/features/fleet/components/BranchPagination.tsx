import {
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
} from "lucide-react";

type BranchPaginationProps = {
  page: number;
  size: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
  disabled?: boolean;
  onPageChange: (page: number) => void;
  onSizeChange: (size: number) => void;
};

const pageSizeOptions = [10, 20, 50, 100];

export function BranchPagination({
  page,
  size,
  total,
  totalPages,
  hasNext,
  hasPrevious,
  disabled = false,
  onPageChange,
  onSizeChange,
}: BranchPaginationProps) {
  const safeTotalPages = Math.max(totalPages, 1);
  const start = total === 0 ? 0 : (page - 1) * size + 1;
  const end = Math.min(page * size, total);
  const pageNumbers = getVisiblePages(page, safeTotalPages);

  return (
    <div className="branch-pagination card animate-fade-up">
      <div className="branch-pagination-summary">
        <span>
          Hiển thị <strong>{start}</strong>-<strong>{end}</strong> trong{" "}
          <strong>{total}</strong> chi nhánh
        </span>
        <span className="branch-pagination-page">
          Trang {page}/{safeTotalPages}
        </span>
      </div>

      <div className="branch-pagination-controls">
        <label className="branch-page-size">
          <span>Số dòng</span>
          <select
            className="form-select branch-page-size-select"
            value={size}
            disabled={disabled}
            onChange={(event) => onSizeChange(Number(event.target.value))}
          >
            {pageSizeOptions.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>

        <div className="pagination-btns">
          <button
            className="page-btn"
            type="button"
            title="Trang đầu"
            disabled={disabled || !hasPrevious}
            onClick={() => onPageChange(1)}
          >
            <ChevronsLeft size={15} />
          </button>
          <button
            className="page-btn"
            type="button"
            title="Trang trước"
            disabled={disabled || !hasPrevious}
            onClick={() => onPageChange(page - 1)}
          >
            <ChevronLeft size={15} />
          </button>

          {pageNumbers.map((pageNumber, index) =>
            pageNumber === "ellipsis" ? (
              <span className="branch-page-ellipsis" key={`ellipsis-${index}`}>
                ...
              </span>
            ) : (
              <button
                className={`page-btn ${pageNumber === page ? "active" : ""}`}
                type="button"
                key={pageNumber}
                disabled={disabled}
                onClick={() => onPageChange(pageNumber)}
              >
                {pageNumber}
              </button>
            ),
          )}

          <button
            className="page-btn"
            type="button"
            title="Trang sau"
            disabled={disabled || !hasNext}
            onClick={() => onPageChange(page + 1)}
          >
            <ChevronRight size={15} />
          </button>
          <button
            className="page-btn"
            type="button"
            title="Trang cuối"
            disabled={disabled || !hasNext}
            onClick={() => onPageChange(safeTotalPages)}
          >
            <ChevronsRight size={15} />
          </button>
        </div>
      </div>
    </div>
  );
}

function getVisiblePages(currentPage: number, totalPages: number) {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1);
  }
  if (currentPage <= 4) {
    return [1, 2, 3, 4, 5, "ellipsis", totalPages] as const;
  }
  if (currentPage >= totalPages - 3) {
    return [
      1,
      "ellipsis",
      totalPages - 4,
      totalPages - 3,
      totalPages - 2,
      totalPages - 1,
      totalPages,
    ] as const;
  }
  return [
    1,
    "ellipsis",
    currentPage - 1,
    currentPage,
    currentPage + 1,
    "ellipsis",
    totalPages,
  ] as const;
}
