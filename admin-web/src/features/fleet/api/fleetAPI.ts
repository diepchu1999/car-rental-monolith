import { httpClient } from "../../../services/api/httpClient";
import type {
  ApiResponse,
  ListResponse,
  PageResponse,
} from "../../../services/api/types";

export type FleetVehicleSummary = {
  id: string;
  vehicleId?: string | null;
  assetCode: string;
  assetStatus: string;
  branchId?: string | null;
  branchName?: string | null;
  branchCity?: string | null;
  licensePlate?: string | null;
};

export type BranchSummary = {
  id: string;
  code: string;
  name: string;
  city: string;
  status: string;
};

export async function searchFleetVehicles(params: {
  q?: string;
  branchId?: string;
  page?: number;
  size?: number;
}) {
  const response = await httpClient.get<
    ApiResponse<PageResponse<FleetVehicleSummary>>
  >("/admin/fleet/vehicles/search", {
    params: {
      q: params.q || undefined,
      branchId: params.branchId || undefined,
      page: params.page ?? 1,
      size: params.size ?? 20,
    },
  });
  return response.data.data;
}

export async function listActiveBranches() {
  const response = await httpClient.get<ApiResponse<ListResponse<BranchSummary>>>(
    "/admin/fleet/branches",
  );
  return response.data.data;
}
