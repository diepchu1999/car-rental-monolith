import { httpClient } from "../../../services/api/httpClient";
import type { ApiResponse, PageResponse } from "../../../services/api/types";
import type { Vehicle, VehiclePageParams } from "../types";

export async function getVehiclesPage(params: VehiclePageParams = {}) {
  const response = await httpClient.get<ApiResponse<PageResponse<Vehicle>>>(
    "/admin/vehicles/paged",
    {
      params: {
        q: params.q || undefined,
        source: params.source || undefined,
        status: params.status || undefined,
        page: params.page ?? 1,
        size: params.size ?? 20,
      },
    },
  );

  return response.data.data;
}

export async function getVehicleById(id: string) {
  const response = await httpClient.get<ApiResponse<Vehicle>>(
    `/admin/vehicles/${id}`,
  );

  return response.data.data;
}
