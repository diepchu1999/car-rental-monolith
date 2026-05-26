export type CustomerStatus = "available" | "driving" | "off";
export type CustomerStatType = "renter" | "host" | "both";
export type Customer = {
    id: string;
    name: string;
    phone: string;
    license: string;
    licenseNo: string;
    exp: number;
    trips: number;
    rating: number;
    status: CustomerStatus;
};

export type CustomerStatusMeta = {
    label: string;
    className: string;
};

export type CustomerStat = {
    type: CustomerStatType;
    label: string;
    value: string;
    style: string;
    trend?: string;
    showTrend?: boolean;
};