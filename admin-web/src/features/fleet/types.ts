export type BranchStatus = "ACTIVE" | "INACTIVE";

// Bản ghi chi nhánh đầy đủ (list paged + detail). Khác BranchSummary trong
// fleetAPI.ts (vốn chỉ dùng cho dropdown chọn chi nhánh ở Vehicle form).
export type Branch = {
  id: string;
  code: string;
  name: string;
  address: string;
  city: string;
  phone: string;
  status: BranchStatus;
  createdAt: string;
  updatedAt: string;
};

export type BranchStatusFilter = "all" | "active" | "inactive";

export type BranchFiltersState = {
  q?: string;
  status?: BranchStatusFilter;
};

export type BranchPageParams = BranchFiltersState & {
  page?: number;
  size?: number;
};

// Payload tạo/sửa chi nhánh — đúng các field form cho phép chỉnh.
export type BranchInput = {
  code: string;
  name: string;
  city: string;
  address: string;
  phone: string;
  status: BranchStatus;
};
