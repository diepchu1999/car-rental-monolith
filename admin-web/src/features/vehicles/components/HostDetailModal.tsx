import { useEffect, type ReactNode } from "react";
import { X } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { getAdminCustomerDetail } from "../../customers/api/customerAPI";
import type { Gender, HostStatus } from "../../customers/types";
import {
  formatDateTime,
  formatFullDate,
  getAvatarColor,
  getInitials,
} from "../../customers/utils";

type HostDetailModalProps = {
  customerId: string | null;
  onClose: () => void;
};

const genderLabel: Record<Gender, string> = {
  MALE: "Nam",
  FEMALE: "Nữ",
  OTHER: "Khác",
};

const hostStatusLabel: Record<HostStatus, string> = {
  ACTIVE: "Đang hoạt động",
  SUSPENDED: "Tạm ngưng",
  PENDING_KYC: "Chờ KYC",
};

// View chi tiết host READ-ONLY cho luồng chọn chủ xe. KHÔNG có sửa/khóa/duyệt —
// chỉ để xem trước khi chọn. CSS tự chứa trong vehicles.css (không phụ thuộc
// customers.css vốn nằm ở chunk riêng, không load khi đang ở trang Vehicles).
export function HostDetailModal({ customerId, onClose }: HostDetailModalProps) {
  const open = customerId !== null;

  const query = useQuery({
    queryKey: ["customers", "detail", customerId],
    queryFn: () => getAdminCustomerDetail(customerId!),
    enabled: open,
  });

  useEffect(() => {
    if (!open) return;
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  const customer = query.data;
  const host = customer?.hostProfile ?? null;
  const color = customer ? getAvatarColor(customer.id) : "var(--accent)";

  function handleBackdropClick(event: React.MouseEvent<HTMLDivElement>) {
    if (event.target === event.currentTarget) onClose();
  }

  return (
    <div className="modal-overlay active" onClick={handleBackdropClick}>
      <div className="modal host-detail-modal">
        <div className="modal-header">
          <h3 className="modal-title">Chi tiết chủ xe</h3>
          <button type="button" className="modal-close" onClick={onClose} title="Đóng">
            <X size={16} />
          </button>
        </div>

        {query.isLoading ? (
          <div className="modal-body host-detail-state">Đang tải...</div>
        ) : query.isError || !customer ? (
          <div className="modal-body host-detail-state">
            Không tải được chi tiết chủ xe.
          </div>
        ) : (
          <>
            <div className="modal-body host-detail-body">
              <div className="host-detail-hero">
                <div
                  className="host-detail-avatar"
                  style={{ background: `${color}26`, color }}
                >
                  {getInitials(customer.fullName)}
                </div>
                <div>
                  <div className="host-detail-name">{customer.fullName}</div>
                  <div className="host-detail-contact">
                    {[customer.phone, customer.email].filter(Boolean).join(" · ") || "—"}
                  </div>
                </div>
              </div>

              <Section title="Hồ sơ host">
                {host ? (
                  <>
                    <Field label="Mã host" value={host.hostCode} />
                    <Field label="Tên hiển thị" value={host.displayName} />
                    <Field
                      label="Đánh giá"
                      value={`${host.ratingAverage.toFixed(1)} ★ (${host.ratingCount})`}
                    />
                    <Field label="Trạng thái host" value={hostStatusLabel[host.status]} />
                    <Field label="Trở thành host" value={formatFullDate(host.joinedAt)} />
                    {host.bio ? <Field label="Giới thiệu" value={host.bio} /> : null}
                  </>
                ) : (
                  <div className="host-detail-empty">Khách hàng chưa có hồ sơ host.</div>
                )}
              </Section>

              <Section title="Thông tin cá nhân">
                <Field label="Họ tên" value={customer.fullName} />
                <Field label="Số điện thoại" value={customer.phone} />
                <Field label="Email" value={customer.email} />
                <Field label="Ngày sinh" value={formatFullDate(customer.dateOfBirth)} />
                <Field
                  label="Giới tính"
                  value={customer.gender ? genderLabel[customer.gender] : null}
                />
                <Field label="Tham gia" value={formatDateTime(customer.joinedAt)} />
              </Section>
            </div>

            <div className="modal-footer">
              <button type="button" className="btn btn-secondary" onClick={onClose}>
                Đóng
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className="host-detail-section">
      <div className="host-detail-section-title">{title}</div>
      <div className="host-detail-grid">{children}</div>
    </div>
  );
}

function Field({
  label,
  value,
}: {
  label: string;
  value: string | number | null | undefined;
}) {
  const isEmpty = value === null || value === undefined || value === "";
  return (
    <div className="host-detail-field">
      <label>{label}</label>
      <span className={isEmpty ? "host-detail-muted" : ""}>
        {isEmpty ? "—" : String(value)}
      </span>
    </div>
  );
}
