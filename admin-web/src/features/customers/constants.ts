import {CustomerStat, CustomerStatus, CustomerStatusMeta} from "./types";

export const customerStatusMeta: Record<CustomerStatus, CustomerStatusMeta> = {
    available: {label: "Sẵn sàng", className: "badge-active"},
    driving: {label: "Đang chạy", className: "badge-completed"},
    off: {label: "Nghỉ phép", className: "badge-pending"},
};

export const customerStats: CustomerStat[] = [
    {
        type: "renter",
        label: "Người thuê xe",
        value: "4,320",
        style: "var(--success-dim)",
        trend: "▲ 5.2%",
        showTrend: true,
    },
    {
        type: "host",
        label: "Chủ xe (Host)",
        value: "1,571",
        style: "var(--accent-dim)",
        trend: "▲ 12%",
        showTrend: true,
    },
    {
        type: "both",
        label: "Cả hai vai trò",
        value: "892",
        style: "var(--info-dim)",
        showTrend: false,
    },
];