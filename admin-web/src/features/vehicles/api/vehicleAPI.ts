import { httpClient } from "../../../services/api/httpClient";
import type { ApiResponse, PageResponse } from "../../../services/api/types";
import type {
  AdminVehicleDetail,
  AdminVehicleListItem,
  VehiclePageParams,
} from "../types";

export type VehicleStatusAction = "activate" | "suspend" | "deactivate";
export type ListingStatusAction = "publish" | "pause" | "archive" | "draft";

export type CreateVehiclePayload = {
  source: "HOST_OWNED" | "COMPANY_OWNED";
  ownerCustomerId?: string | null;
  assetCode?: string | null;
  branchId?: string | null;
  brand: string;
  model: string;
  version?: string | null;
  manufactureYear: number;
  licensePlate: string;
  seats: number;
  transmission: string;
  fuelType: string;
};

export type UpdateListingPayload = {
  title?: string | null;
  description?: string | null;
  provinceCode?: string | null;
  communeCode?: string | null;
  pickupAddress?: string | null;
  baseDailyRate?: number | null;
  currency?: string | null;
  instantBookingEnabled?: boolean;
  deliveryEnabled?: boolean;
};

export type SyncImagesPayload = {
  images: Array<{ fileUrl: string; sortOrder: number; cover: boolean }>;
};

export type SyncFeaturesPayload = {
  features: Array<{ code: string; name: string }>;
};

export type UpsertPricePlanPayload = {
  name: string;
  baseDailyRate: number;
  hourlyRate?: number | null;
  weekendMultiplier?: number | null;
  depositAmount?: number | null;
  currency?: string | null;
};

export async function getVehiclesPage(params: VehiclePageParams = {}) {
  const response = await httpClient.get<
    ApiResponse<PageResponse<AdminVehicleListItem>>
  >("/admin/vehicles/paged", {
    params: buildPagedParams(params),
  });

  return response.data.data;
}

export async function getVehicleById(id: string) {
  const response = await httpClient.get<ApiResponse<AdminVehicleDetail>>(
    `/admin/vehicles/${id}`,
  );
  return response.data.data;
}

export async function createVehicle(payload: CreateVehiclePayload) {
  const response = await httpClient.post<ApiResponse<AdminVehicleDetail>>(
    "/admin/vehicles",
    payload,
  );
  return response.data.data;
}

export async function uploadVehicleImage(file: File) {
  const formData = new FormData();
  formData.append("file", file);
  const response = await httpClient.post<ApiResponse<{ fileName: string }>>(
    "/admin/vehicles/images",
    formData,
    { headers: { "Content-Type": "multipart/form-data" } },
  );
  return response.data.data;
}

const MEDIA_BASE_URL =
  import.meta.env.VITE_MEDIA_BASE_URL ?? "http://localhost:8080";

// Stored images keep only their filename in the DB. Absolute URLs (legacy/seed
// data) are returned untouched; bare filenames resolve to the media endpoint.
export function resolveVehicleImageUrl(fileUrl: string): string {
  if (!fileUrl) return "";
  if (/^https?:\/\//i.test(fileUrl) || fileUrl.startsWith("/")) return fileUrl;
  return `${MEDIA_BASE_URL}/media/vehicle-images/${fileUrl}`;
}

export async function changeVehicleStatus(id: string, action: VehicleStatusAction) {
  const response = await httpClient.patch<ApiResponse<AdminVehicleDetail>>(
    `/admin/vehicles/${id}/status`,
    { action },
  );
  return response.data.data;
}

export async function changeListingStatus(id: string, action: ListingStatusAction) {
  const response = await httpClient.patch<ApiResponse<AdminVehicleDetail>>(
    `/admin/vehicles/${id}/listing/status`,
    { action },
  );
  return response.data.data;
}

export async function updateListing(id: string, payload: UpdateListingPayload) {
  const response = await httpClient.patch<ApiResponse<AdminVehicleDetail>>(
    `/admin/vehicles/${id}/listing`,
    payload,
  );
  return response.data.data;
}

export async function syncImages(id: string, payload: SyncImagesPayload) {
  const response = await httpClient.put<ApiResponse<AdminVehicleDetail>>(
    `/admin/vehicles/${id}/images`,
    payload,
  );
  return response.data.data;
}

export async function syncFeatures(id: string, payload: SyncFeaturesPayload) {
  const response = await httpClient.put<ApiResponse<AdminVehicleDetail>>(
    `/admin/vehicles/${id}/features`,
    payload,
  );
  return response.data.data;
}

export async function upsertPricePlan(id: string, payload: UpsertPricePlanPayload) {
  const response = await httpClient.put<ApiResponse<AdminVehicleDetail>>(
    `/admin/vehicles/${id}/price-plan`,
    payload,
  );
  return response.data.data;
}

function buildPagedParams(params: VehiclePageParams) {
  return {
    q: emptyToUndefined(params.q),
    source: emptyToUndefined(params.source),
    status: emptyToUndefined(params.status),
    listingStatus: emptyToUndefined(params.listingStatus),
    provinceCode: emptyToUndefined(params.provinceCode),
    communeCode: emptyToUndefined(params.communeCode),
    fuelType: emptyToUndefined(params.fuelType),
    transmission: emptyToUndefined(params.transmission),
    seats: numberOrUndefined(params.seats),
    minRate: numberOrUndefined(params.minRate),
    maxRate: numberOrUndefined(params.maxRate),
    hasBookings: params.hasBookings === undefined ? undefined : params.hasBookings,
    page: params.page ?? 1,
    size: params.size ?? 20,
    sortBy: params.sortBy,
    sortDir: params.sortDir,
  };
}

function emptyToUndefined(value: string | undefined | null) {
  if (value === undefined || value === null) return undefined;
  return value === "" ? undefined : value;
}

function numberOrUndefined(value: number | "" | undefined) {
  if (value === undefined || value === "") return undefined;
  return value;
}
