export type ApiResponse<T> = {
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
};

export type ListResponse<T> = {
  items: T[];
  total: number;
};

export type PageResponse<T> = {
  items: T[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
};
