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

export const listingStatusLabel: Record<string, string> = {
  DRAFT: "Bản nháp",
  PUBLISHED: "Đã đăng",
  PAUSED: "Tạm dừng",
  REJECTED: "Bị từ chối",
};

export const listingStatusOptions: Array<{ value: string; label: string }> = [
  { value: "", label: "Tất cả listing" },
  { value: "DRAFT", label: listingStatusLabel.DRAFT },
  { value: "PUBLISHED", label: listingStatusLabel.PUBLISHED },
  { value: "PAUSED", label: listingStatusLabel.PAUSED },
  { value: "REJECTED", label: listingStatusLabel.REJECTED },
];

export const fuelTypeOptions: Array<{ value: string; label: string }> = [
  { value: "", label: "Mọi nhiên liệu" },
  { value: "GASOLINE", label: "Xăng" },
  { value: "DIESEL", label: "Dầu" },
  { value: "ELECTRIC", label: "Điện" },
  { value: "HYBRID", label: "Hybrid" },
];

export const transmissionOptions: Array<{ value: string; label: string }> = [
  { value: "", label: "Mọi hộp số" },
  { value: "AUTOMATIC", label: "Tự động" },
  { value: "MANUAL", label: "Số sàn" },
];

export const vehicleSortOptions: Array<{ value: string; label: string }> = [
  { value: "CREATED_AT", label: "Ngày tạo" },
  { value: "UPDATED_AT", label: "Ngày cập nhật" },
  { value: "BASE_DAILY_RATE", label: "Giá ngày" },
  { value: "MANUFACTURE_YEAR", label: "Năm SX" },
  { value: "BOOKING_COUNT", label: "Số booking" },
];
