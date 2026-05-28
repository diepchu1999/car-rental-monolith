export type CustomerRole = "RENTER" | "HOST";

export type CustomerStatus = "ACTIVE" | "PENDING_KYC" | "BLOCKED" | "DELETED";

// Trạng thái 1 hồ sơ KYC cụ thể.
export type KycStatus = "PENDING" | "APPROVED" | "REJECTED" | "EXPIRED";

// Trạng thái KYC tổng hợp cho 1 customer, tính từ toàn bộ hồ sơ KYC hiện có.
// FE hiển thị badge này ở list + detail; nút duyệt/từ chối chỉ nằm trong popup
// chi tiết từng KYC, không có "duyệt KYC tổng".
export type KycAggregateStatus =
  | "NO_KYC"
  | "PENDING"
  | "PARTIALLY_APPROVED"
  | "FULLY_APPROVED"
  | "REJECTED";

export type Gender = "MALE" | "FEMALE" | "OTHER";

export type DocumentType = "NATIONAL_ID" | "PASSPORT" | "DRIVING_LICENSE";

export type HostStatus = "ACTIVE" | "SUSPENDED" | "PENDING_KYC";

export type DocumentSide = "FRONT" | "BACK" | "SELFIE" | "OTHER";

export type CustomerAddress = {
  id: string;
  label: string;
  line1: string;
  provinceCode?: string | null;
  communeCode?: string | null;
  provinceName?: string | null;
  communeName?: string | null;
  legacyDistrict?: string | null;
  isDefault: boolean;
};

export type HostProfile = {
  hostCode: string;
  displayName: string;
  bio?: string | null;
  ratingAverage: number;
  ratingCount: number;
  status: HostStatus;
  joinedAt: string;
};

export type KycDocument = {
  id: string;
  documentSide: DocumentSide;
  fileUrl: string;
  createdAt: string;
};

// Item KYC hiển thị trong section KYC của Customer Detail (chưa tải documents).
// Khi user click "Xem giấy tờ", FE mới fetch CustomerKycDetail kèm documents.
export type CustomerKycSummary = {
  id: string;
  kycCode: string;
  legalName: string;
  documentType: DocumentType;
  documentNumber: string;
  issuedDate?: string | null;
  issuedPlace?: string | null;
  status: KycStatus;
  reviewedBy?: string | null;
  reviewedAt?: string | null;
  rejectionReason?: string | null;
  submittedAt: string;
};

// Chi tiết đầy đủ 1 KYC trả về cho popup review (= CustomerKycSummary + ảnh).
export type CustomerKycDetail = CustomerKycSummary & {
  documents: KycDocument[];
};

export type CustomerActivity = {
  bookingCount: number;
  vehicleCount: number;
  totalRevenue: number;
};

export type AdminCustomer = {
  id: string;
  fullName: string;
  phone?: string | null;
  email?: string | null;
  dateOfBirth?: string | null;
  gender?: Gender | null;
  status: CustomerStatus;
  joinedAt: string;
  roles: CustomerRole[];
  hostProfile?: HostProfile | null;
  // Multi-KYC: customer có 0..N hồ sơ. List page trả về [] vì không nạp chi
  // tiết — chỉ dùng kycAggregateStatus để hiển thị badge.
  kycs: CustomerKycSummary[];
  kycAggregateStatus: KycAggregateStatus;
  addresses: CustomerAddress[];
  activity: CustomerActivity;
};

export type CustomerRoleFilter = "all" | "renter" | "host" | "both";

export type CustomerStatusFilter = "all" | CustomerStatus;

// Filter dropdown ở list page lọc theo aggregate KYC status, không theo từng
// hồ sơ riêng — vì 1 customer có nhiều KYC, lọc per-KYC không có ý nghĩa.
export type CustomerKycFilter = "all" | KycAggregateStatus;

export type CustomerFiltersState = {
  q?: string;
  role?: CustomerRoleFilter;
  status?: CustomerStatusFilter;
  kyc?: CustomerKycFilter;
};

export type CustomerPageParams = CustomerFiltersState & {
  page?: number;
  size?: number;
};

export type CustomerStatsSummary = {
  total: number;
  renters: number;
  hosts: number;
  pendingOrBlocked: number;
};

export type SelectOption<T extends string> = {
  value: T;
  label: string;
};

export type BadgeMeta = {
  label: string;
  className: string;
};

export type UpdateCustomerRequest = {
  fullName: string;
  phone: string;
  email?: string | null;
  dateOfBirth?: string | null;
  gender?: Gender | null;
};

export type RejectKycRequest = {
  rejectionReason: string;
};
