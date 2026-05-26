import { useState } from "react";
import {CustomerStats} from "../components/CustomerStats";

type CustomerRole = "renter" | "host" | "both";
type CustomerFilter = "all" | CustomerRole;
type CustomerStatus = "active" | "blocked";
type ToastType = "success" | "error" | "warning";

type Customer = {
    name: string;
    init: string;
    col: string;
    phone: string;
    role: CustomerRole;
    bookings: number;
    revenue: number;
    rating: number;
    joined: string;
    status: CustomerStatus;
};

const customers: Customer[] = [
    {
        name: "Trần Hưng",
        init: "TH",
        col: "var(--info)",
        phone: "0901234567",
        role: "renter",
        bookings: 12,
        revenue: 38400000,
        rating: 4.8,
        joined: "01/2024",
        status: "active",
    },
    {
        name: "Mai Linh",
        init: "ML",
        col: "var(--success)",
        phone: "0912345678",
        role: "both",
        bookings: 8,
        revenue: 62100000,
        rating: 4.9,
        joined: "11/2023",
        status: "active",
    },
    {
        name: "Phạm Duy",
        init: "PD",
        col: "var(--accent)",
        phone: "0923456789",
        role: "renter",
        bookings: 5,
        revenue: 22500000,
        rating: 4.5,
        joined: "03/2024",
        status: "active",
    },
    {
        name: "Nguyễn An",
        init: "NA",
        col: "var(--danger)",
        phone: "0934567890",
        role: "host",
        bookings: 0,
        revenue: 45800000,
        rating: 4.7,
        joined: "06/2023",
        status: "active",
    },
    {
        name: "Lê Thảo",
        init: "LT",
        col: "var(--info)",
        phone: "0945678901",
        role: "renter",
        bookings: 3,
        revenue: 6300000,
        rating: 4.2,
        joined: "02/2024",
        status: "blocked",
    },
    {
        name: "Hoàng Nam",
        init: "HN",
        col: "var(--warning)",
        phone: "0956789012",
        role: "both",
        bookings: 15,
        revenue: 85200000,
        rating: 4.9,
        joined: "08/2023",
        status: "active",
    },
    {
        name: "Bùi Hoa",
        init: "BH",
        col: "var(--success)",
        phone: "0967890123",
        role: "host",
        bookings: 0,
        revenue: 31500000,
        rating: 4.6,
        joined: "10/2023",
        status: "active",
    },
    {
        name: "Võ Trang",
        init: "VT",
        col: "var(--accent)",
        phone: "0978901234",
        role: "renter",
        bookings: 7,
        revenue: 8400000,
        rating: 4.3,
        joined: "04/2024",
        status: "active",
    },
];

const roleLabels: Record<CustomerRole, string> = {
    renter: "Người thuê",
    host: "Chủ xe",
    both: "Cả hai",
};

const roleStyles: Record<CustomerRole, string> = {
    renter: "tag-p2p",
    host: "tag-self",
    both: "tag-chauffeur",
};

export function CustomerListPage() {
    const [activeRole, setActiveRole] = useState<CustomerFilter>("all");
    const [isAddCustomerOpen, setIsAddCustomerOpen] = useState(false);

    const filteredCustomers =
        activeRole === "all"
            ? customers
            : customers.filter((customer) => customer.role === activeRole);

    const closeAddCustomerModal = (): void => {
        setIsAddCustomerOpen(false);
    };

    const addCustomer = (): void => {
        closeAddCustomerModal();
        showToast("Thêm khách hàng thành công!", "success");
    };

    return (
        <>
            <div className="page-header">
                <div className="page-header-left">
                    <h2>Khách hàng</h2>
                    <p>5,891 khách hàng đã đăng ký</p>
                </div>

                <div className="flex gap-8">
                    <button
                        className="btn btn-secondary btn-sm"
                        type="button"
                        onClick={() => showToast("Đã xuất danh sách", "success")}
                    >
                        Xuất CSV
                    </button>

                    <button
                        className="btn btn-primary btn-sm"
                        type="button"
                        onClick={() => setIsAddCustomerOpen(true)}
                    >
                        + Thêm KH
                    </button>
                </div>
            </div>

            <CustomerStats/>

            <div
                className="card animate-fade-up"
                style={{ animationDelay: ".15s" }}
            >
                <div style={{ padding: "20px 24px 0" }}>
                    <div className="tabs">
                        <button
                            type="button"
                            className={`tab ${activeRole === "all" ? "active" : ""}`}
                            onClick={() => setActiveRole("all")}
                        >
                            Tất cả
                        </button>

                        <button
                            type="button"
                            className={`tab ${activeRole === "renter" ? "active" : ""}`}
                            onClick={() => setActiveRole("renter")}
                        >
                            Người thuê
                        </button>

                        <button
                            type="button"
                            className={`tab ${activeRole === "host" ? "active" : ""}`}
                            onClick={() => setActiveRole("host")}
                        >
                            Chủ xe
                        </button>

                        <button
                            type="button"
                            className={`tab ${activeRole === "both" ? "active" : ""}`}
                            onClick={() => setActiveRole("both")}
                        >
                            Cả hai
                        </button>
                    </div>
                </div>

                <div
                    className="table-wrapper"
                    style={{ padding: "0 24px 24px" }}
                >
                    <table>
                        <thead>
                        <tr>
                            <th>Khách hàng</th>
                            <th>SĐT</th>
                            <th>Vai trò</th>
                            <th>Booking</th>
                            <th>Doanh thu</th>
                            <th>Đánh giá</th>
                            <th>Tham gia</th>
                            <th>Trạng thái</th>
                        </tr>
                        </thead>

                        <tbody>
                        {filteredCustomers.map((customer) => (
                            <tr key={customer.phone}>
                                <td>
                                    <div className="flex items-center gap-12">
                                        <div
                                            className="avatar avatar-md"
                                            style={{
                                                background: `${customer.col}15`,
                                                color: customer.col,
                                            }}
                                        >
                                            {customer.init}
                                        </div>

                                        <div>
                                            <div style={{ fontWeight: 500 }}>{customer.name}</div>
                                            <div
                                                style={{
                                                    fontSize: "11px",
                                                    color: "var(--text-muted)",
                                                }}
                                            >
                                                {getCustomerSubtitle(customer.role)}
                                            </div>
                                        </div>
                                    </div>
                                </td>

                                <td style={{ color: "var(--text-secondary)" }}>
                                    {customer.phone}
                                </td>

                                <td>
                                        <span className={`tag ${roleStyles[customer.role]}`}>
                                            {roleLabels[customer.role]}
                                        </span>
                                </td>

                                <td>{customer.bookings}</td>

                                <td>
                                        <span
                                            className="font-display"
                                            style={{
                                                fontWeight: 500,
                                                color: "var(--accent)",
                                            }}
                                        >
                                            {formatVND(customer.revenue)}
                                        </span>
                                </td>

                                <td>⭐ {customer.rating}</td>

                                <td style={{ color: "var(--text-muted)" }}>
                                    {customer.joined}
                                </td>

                                <td>
                                        <span
                                            className={`badge ${
                                                customer.status === "active"
                                                    ? "badge-active"
                                                    : "badge-cancelled"
                                            }`}
                                        >
                                            {customer.status === "active" ? "Hoạt động" : "Bị khóa"}
                                        </span>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {isAddCustomerOpen && (
                <div
                    className="modal-overlay"
                    onClick={(event) => {
                        if (event.target === event.currentTarget) {
                            closeAddCustomerModal();
                        }
                    }}
                >
                    <div className="modal">
                        <div className="modal-header">
                            <h3 className="modal-title">Thêm khách hàng</h3>
                            <button
                                className="modal-close"
                                type="button"
                                onClick={closeAddCustomerModal}
                            >
                                ✕
                            </button>
                        </div>

                        <div className="modal-body">
                            <div className="form-row">
                                <div className="form-group">
                                    <label className="form-label">Họ tên</label>
                                    <input className="form-input" placeholder="Nguyễn Văn A" />
                                </div>

                                <div className="form-group">
                                    <label className="form-label">SĐT</label>
                                    <input className="form-input" placeholder="0901234567" />
                                </div>
                            </div>

                            <div className="form-row">
                                <div className="form-group">
                                    <label className="form-label">Email</label>
                                    <input
                                        className="form-input"
                                        type="email"
                                        placeholder="email@example.com"
                                    />
                                </div>

                                <div className="form-group">
                                    <label className="form-label">Vai trò</label>
                                    <select className="form-select">
                                        <option>Người thuê</option>
                                        <option>Chủ xe</option>
                                        <option>Cả hai</option>
                                    </select>
                                </div>
                            </div>

                            <div className="form-group">
                                <label className="form-label">CCCD/CMND</label>
                                <input className="form-input" placeholder="Số CCCD" />
                            </div>
                        </div>

                        <div className="modal-footer">
                            <button
                                className="btn btn-secondary"
                                type="button"
                                onClick={closeAddCustomerModal}
                            >
                                Hủy
                            </button>

                            <button
                                className="btn btn-primary"
                                type="button"
                                onClick={addCustomer}
                            >
                                Thêm
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}

function getCustomerSubtitle(role: CustomerRole): string {
    if (role === "host") {
        return "Host";
    }

    if (role === "both") {
        return "Renter & Host";
    }

    return "Renter";
}

function formatVND(value: number): string {
    return new Intl.NumberFormat("vi-VN").format(value) + "đ";
}

function showToast(message: string, type: ToastType = "success"): void {
    let container = document.querySelector(".toast-container") as HTMLElement | null;

    if (!container) {
        container = document.createElement("div");
        container.className = "toast-container";
        document.body.appendChild(container);
    }

    const icons: Record<ToastType, string> = {
        success: `<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7" stroke="#3ECF8E" stroke-width="1.3"/><path d="M6 9L8 11L12 7" stroke="#3ECF8E" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
        error: `<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7" stroke="#EF5A5A" stroke-width="1.3"/><path d="M6.5 6.5L11.5 11.5M11.5 6.5L6.5 11.5" stroke="#EF5A5A" stroke-width="1.3" stroke-linecap="round"/></svg>`,
        warning: `<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 2L16 15H2L9 2Z" stroke="#F5A623" stroke-width="1.3" stroke-linejoin="round"/><path d="M9 7V10M9 12V12.5" stroke="#F5A623" stroke-width="1.3" stroke-linecap="round"/></svg>`,
    };

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `${icons[type]}<span>${message}</span>`;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateX(40px)";
        toast.style.transition = "all 0.3s ease";

        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 3500);
}
