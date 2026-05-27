import { useEffect, useMemo, useState } from "react";
import { CheckCircle2, ImageOff, ShieldX, X } from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  approveCustomerKyc,
  getCustomerKycDetail,
  rejectCustomerKyc,
} from "../api/kycAPI";
import { documentTypeLabel, documentSideLabel } from "../constants";
import { showToast } from "../notify";
import type {
  AdminCustomer,
  CustomerKycDetail,
  KycDocument,
  KycStatus,
} from "../types";
import { formatDateTime, formatFullDate } from "../utils";
import { CustomerKycBadge } from "./CustomerKycBadge";
import { KycDocumentThumbnail } from "./KycDocumentThumbnail";
import {
  INITIAL_CHECKLIST,
  KycReviewChecklist,
  type ChecklistState,
} from "./KycReviewChecklist";

type Props = {
  open: boolean;
  customerId: string | null;
  customerName: string;
  kycId: string | null;
  onClose: () => void;
  // Cha cần biết customer mới sau khi approve/reject để đồng bộ detail UI.
  onUpdated?: (next: AdminCustomer) => void;
};

const FINAL_STATUSES: KycStatus[] = ["APPROVED", "REJECTED"];

export function KycDocumentReviewModal({
  open,
  customerId,
  customerName,
  kycId,
  onClose,
  onUpdated,
}: Props) {
  const queryClient = useQueryClient();
  const [activeDocId, setActiveDocId] = useState<string | null>(null);
  const [checklist, setChecklist] = useState<ChecklistState>(INITIAL_CHECKLIST);
  const [rejectOpen, setRejectOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState("");
  const [imageBroken, setImageBroken] = useState(false);

  const kycQuery = useQuery({
    queryKey: ["customer-kyc", customerId, kycId],
    queryFn: () => getCustomerKycDetail(customerId!, kycId!),
    enabled: open && customerId !== null && kycId !== null,
  });

  const detail = kycQuery.data ?? null;
  const orderedDocs = useMemo(
    () => orderDocuments(detail?.documents ?? []),
    [detail],
  );

  // Reset local UI khi mở popup cho KYC mới hoặc khi data về.
  useEffect(() => {
    if (!open) return;
    setActiveDocId(orderedDocs[0]?.id ?? null);
    setChecklist(INITIAL_CHECKLIST);
    setRejectOpen(false);
    setRejectReason("");
    setImageBroken(false);
  }, [open, orderedDocs]);

  useEffect(() => {
    if (!open) return;
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [open, onClose]);

  const approveMutation = useMutation({
    mutationFn: () => approveCustomerKyc(customerId!, kycId!),
    onSuccess: (updated) => {
      showToast("Đã duyệt KYC", "success");
      invalidateAfterReview(updated);
    },
    onError: () => showToast("Không thể duyệt KYC", "error"),
  });

  const rejectMutation = useMutation({
    mutationFn: (reason: string) =>
      rejectCustomerKyc(customerId!, kycId!, { rejectionReason: reason }),
    onSuccess: (updated) => {
      showToast("Đã từ chối KYC", "warning");
      invalidateAfterReview(updated);
    },
    onError: () => showToast("Không thể từ chối KYC", "error"),
  });

  function invalidateAfterReview(updated: AdminCustomer) {
    queryClient.invalidateQueries({ queryKey: ["customers"] });
    queryClient.invalidateQueries({
      queryKey: ["customers", "detail", customerId],
    });
    queryClient.invalidateQueries({
      queryKey: ["customer-kyc", customerId, kycId],
    });
    onUpdated?.(updated);
    onClose();
  }

  if (!open) return null;

  const isPending = approveMutation.isPending || rejectMutation.isPending;
  const isFinal = detail ? FINAL_STATUSES.includes(detail.status) : false;
  const canApprove = !!detail && !isFinal && !isPending;
  const canReject = !!detail && !isFinal && !isPending;
  const active = orderedDocs.find((d) => d.id === activeDocId) ?? orderedDocs[0];

  function handleBackdropClick(event: React.MouseEvent<HTMLDivElement>) {
    if (event.target === event.currentTarget) onClose();
  }

  function handleApprove() {
    if (!canApprove) return;
    approveMutation.mutate();
  }

  function handleSubmitReject() {
    const reason = rejectReason.trim();
    if (!reason) {
      showToast("Vui lòng nhập lý do từ chối", "warning");
      return;
    }
    rejectMutation.mutate(reason);
  }

  return (
    <div
      className="modal-overlay active kyc-modal-overlay"
      onClick={handleBackdropClick}
    >
      <div className="modal kyc-review-modal">
        <div className="modal-header kyc-modal-header">
          <div className="kyc-modal-header-main">
            <h3 className="modal-title">Hồ sơ KYC</h3>
            <div className="kyc-modal-subline">
              <span className="kyc-modal-customer">{customerName}</span>
              {detail ? <CustomerKycBadge status={detail.status} /> : null}
              {detail ? (
                <span className="kyc-modal-doctype">
                  {detail.kycCode} · {documentTypeLabel[detail.documentType]}
                </span>
              ) : null}
            </div>
          </div>
          <button type="button" className="modal-close" onClick={onClose} title="Đóng">
            <X size={16} />
          </button>
        </div>

        {kycQuery.isLoading ? (
          <div className="modal-body customer-detail-state">
            Đang tải hồ sơ KYC...
          </div>
        ) : null}

        {kycQuery.isError ? (
          <div className="modal-body customer-detail-state">
            <div>
              <div className="customer-state-title">Không tải được hồ sơ KYC</div>
              <div className="customer-state-description">
                Hồ sơ có thể đã bị xoá hoặc không thuộc khách hàng này.
              </div>
            </div>
            <button
              className="btn btn-secondary btn-sm"
              type="button"
              onClick={() => kycQuery.refetch()}
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {detail ? (
          <div className="kyc-modal-body">
            <div className="kyc-viewer">
              <div className="kyc-viewer-stage">
                {active && !imageBroken ? (
                  <img
                    key={active.id}
                    src={active.fileUrl}
                    alt={documentSideLabel[active.documentSide]}
                    onError={() => setImageBroken(true)}
                  />
                ) : (
                  <div className="kyc-viewer-fallback">
                    <ImageOff size={28} />
                    <span>
                      {orderedDocs.length === 0
                        ? "Chưa có ảnh giấy tờ"
                        : "Không tải được ảnh giấy tờ"}
                    </span>
                  </div>
                )}
              </div>
              <div className="kyc-thumb-row">
                {orderedDocs.map((doc) => (
                  <KycDocumentThumbnail
                    key={doc.id}
                    side={doc.documentSide}
                    fileUrl={doc.fileUrl}
                    selected={doc.id === active?.id}
                    onSelect={() => {
                      setActiveDocId(doc.id);
                      setImageBroken(false);
                    }}
                  />
                ))}
                {orderedDocs.length === 0 ? (
                  <div className="kyc-thumb-empty">Chưa có ảnh giấy tờ</div>
                ) : null}
              </div>
            </div>

            <aside className="kyc-side">
              <section className="kyc-side-section">
                <div className="kyc-side-title">Thông tin giấy tờ</div>
                <dl className="kyc-meta">
                  <MetaRow label="Mã KYC" value={detail.kycCode} mono />
                  <MetaRow label="Tên trên giấy tờ" value={detail.legalName} />
                  <MetaRow
                    label="Số giấy tờ"
                    value={detail.documentNumber}
                    mono
                  />
                  <MetaRow
                    label="Ngày cấp"
                    value={formatFullDate(detail.issuedDate)}
                  />
                  <MetaRow label="Nơi cấp" value={detail.issuedPlace ?? "—"} />
                  <MetaRow
                    label="Ngày nộp"
                    value={formatDateTime(detail.submittedAt)}
                  />
                  <MetaRow label="Reviewed by" value={detail.reviewedBy ?? "—"} />
                  <MetaRow
                    label="Reviewed at"
                    value={formatDateTime(detail.reviewedAt)}
                  />
                  {detail.rejectionReason ? (
                    <MetaRow
                      label="Lý do từ chối"
                      value={detail.rejectionReason}
                      danger
                    />
                  ) : null}
                </dl>
              </section>

              <section className="kyc-side-section">
                <div className="kyc-side-title">Checklist xác minh</div>
                <KycReviewChecklist
                  state={checklist}
                  onChange={setChecklist}
                  disabled={isFinal}
                />
              </section>
            </aside>
          </div>
        ) : null}

        {rejectOpen ? (
          <div className="kyc-reject-panel">
            <label htmlFor="kyc-reject-reason">Lý do từ chối</label>
            <textarea
              id="kyc-reject-reason"
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              rows={2}
              placeholder="Mô tả lý do rõ ràng để khách hàng nộp lại đúng giấy tờ..."
              autoFocus
            />
            <div className="kyc-reject-actions">
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => {
                  setRejectOpen(false);
                  setRejectReason("");
                }}
              >
                Hủy
              </button>
              <button
                type="button"
                className="btn btn-danger btn-sm"
                onClick={handleSubmitReject}
                disabled={isPending}
              >
                <ShieldX size={14} />
                Xác nhận từ chối
              </button>
            </div>
          </div>
        ) : null}

        <div className="modal-footer kyc-modal-footer">
          <button
            type="button"
            className="btn btn-sm btn-primary kyc-btn-approve"
            disabled={!canApprove}
            onClick={handleApprove}
          >
            <CheckCircle2 size={14} />
            Duyệt KYC
          </button>
          <button
            type="button"
            className="btn btn-sm btn-danger"
            disabled={!canReject}
            onClick={() => setRejectOpen((v) => !v)}
          >
            <ShieldX size={14} />
            Từ chối KYC
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={onClose}>
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
}

const SIDE_ORDER: Record<KycDocument["documentSide"], number> = {
  FRONT: 0,
  BACK: 1,
  SELFIE: 2,
  OTHER: 3,
};

function orderDocuments(docs: KycDocument[]): KycDocument[] {
  return [...docs].sort(
    (a, b) => SIDE_ORDER[a.documentSide] - SIDE_ORDER[b.documentSide],
  );
}

function MetaRow({
  label,
  value,
  mono,
  danger,
}: {
  label: string;
  value: string | number | null | undefined;
  mono?: boolean;
  danger?: boolean;
}) {
  const isEmpty = value === null || value === undefined || value === "";
  const classes = [
    "kyc-meta-value",
    mono ? "is-mono" : "",
    danger ? "is-danger" : "",
    isEmpty ? "is-empty" : "",
  ]
    .filter(Boolean)
    .join(" ");
  return (
    <>
      <dt>{label}</dt>
      <dd className={classes}>{isEmpty ? "—" : String(value)}</dd>
    </>
  );
}

// Export type cho cha có thể track decision callback đồng nhất nếu cần.
export type KycReviewProps = Props;
