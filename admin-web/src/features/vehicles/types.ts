export type VehicleSource = "COMPANY_OWNED" | "HOST_OWNED";

export type VehicleStatus =
  | "DRAFT"
  | "PENDING_REVIEW"
  | "ACTIVE"
  | "INACTIVE"
  | "SUSPENDED";

export type ListingStatus = "DRAFT" | "PUBLISHED" | "PAUSED" | "REJECTED";

export type AdminVehicleListItem = {
  id: string;
  ownerCustomerId?: string | null;
  ownerCustomerName?: string | null;
  hostCode?: string | null;
  fleetVehicleId?: string | null;
  assetCode?: string | null;
  branchName?: string | null;
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
  listingStatus?: ListingStatus | null;
  city?: string | null;
  district?: string | null;
  baseDailyRate?: number | null;
  coverImageUrl?: string | null;
  featureCount: number;
  activeAvailabilityBlockCount: number;
  bookingCount: number;
  createdAt: string;
  updatedAt: string;
};

export type AdminVehicleDetailOwner = {
  customerId: string;
  fullName?: string | null;
  phone?: string | null;
  email?: string | null;
  hostCode?: string | null;
  hostDisplayName?: string | null;
};

export type AdminVehicleDetailFleet = {
  fleetVehicleId: string;
  assetCode?: string | null;
  assetStatus?: string | null;
  branchId?: string | null;
  branchName?: string | null;
  branchCity?: string | null;
};

export type AdminVehicleDetailListing = {
  id: string;
  title?: string | null;
  description?: string | null;
  city?: string | null;
  district?: string | null;
  provinceCode?: string | null;
  communeCode?: string | null;
  provinceName?: string | null;
  communeName?: string | null;
  pickupAddress?: string | null;
  baseDailyRate?: number | null;
  currency?: string | null;
  instantBookingEnabled: boolean;
  deliveryEnabled: boolean;
  status?: ListingStatus | string | null;
  publishedAt?: string | null;
};

export type AdminVehicleDetailImage = {
  id: string;
  fileUrl: string;
  sortOrder: number;
  cover: boolean;
};

export type AdminVehicleDetailFeature = {
  id: string;
  code: string;
  name: string;
};

export type AdminVehicleDetailPricePlan = {
  id: string;
  name: string;
  baseDailyRate: number;
  hourlyRate?: number | null;
  weekendMultiplier?: number | null;
  depositAmount?: number | null;
  currency: string;
  status: string;
  validFrom?: string | null;
  validTo?: string | null;
};

export type AdminVehicleDetailAvailabilityBlock = {
  id: string;
  startAt: string;
  endAt: string;
  reason: string;
  bookingId?: string | null;
  note?: string | null;
};

export type AdminVehicleDetailRecentBooking = {
  id: string;
  bookingCode: string;
  customerId: string;
  startAt: string;
  endAt: string;
  totalAmount: number;
  currency: string;
  status: string;
  createdAt: string;
};

export type AdminVehicleDetail = {
  id: string;
  ownerCustomerId?: string | null;
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
  owner?: AdminVehicleDetailOwner | null;
  fleet?: AdminVehicleDetailFleet | null;
  listing?: AdminVehicleDetailListing | null;
  images: AdminVehicleDetailImage[];
  features: AdminVehicleDetailFeature[];
  activePricePlan?: AdminVehicleDetailPricePlan | null;
  upcomingAvailabilityBlocks: AdminVehicleDetailAvailabilityBlock[];
  recentBookings: AdminVehicleDetailRecentBooking[];
};

export type VehicleSort =
  | "CREATED_AT"
  | "UPDATED_AT"
  | "BASE_DAILY_RATE"
  | "MANUFACTURE_YEAR"
  | "BOOKING_COUNT";

export type SortDirection = "ASC" | "DESC";

export type VehicleFiltersState = {
  q?: string;
  source?: VehicleSource | "";
  status?: VehicleStatus | "";
  listingStatus?: ListingStatus | "";
  provinceCode?: string;
  communeCode?: string;
  fuelType?: string | "";
  transmission?: string | "";
  seats?: number | "";
  minRate?: number | "";
  maxRate?: number | "";
  hasBookings?: boolean;
};

export type VehiclePageParams = VehicleFiltersState & {
  page?: number;
  size?: number;
  sortBy?: VehicleSort;
  sortDir?: SortDirection;
};
