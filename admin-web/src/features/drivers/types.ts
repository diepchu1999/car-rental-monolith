export type DriverStatus = "available" | "driving" | "off";

export type Driver = {
  id: string;
  name: string;
  phone: string;
  license: string;
  licenseNo: string;
  exp: number;
  trips: number;
  rating: number;
  status: DriverStatus;
};

export type DriverStatusMeta = {
  label: string;
  className: string;
};

export type DriverStat = {
  label: string;
  value: string;
  className: string;
};
