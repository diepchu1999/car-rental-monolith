import { useEffect, useState, type ReactNode } from "react";
import {
  Ban,
  FileSearch,
  PauseCircle,
  Pencil,
  X,
} from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  changeCustomerStatus,
  changeHostStatus,
  getAdminCustomerDetail,
} from "../api/customerAPI";
import {
  documentTypeLabel,
  genderLabel,
  hostStatusMeta,
  kycAggregateStatusMeta,
} from "../constants";
import { showToast } from "../notify";
import type { AdminCustomer, CustomerKycSummary } from "../types";
import {
  formatDateTime,
  formatFullDate,
  formatVND,
  getAvatarColor,
  getInitials,
  maskDocumentNumber,
} from "../utils";
import { CustomerKycBadge } from "./CustomerKycBadge";
import { CustomerRoleBadges } from "./CustomerRoleBadges";
import { CustomerStatusBadge } from "./CustomerStatusBadge";
import { EditCustomerModal } from "./EditCustomerModal";
import { KycDocumentReviewModal } from "./KycDocumentReviewModal";

type CustomerDetailModalProps = {
  customerId: string | null;
  onClose: () => void;
};

export function CustomerDetailModal({
  customerId,
  onClose,
}: CustomerDetailModalProps) {
  const open = customerId !== null;
  const [customer, setCustomer] = useState<AdminCustomer | null>(null);
  const [editOpen, setEditOpen] = useState(false);
  const [reviewKycId, setReviewKycId] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const customerQuery = useQuery({
    queryKey: ["customers", "detail", customerId],
    queryFn: () => getAdminCustomerDetail(customerId!),
    enabled: open,
  });

  const statusMutation = useMutation({
    mutationFn: (action: "block" | "unblock") =>
      changeCustomerStatus(customerId!, action),
    onSuccess: (updated, action) => {
      setCustomer(updated);
      queryClient.invalidateQueries({ queryKey: ["customers"] });
      showToast(
        action === "block" ? "Đã khóa khách hàng" : "Đã mở khóa khách hàng",
        action === "block" ? "warning" : "success",
      );
    },
    onError: () => showToast("Không thể cập nhật trạng thái khách hàng", "error"),
  });

  const hostMutation = useMutation({
    mutationFn: (action: "suspend" | "activate") =>
      changeHostStatus(customerId!, action),
    onSuccess: (updated, action) => {
      setCustomer(updated);
      queryClient.invalidateQueries({ queryKey: ["customers"] });
      showToast(
        action === "suspend" ? "Đã tạm ngưng host" : "Đã kích hoạt lại host",
        action === "suspend" ? "warning" : "success",
      );
    },
    onError: () => showToast("Không thể cập nhật trạng thái host", "error"),
  });

  useEffect(() => {
    setCustomer(customerQuery.data ?? null);
  }, [customerQuery.data]);

  useEffect(() => {
    if (!open) return;
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  function handleBackdropClick(event: React.MouseEvent<HTMLDivElement>) {
    if (event.target === event.currentTarget) onClose();
  }

  const avatarColor = customer ? getAvatarColor(customer.id) : "var(--accent)";
  const aggregateMeta = customer
    ? kycAggregateStatusMeta[customer.kycAggregateStatus]
    : null;

  return (
    <div className="modal-overlay active" onClick={handleBackdropClick}>
      <div className="modal customer-detail-modal">
        <div className="modal-header">
          <h3 className="modal-title">Chi tiết khách hàng</h3>
          <button type="button" className="modal-close" onClick={onClose} title="Đóng">
            <X size={16} />
          </button>
        </div>

        {customerQuery.isLoading ? (
          <div className="modal-body customer-detail-state">
            Đang tải chi tiết khách hàng...
          </div>
        ) : null}

        {customerQuery.isError ? (
          <div className="modal-body customer-detail-state">
            <div>
              <div className="customer-state-title">Không tải được chi tiết</div>
              <div className="customer-state-description">
                Khách hàng có thể không tồn tại.
              </div>
            </div>
            <button
              className="btn btn-secondary btn-sm"
              type="button"
              onClick={() => customerQuery.refetch()}
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {customer ? (
          <>
            <div className="customer-detail-hero">
              <div
                className="avatar avatar-lg"
                style={{ background: `${avatarColor}26`, color: avatarColor }}
              >
                {getInitials(customer.fullName)}
              </div>
              <div className="customer-detail-hero-main">
                <div className="customer-detail-hero-title">
                  <h3>{customer.fullName}</h3>
                  <CustomerStatusBadge status={customer.status} />
                  {aggregateMeta ? (
                    <span className={`badge ${aggregateMeta.className}`}>
                      KYC: {aggregateMeta.label}
                    </span>
                  ) : null}
                </div>
                <div className="customer-detail-hero-meta">
                  <span>{customer.phone ?? "—"}</span>
                  <span>·</span>
                  <span>{customer.email ?? "—"}</span>
                </div>
                <CustomerRoleBadges roles={customer.roles} />
              </div>
            </div>

            <div className="customer-detail-body">
              <Section
                title="Thông tin cá nhân"
                action={
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() => setEditOpen(true)}
                  >
                    <Pencil size={14} />
                    Sửa thông tin
                  </button>
                }
              >
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

              <HostSection customer={customer} />
              <KycSection
                customer={customer}
                onOpenReview={(kycId) => setReviewKycId(kycId)}
              />
              <AddressSection customer={customer} />

              <Section title="Hoạt động">
                <Field label="Số booking" value={customer.activity.bookingCount} />
                <Field label="Số xe" value={customer.activity.vehicleCount} />
                <Field
                  label="Tổng doanh thu"
                  value={formatVND(customer.activity.totalRevenue)}
                />
              </Section>
            </div>

            <div className="modal-footer customer-detail-footer">
              <button
                className={`btn btn-sm ${
                  customer.status === "BLOCKED" ? "btn-secondary" : "btn-danger"
                }`}
                type="button"
                disabled={statusMutation.isPending}
                onClick={() =>
                  statusMutation.mutate(
                    customer.status === "BLOCKED" ? "unblock" : "block",
                  )
                }
              >
                <Ban size={14} />
                {customer.status === "BLOCKED" ? "Mở khóa" : "Khóa"}
              </button>

              {customer.hostProfile ? (
                <button
                  className="btn btn-sm btn-secondary"
                  type="button"
                  disabled={hostMutation.isPending}
                  onClick={() =>
                    hostMutation.mutate(
                      customer.hostProfile!.status === "SUSPENDED"
                        ? "activate"
                        : "suspend",
                    )
                  }
                >
                  <PauseCircle size={14} />
                  {customer.hostProfile.status === "SUSPENDED"
                    ? "Kích hoạt host"
                    : "Tạm ngưng host"}
                </button>
              ) : null}

              <button className="btn btn-secondary btn-sm" type="button" onClick={onClose}>
                Đóng
              </button>
            </div>
          </>
        ) : null}
      </div>

      <EditCustomerModal
        open={editOpen}
        customer={customer}
        onClose={() => setEditOpen(false)}
        onUpdated={(next) => setCustomer(next)}
      />

      <KycDocumentReviewModal
        open={reviewKycId !== null}
        customerId={customer?.id ?? null}
        customerName={customer?.fullName ?? ""}
        kycId={reviewKycId}
        onClose={() => setReviewKycId(null)}
        onUpdated={(next) => setCustomer(next)}
      />
    </div>
  );
}

function HostSection({ customer }: { customer: AdminCustomer }) {
  const host = customer.hostProfile;
  if (!host) {
    return (
      <Section title="Hồ sơ Host">
        <EmptyHint message="Khách hàng chưa có hồ sơ chủ xe." />
      </Section>
    );
  }
  const meta = hostStatusMeta[host.status];
  return (
    <Section title="Hồ sơ Host">
      <Field label="Mã host" value={host.hostCode} />
      <Field label="Tên hiển thị" value={host.displayName} />
      <Field
        label="Đánh giá"
        value={`${host.ratingAverage.toFixed(1)} ★ (${host.ratingCount})`}
      />
      {host.bio ? <Field label="Giới thiệu" value={host.bio} /> : null}
      <div className="customer-detail-field">
        <label>Trạng thái host</label>
        <span>
          <span className={`badge ${meta.className}`}>{meta.label}</span>
        </span>
      </div>
      <Field label="Trở thành host" value={formatFullDate(host.joinedAt)} />
    </Section>
  );
}

function KycSection({
  customer,
  onOpenReview,
}: {
  customer: AdminCustomer;
  onOpenReview: (kycId: string) => void;
}) {
  if (customer.kycs.length === 0) {
    return (
      <Section title="KYC">
        <EmptyHint message="Chưa nộp hồ sơ định danh." />
      </Section>
    );
  }
  return (
    <Section title={`KYC (${customer.kycs.length})`}>
      <div className="customer-kyc-list">
        {customer.kycs.map((kyc) => (
          <KycItem key={kyc.id} kyc={kyc} onOpenReview={onOpenReview} />
        ))}
      </div>
    </Section>
  );
}

function KycItem({
  kyc,
  onOpenReview,
}: {
  kyc: CustomerKycSummary;
  onOpenReview: (kycId: string) => void;
}) {
  return (
    <div className="customer-kyc-item">
      <div className="customer-kyc-item-head">
        <div className="customer-kyc-item-id">
          <span className="customer-kyc-code">{kyc.kycCode}</span>
          <CustomerKycBadge status={kyc.status} />
        </div>
        <button
          type="button"
          className="btn btn-secondary btn-sm customer-kyc-review-btn"
          onClick={() => onOpenReview(kyc.id)}
          title="Xem giấy tờ và duyệt / từ chối"
        >
          <FileSearch size={14} />
          Xem giấy tờ
        </button>
      </div>
      <div className="customer-kyc-item-grid">
        <Field label="Loại giấy tờ" value={documentTypeLabel[kyc.documentType]} />
        <Field label="Tên trên giấy tờ" value={kyc.legalName} />
        <Field label="Số giấy tờ" value={maskDocumentNumber(kyc.documentNumber)} />
        <Field label="Ngày nộp" value={formatDateTime(kyc.submittedAt)} />
        <Field label="Duyệt lúc" value={formatDateTime(kyc.reviewedAt)} />
        {kyc.rejectionReason ? (
          <Field label="Lý do từ chối" value={kyc.rejectionReason} />
        ) : null}
      </div>
    </div>
  );
}

function AddressSection({ customer }: { customer: AdminCustomer }) {
  return (
    <Section title={`Địa chỉ (${customer.addresses.length})`}>
      {customer.addresses.length === 0 ? (
        <EmptyHint message="Chưa có địa chỉ." />
      ) : (
        <div className="customer-address-list">
          {customer.addresses.map((address) => (
            <div className="customer-address-item" key={address.id}>
              <div className="customer-address-head">
                <span className="customer-address-label">{address.label}</span>
                {address.isDefault ? (
                  <span className="badge badge-active">Mặc định</span>
                ) : null}
              </div>
              <div className="customer-address-line">{address.line1}</div>
              <div className="customer-address-meta">
                {[address.communeName, address.provinceName]
                  .filter(Boolean)
                  .join(", ") || "—"}
              </div>
              {address.legacyDistrict ? (
                <div className="customer-address-legacy">
                  Quận / Huyện (cũ): {address.legacyDistrict}
                </div>
              ) : null}
            </div>
          ))}
        </div>
      )}
    </Section>
  );
}

function Section({
  title,
  action,
  children,
}: {
  title: string;
  action?: ReactNode;
  children: ReactNode;
}) {
  return (
    <div className="customer-detail-section">
      <div className="customer-detail-section-head">
        <div className="customer-detail-section-title">{title}</div>
        {action ? <div className="customer-detail-section-action">{action}</div> : null}
      </div>
      <div className="customer-detail-grid">{children}</div>
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
    <div className="customer-detail-field">
      <label>{label}</label>
      <span className={isEmpty ? "muted" : ""}>{isEmpty ? "—" : String(value)}</span>
    </div>
  );
}

function EmptyHint({ message }: { message: string }) {
  return <div className="customer-detail-empty-hint">{message}</div>;
}
