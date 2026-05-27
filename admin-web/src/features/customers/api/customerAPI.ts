import { httpClient } from "../../../services/api/httpClient";
import type { ApiResponse, PageResponse } from "../../../services/api/types";
import type {
  AdminCustomer,
  CustomerPageParams,
  CustomerRole,
  CustomerStatsSummary,
  DocumentType,
  Gender,
  UpdateCustomerRequest,
} from "../types";

export type CustomerSummary = {
  id: string;
  fullName: string;
  phone?: string | null;
  email?: string | null;
  status: string;
  hostCode?: string | null;
};

// Search nhẹ (vehicle owner picker). Khác list ở chỗ trả CustomerSummary.
export async function searchCustomers(params: {
  q?: string;
  page?: number;
  size?: number;
}) {
  const response = await httpClient.get<ApiResponse<PageResponse<CustomerSummary>>>(
    "/admin/customers/search",
    {
      params: {
        q: params.q || undefined,
        page: params.page ?? 1,
        size: params.size ?? 20,
      },
    },
  );
  return response.data.data;
}

// List customer cho trang admin. URL theo REST conventional: GET /admin/customers.
// Filter kyc dùng aggregate status (NO_KYC / PENDING / PARTIALLY_APPROVED /
// FULLY_APPROVED / REJECTED).
export async function getAdminCustomersPage(params: CustomerPageParams = {}) {
  const response = await httpClient.get<ApiResponse<PageResponse<AdminCustomer>>>(
    "/admin/customers",
    { params: buildPagedParams(params) },
  );
  return response.data.data;
}

// Detail page: trả về danh sách kycs đầy đủ kèm documents.
export async function getAdminCustomerDetail(customerId: string) {
  const response = await httpClient.get<ApiResponse<AdminCustomer>>(
    `/admin/customers/${customerId}`,
  );
  return response.data.data;
}

// Alias giữ tương thích với chỗ gọi cũ ở CustomerDetailModal.
export const getAdminCustomerById = getAdminCustomerDetail;

export async function getCustomerStats() {
  const response = await httpClient.get<ApiResponse<CustomerStatsSummary>>(
    "/admin/customers/stats",
  );
  return response.data.data;
}

export type CreateCustomerInput = {
  fullName: string;
  phone: string;
  email?: string;
  dateOfBirth?: string;
  gender?: Gender;
  roles: CustomerRole[];
  host?: {
    hostCode?: string;
    displayName: string;
    bio?: string;
  };
  kyc?: {
    legalName: string;
    documentType: DocumentType;
    documentNumber: string;
    issuedDate?: string;
    issuedPlace?: string;
  };
  address?: {
    label: string;
    line1: string;
    provinceCode?: string;
    communeCode?: string;
  };
};

export async function createAdminCustomer(input: CreateCustomerInput) {
  const response = await httpClient.post<ApiResponse<AdminCustomer>>(
    "/admin/customers",
    input,
  );
  return response.data.data;
}

// Sửa thông tin cá nhân (KHÔNG đụng KYC / host / status / address). BE từ chối
// nếu thiếu fullName/phone hoặc gender ngoài enum.
export async function updateCustomer(
  customerId: string,
  payload: UpdateCustomerRequest,
) {
  const response = await httpClient.patch<ApiResponse<AdminCustomer>>(
    `/admin/customers/${customerId}`,
    payload,
  );
  return response.data.data;
}

export type CustomerStatusAction = "block" | "unblock";
export type HostStatusAction = "suspend" | "activate";

export async function changeCustomerStatus(id: string, action: CustomerStatusAction) {
  const response = await httpClient.patch<ApiResponse<AdminCustomer>>(
    `/admin/customers/${id}/status`,
    { action },
  );
  return response.data.data;
}

export async function changeHostStatus(id: string, action: HostStatusAction) {
  const response = await httpClient.patch<ApiResponse<AdminCustomer>>(
    `/admin/customers/${id}/host-status`,
    { action },
  );
  return response.data.data;
}

// KYC: getCustomerKycDetail + approveCustomerKyc + rejectCustomerKyc đã tách
// sang ./kycAPI.ts (cùng module Customer nhưng feature KYC review riêng).

function buildPagedParams(params: CustomerPageParams) {
  return {
    q: emptyToUndefined(params.q),
    role: allToUndefined(params.role),
    status: allToUndefined(params.status),
    kyc: allToUndefined(params.kyc),
    page: params.page ?? 1,
    size: params.size ?? 20,
  };
}

function emptyToUndefined(value: string | undefined | null) {
  if (value === undefined || value === null) return undefined;
  return value === "" ? undefined : value;
}

function allToUndefined(value: string | undefined | null) {
  if (!value || value === "all") return undefined;
  return value;
}
