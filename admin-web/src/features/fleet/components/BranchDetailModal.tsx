import { useEffect, type ReactNode } from "react";
import { Pencil, Power, PowerOff, X } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { getBranchDetail } from "../api/fleetAPI";
import type { Branch } from "../types";
import { BranchStatusBadge } from "./BranchStatusBadge";

type BranchDetailModalProps = {
  branchId: string | null;
  toggling?: boolean;
  onClose: () => void;
  onEdit: (branch: Branch) => void;
  onToggleStatus: (branch: Branch) => void;
};

function formatDateTime(value: string | null | undefined): string {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "—"
    : new Intl.DateTimeFormat("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }).format(date);
}

export function BranchDetailModal({
  branchId,
  toggling,
  onClose,
  onEdit,
  onToggleStatus,
}: BranchDetailModalProps) {
  const open = branchId !== null;

  const query = useQuery({
    queryKey: ["fleet", "branches", "detail", branchId],
    queryFn: () => getBranchDetail(branchId!),
    enabled: open,
  });

  useEffect(() => {
    if (!open) return;
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  const branch = query.data;
  const isActive = branch?.status === "ACTIVE";

  function handleBackdropClick(event: React.MouseEvent<HTMLDivElement>) {
    if (event.target === event.currentTarget) onClose();
  }

  return (
    <div className="modal-overlay active" onClick={handleBackdropClick}>
      <div className="modal branch-detail-modal">
        <div className="modal-header">
          <h3 className="modal-title">Chi tiết chi nhánh</h3>
          <button type="button" className="modal-close" onClick={onClose} title="Đóng">
            <X size={16} />
          </button>
        </div>

        {query.isLoading ? (
          <div className="modal-body branch-detail-state">Đang tải...</div>
        ) : query.isError || !branch ? (
          <div className="modal-body branch-detail-state">
            Không tải được chi tiết chi nhánh.
          </div>
        ) : (
          <>
            <div className="modal-body branch-detail-body">
              <div className="branch-detail-hero">
                <div>
                  <span className="branch-code">{branch.code}</span>
                  <div className="branch-detail-name">{branch.name}</div>
                </div>
                <BranchStatusBadge status={branch.status} />
              </div>

              <div className="branch-detail-grid">
                <Field label="Thành phố" value={branch.city} />
                <Field label="Số điện thoại" value={branch.phone} />
                <Field label="Địa chỉ" value={branch.address} full />
                <Field label="Tạo lúc" value={formatDateTime(branch.createdAt)} />
                <Field label="Cập nhật" value={formatDateTime(branch.updatedAt)} />
              </div>
            </div>

            <div className="modal-footer">
              <button
                className={`btn btn-sm ${isActive ? "btn-danger" : "btn-primary"}`}
                type="button"
                disabled={toggling}
                onClick={() => onToggleStatus(branch)}
              >
                {isActive ? <PowerOff size={14} /> : <Power size={14} />}
                {isActive ? "Ngừng hoạt động" : "Kích hoạt"}
              </button>
              <button
                className="btn btn-secondary btn-sm"
                type="button"
                onClick={() => onEdit(branch)}
              >
                <Pencil size={14} />
                Sửa
              </button>
              <button className="btn btn-secondary btn-sm" type="button" onClick={onClose}>
                Đóng
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

function Field({
  label,
  value,
  full,
}: {
  label: string;
  value: string | null | undefined;
  full?: boolean;
}): ReactNode {
  const isEmpty = value === null || value === undefined || value === "";
  return (
    <div className={`branch-detail-field ${full ? "branch-detail-field-full" : ""}`}>
      <label>{label}</label>
      <span className={isEmpty ? "branch-detail-muted" : ""}>
        {isEmpty ? "—" : value}
      </span>
    </div>
  );
}
