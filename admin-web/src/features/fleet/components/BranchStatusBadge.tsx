import type { BranchStatus } from "../types";

const meta: Record<BranchStatus, { label: string; className: string }> = {
  ACTIVE: { label: "Đang hoạt động", className: "branch-badge-active" },
  INACTIVE: { label: "Ngừng hoạt động", className: "branch-badge-inactive" },
};

export function BranchStatusBadge({ status }: { status: BranchStatus }) {
  const item = meta[status] ?? meta.INACTIVE;
  return <span className={`branch-status-badge ${item.className}`}>{item.label}</span>;
}
