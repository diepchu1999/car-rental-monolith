import { httpClient } from "../../../services/api/httpClient";
import type { ApiResponse } from "../../../services/api/types";
import type {
  AdminCustomer,
  CustomerKycDetail,
  RejectKycRequest,
} from "../types";

// GET 1 hồ sơ KYC kèm documents. Mở từ popup "Xem giấy tờ" trên customer
// detail; FE truyền kycId của item đang chọn.
export async function getCustomerKycDetail(
  customerId: string,
  kycId: string,
): Promise<CustomerKycDetail> {
  const response = await httpClient.get<ApiResponse<CustomerKycDetail>>(
    `/admin/customers/${customerId}/kyc/${kycId}`,
  );
  return response.data.data;
}

// Duyệt 1 KYC. BE chặn nếu KYC đã APPROVED hoặc không thuộc customer. Trả về
// AdminCustomer đã reload để FE cập nhật badge/aggregate ở list + detail +
// popup trong cùng response.
export async function approveCustomerKyc(
  customerId: string,
  kycId: string,
): Promise<AdminCustomer> {
  const response = await httpClient.patch<ApiResponse<AdminCustomer>>(
    `/admin/customers/${customerId}/kyc/${kycId}/approve`,
  );
  return response.data.data;
}

// Từ chối 1 KYC. rejectionReason bắt buộc — BE ném 400 nếu rỗng.
export async function rejectCustomerKyc(
  customerId: string,
  kycId: string,
  payload: RejectKycRequest,
): Promise<AdminCustomer> {
  const response = await httpClient.patch<ApiResponse<AdminCustomer>>(
    `/admin/customers/${customerId}/kyc/${kycId}/reject`,
    payload,
  );
  return response.data.data;
}
