export type VehicleSource = "COMPANY_OWNED" | "HOST_OWNED";

export type VehicleStatus =
  | "DRAFT"
  | "PENDING_REVIEW"
  | "ACTIVE"
  | "INACTIVE"
  | "SUSPENDED";

export type Vehicle = {
  id: string;
  ownerCustomerId?: string | null;
  ownerCustomerName?: string | null;
  fleetVehicleId?: string | null;
  source: VehicleSource;
  brand: string;
  model: string;
  version?: string | null;
  manufactureYear: number;
  licensePlate: string;
  seats: number;
  transmission: string;
  fuelType: string;
  status: VehicleStatus;
  createdAt: string;
  updatedAt: string;
};

export type VehicleFiltersState = {
  q?: string;
  source?: VehicleSource | "";
  status?: VehicleStatus | "";
};

export type VehiclePageParams = VehicleFiltersState & {
  page?: number;
  size?: number;
};
