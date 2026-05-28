import type {
  BadgeMeta,
  CustomerKycFilter,
  CustomerRole,
  CustomerRoleFilter,
  CustomerStatus,
  CustomerStatusFilter,
  DocumentSide,
  DocumentType,
  Gender,
  HostStatus,
  KycAggregateStatus,
  KycStatus,
  SelectOption,
} from "./types";

export const roleLabel: Record<CustomerRole, string> = {
  RENTER: "Người thuê",
  HOST: "Chủ xe",
};

export const roleTagClassName: Record<CustomerRole, string> = {
  RENTER: "tag-p2p",
  HOST: "tag-self",
};

export const customerStatusMeta: Record<CustomerStatus, BadgeMeta> = {
  ACTIVE: { label: "Hoạt động", className: "badge-active" },
  PENDING_KYC: { label: "Chờ KYC", className: "badge-pending" },
  BLOCKED: { label: "Bị khóa", className: "badge-cancelled" },
  DELETED: { label: "Đã xóa", className: "badge-draft" },
};

// Badge cho 1 hồ sơ KYC cụ thể (popup, list trong customer detail).
export const kycStatusMeta: Record<KycStatus, BadgeMeta> = {
  PENDING: { label: "Chờ duyệt", className: "badge-pending" },
  APPROVED: { label: "Đã duyệt", className: "badge-active" },
  REJECTED: { label: "Từ chối", className: "badge-cancelled" },
  EXPIRED: { label: "Hết hạn", className: "badge-draft" },
};

// Badge cho trạng thái KYC tổng (list page, header customer detail).
export const kycAggregateStatusMeta: Record<KycAggregateStatus, BadgeMeta> = {
  NO_KYC: { label: "Chưa có KYC", className: "badge-draft" },
  PENDING: { label: "Chờ duyệt", className: "badge-pending" },
  PARTIALLY_APPROVED: { label: "Duyệt một phần", className: "badge-pending" },
  FULLY_APPROVED: { label: "Đã duyệt đầy đủ", className: "badge-active" },
  REJECTED: { label: "Từ chối", className: "badge-cancelled" },
};

export const hostStatusMeta: Record<HostStatus, BadgeMeta> = {
  ACTIVE: { label: "Đang hoạt động", className: "badge-active" },
  SUSPENDED: { label: "Tạm ngưng", className: "badge-cancelled" },
  PENDING_KYC: { label: "Chờ duyệt", className: "badge-pending" },
};

export const genderLabel: Record<Gender, string> = {
  MALE: "Nam",
  FEMALE: "Nữ",
  OTHER: "Khác",
};

export const documentTypeLabel: Record<DocumentType, string> = {
  NATIONAL_ID: "CCCD/CMND",
  PASSPORT: "Hộ chiếu",
  DRIVING_LICENSE: "GPLX",
};

export const documentSideLabel: Record<DocumentSide, string> = {
  FRONT: "Mặt trước",
  BACK: "Mặt sau",
  SELFIE: "Selfie",
  OTHER: "Khác",
};

export const roleFilterOptions: SelectOption<CustomerRoleFilter>[] = [
  { value: "all", label: "Tất cả vai trò" },
  { value: "renter", label: "Người thuê" },
  { value: "host", label: "Chủ xe" },
  { value: "both", label: "Cả hai vai trò" },
];

export const statusFilterOptions: SelectOption<CustomerStatusFilter>[] = [
  { value: "all", label: "Tất cả trạng thái" },
  { value: "ACTIVE", label: "Hoạt động" },
  { value: "PENDING_KYC", label: "Chờ KYC" },
  { value: "BLOCKED", label: "Bị khóa" },
  { value: "DELETED", label: "Đã xóa" },
];

// Filter theo aggregate KYC status — khớp enum BE.
export const kycFilterOptions: SelectOption<CustomerKycFilter>[] = [
  { value: "all", label: "Tất cả KYC" },
  { value: "NO_KYC", label: "Chưa có KYC" },
  { value: "PENDING", label: "Chờ duyệt" },
  { value: "PARTIALLY_APPROVED", label: "Duyệt một phần" },
  { value: "FULLY_APPROVED", label: "Đã duyệt đầy đủ" },
  { value: "REJECTED", label: "Từ chối" },
];
