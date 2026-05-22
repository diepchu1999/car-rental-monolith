import type { VehicleSource, VehicleStatus } from "./types";

export const vehicleSourceLabel: Record<VehicleSource, string> = {
  COMPANY_OWNED: "AresDrive",
  HOST_OWNED: "P2P",
};

export const vehicleSourceClassName: Record<VehicleSource, string> = {
  COMPANY_OWNED: "tag-self",
  HOST_OWNED: "tag-p2p",
};

export const vehicleStatusLabel: Record<VehicleStatus, string> = {
  ACTIVE: "Hoạt động",
  DRAFT: "Bản nháp",
  PENDING_REVIEW: "Chờ duyệt",
  INACTIVE: "Tạm dừng",
  SUSPENDED: "Bị khóa",
};

export const vehicleStatusClassName: Record<VehicleStatus, string> = {
  ACTIVE: "badge-active",
  DRAFT: "badge-draft",
  PENDING_REVIEW: "badge-pending",
  INACTIVE: "badge-completed",
  SUSPENDED: "badge-cancelled",
};

export const vehicleTransmissionLabel: Record<string, string> = {
  AUTOMATIC: "Tự động",
  MANUAL: "Số sàn",
};

export const vehicleFuelTypeLabel: Record<string, string> = {
  GASOLINE: "Xăng",
  DIESEL: "Dầu",
  ELECTRIC: "Điện",
  HYBRID: "Hybrid",
};

export const vehicleSourceOptions: Array<{
  value: VehicleSource | "";
  label: string;
}> = [
  { value: "", label: "Tất cả nguồn" },
  { value: "COMPANY_OWNED", label: vehicleSourceLabel.COMPANY_OWNED },
  { value: "HOST_OWNED", label: vehicleSourceLabel.HOST_OWNED },
];

export const vehicleStatusOptions: Array<{
  value: VehicleStatus | "";
  label: string;
}> = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "ACTIVE", label: vehicleStatusLabel.ACTIVE },
  { value: "DRAFT", label: vehicleStatusLabel.DRAFT },
  { value: "PENDING_REVIEW", label: vehicleStatusLabel.PENDING_REVIEW },
  { value: "INACTIVE", label: vehicleStatusLabel.INACTIVE },
  { value: "SUSPENDED", label: vehicleStatusLabel.SUSPENDED },
];
