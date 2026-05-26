import { httpClient } from "../../../services/api/httpClient";
import type { ApiResponse, PageResponse } from "../../../services/api/types";

export type CustomerSummary = {
  id: string;
  fullName: string;
  phone?: string | null;
  email?: string | null;
  status: string;
  hostCode?: string | null;
};

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
