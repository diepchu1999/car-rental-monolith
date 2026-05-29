import { httpClient } from "../../../services/api/httpClient";
import type {
  ApiResponse,
  ListResponse,
  PageResponse,
} from "../../../services/api/types";
import type {
  Branch,
  BranchInput,
  BranchPageParams,
  BranchStatus,
  BranchStatusFilter,
} from "../types";

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

// === Quản lý chi nhánh (Fleet > Chi nhánh) ===
// Map filter UI -> tham số status backend. "all" => bỏ qua (không gửi).
function statusFilterParam(status?: BranchStatusFilter): BranchStatus | undefined {
  if (status === "active") return "ACTIVE";
  if (status === "inactive") return "INACTIVE";
  return undefined;
}

// GET /admin/fleet/branches/paged?q=&status=&page=&size=
export async function getBranchesPage(params: BranchPageParams = {}) {
  const response = await httpClient.get<ApiResponse<PageResponse<Branch>>>(
    "/admin/fleet/branches/paged",
    {
      params: {
        q: params.q?.trim() || undefined,
        status: statusFilterParam(params.status),
        page: params.page ?? 1,
        size: params.size ?? 20,
      },
    },
  );
  return response.data.data;
}

// GET /admin/fleet/branches/{id}
export async function getBranchDetail(id: string) {
  const response = await httpClient.get<ApiResponse<Branch>>(
    `/admin/fleet/branches/${id}`,
  );
  return response.data.data;
}

// POST /admin/fleet/branches
export async function createBranch(input: BranchInput) {
  const response = await httpClient.post<ApiResponse<Branch>>(
    "/admin/fleet/branches",
    input,
  );
  return response.data.data;
}

// PATCH /admin/fleet/branches/{id}
export async function updateBranch(id: string, input: BranchInput) {
  const response = await httpClient.patch<ApiResponse<Branch>>(
    `/admin/fleet/branches/${id}`,
    input,
  );
  return response.data.data;
}

// PATCH /admin/fleet/branches/{id}/status — body { status }.
export async function changeBranchStatus(id: string, status: BranchStatus) {
  const response = await httpClient.patch<ApiResponse<Branch>>(
    `/admin/fleet/branches/${id}/status`,
    { status },
  );
  return response.data.data;
}
