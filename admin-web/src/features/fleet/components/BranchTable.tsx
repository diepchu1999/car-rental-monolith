import { Eye, Pencil, Power, PowerOff } from "lucide-react";
import type { Branch } from "../types";
import { BranchStatusBadge } from "./BranchStatusBadge";

type BranchTableProps = {
  branches: Branch[];
  fetching?: boolean;
  togglingId?: string | null;
  onViewDetail: (id: string) => void;
  onEdit: (branch: Branch) => void;
  onToggleStatus: (branch: Branch) => void;
};

export function BranchTable({
  branches,
  fetching,
  togglingId,
  onViewDetail,
  onEdit,
  onToggleStatus,
}: BranchTableProps) {
  return (
    <div className="card animate-fade-up branch-table-card">
      {fetching ? <div className="branch-table-loading">Đang cập nhật...</div> : null}
      <div className="table-wrapper">
        <table className="branch-table">
          <thead>
            <tr>
              <th>Mã</th>
              <th>Tên chi nhánh</th>
              <th>Thành phố</th>
              <th>Số điện thoại</th>
              <th>Trạng thái</th>
              <th className="branch-col-action" />
            </tr>
          </thead>
          <tbody>
            {branches.map((branch) => {
              const isActive = branch.status === "ACTIVE";
              const toggling = togglingId === branch.id;
              return (
                <tr key={branch.id} onDoubleClick={() => onViewDetail(branch.id)}>
                  <td>
                    <span className="branch-code">{branch.code}</span>
                  </td>
                  <td>
                    <div className="branch-name">{branch.name}</div>
                    <div className="branch-address">{branch.address || "—"}</div>
                  </td>
                  <td>{branch.city || "—"}</td>
                  <td>{branch.phone || "—"}</td>
                  <td>
                    <BranchStatusBadge status={branch.status} />
                  </td>
                  <td className="branch-col-action">
                    <div className="branch-row-actions">
                      <button
                        className="btn btn-secondary btn-sm"
                        type="button"
                        onClick={() => onViewDetail(branch.id)}
                      >
                        <Eye size={14} />
                        Chi tiết
                      </button>
                      <button
                        className="btn btn-secondary btn-sm"
                        type="button"
                        onClick={() => onEdit(branch)}
                      >
                        <Pencil size={14} />
                        Sửa
                      </button>
                      <button
                        className={`btn btn-sm ${isActive ? "btn-danger" : "btn-primary"}`}
                        type="button"
                        disabled={toggling}
                        title={isActive ? "Ngừng hoạt động" : "Kích hoạt"}
                        onClick={() => onToggleStatus(branch)}
                      >
                        {isActive ? <PowerOff size={14} /> : <Power size={14} />}
                        {isActive ? "Ngừng" : "Kích hoạt"}
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
