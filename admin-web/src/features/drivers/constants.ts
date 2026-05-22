import type { DriverStat, DriverStatus, DriverStatusMeta } from "./types";

export const driverStatusMeta: Record<DriverStatus, DriverStatusMeta> = {
  available: { label: "Sẵn sàng", className: "badge-active" },
  driving: { label: "Đang chạy", className: "badge-completed" },
  off: { label: "Nghỉ phép", className: "badge-pending" },
};

export const driverStats: DriverStat[] = [
  { label: "Tổng tài xế", value: "48", className: "" },
  { label: "Đang sẵn sàng", value: "32", className: "text-success" },
  { label: "Đang chạy", value: "12", className: "text-info" },
  { label: "Nghỉ phép", value: "4", className: "text-warning" },
];
