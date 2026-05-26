import { httpClient } from "../../../services/api/httpClient";
import type { ApiResponse, ListResponse } from "../../../services/api/types";

export type AdministrativeUnit = {
  code: string;
  name: string;
  fullName?: string | null;
  type: string;
  parentCode?: string | null;
};

// Cấp tỉnh/thành phố đang ACTIVE.
export async function listProvinces() {
  const response = await httpClient.get<
    ApiResponse<ListResponse<AdministrativeUnit>>
  >("/admin/locations/provinces");
  return response.data.data;
}

// Cấp xã/phường/đặc khu thuộc một tỉnh.
export async function listCommunes(provinceCode: string) {
  const response = await httpClient.get<
    ApiResponse<ListResponse<AdministrativeUnit>>
  >("/admin/locations/communes", { params: { provinceCode } });
  return response.data.data;
}

export async function searchAdministrativeUnits(params: {
  q?: string;
  level?: "PROVINCE" | "COMMUNE";
  provinceCode?: string;
}) {
  const response = await httpClient.get<
    ApiResponse<ListResponse<AdministrativeUnit>>
  >("/admin/locations/search", {
    params: {
      q: params.q || undefined,
      level: params.level || undefined,
      provinceCode: params.provinceCode || undefined,
    },
  });
  return response.data.data;
}
