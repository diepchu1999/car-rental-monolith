import { Eye } from "lucide-react";
import type { AdminCustomer } from "../types";
import { formatMonthYear, formatVND, getAvatarColor, getInitials } from "../utils";
import { CustomerKycAggregateBadge } from "./CustomerKycAggregateBadge";
import { CustomerRoleBadges } from "./CustomerRoleBadges";
import { CustomerStatusBadge } from "./CustomerStatusBadge";

type CustomerTableProps = {
  customers: AdminCustomer[];
  fetching?: boolean;
  onViewDetail: (id: string) => void;
};

export function CustomerTable({
  customers,
  fetching,
  onViewDetail,
}: CustomerTableProps) {
  return (
    <div className="card animate-fade-up customer-table-card">
      {fetching ? (
        <div className="customer-grid-loading">Đang cập nhật...</div>
      ) : null}
      <div className="table-wrapper customer-table-wrapper">
        <table className="customer-table">
          <thead>
            <tr>
              <th>Khách hàng</th>
              <th>Liên hệ</th>
              <th>Vai trò</th>
              <th>Mã host</th>
              <th>KYC</th>
              <th>Trạng thái</th>
              <th>Tham gia</th>
              <th className="customer-col-activity">Hoạt động</th>
              <th className="customer-col-action" />
            </tr>
          </thead>
          <tbody>
            {customers.map((customer) => (
              <tr key={customer.id} onDoubleClick={() => onViewDetail(customer.id)}>
                <td>
                  <div className="flex items-center gap-12">
                    <div
                      className="avatar avatar-md"
                      style={{
                        background: `${getAvatarColor(customer.id)}26`,
                        color: getAvatarColor(customer.id),
                      }}
                    >
                      {getInitials(customer.fullName)}
                    </div>
                    <div className="customer-name-cell">
                      <div className="customer-name">{customer.fullName}</div>
                      <div className="customer-id">{customer.id}</div>
                    </div>
                  </div>
                </td>

                <td>
                  <div className="customer-contact">
                    <span>{customer.phone ?? "—"}</span>
                    <span className="customer-contact-muted">
                      {customer.email ?? "—"}
                    </span>
                  </div>
                </td>

                <td>
                  <CustomerRoleBadges roles={customer.roles} />
                </td>

                <td>
                  {customer.hostProfile ? (
                    <span className="customer-host-code">
                      {customer.hostProfile.hostCode}
                    </span>
                  ) : (
                    <span className="customer-muted">—</span>
                  )}
                </td>

                <td>
                  <CustomerKycAggregateBadge status={customer.kycAggregateStatus} />
                </td>

                <td>
                  <CustomerStatusBadge status={customer.status} />
                </td>

                <td className="customer-muted">
                  {formatMonthYear(customer.joinedAt)}
                </td>

                <td className="customer-col-activity">
                  <div className="customer-activity">
                    <span>{customer.activity.bookingCount} booking</span>
                    <span>{customer.activity.vehicleCount} xe</span>
                    <span className="customer-activity-revenue">
                      {formatVND(customer.activity.totalRevenue)}
                    </span>
                  </div>
                </td>

                <td className="customer-col-action">
                  <button
                    className="btn btn-secondary btn-sm"
                    type="button"
                    onClick={() => onViewDetail(customer.id)}
                  >
                    <Eye size={14} />
                    Chi tiết
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
