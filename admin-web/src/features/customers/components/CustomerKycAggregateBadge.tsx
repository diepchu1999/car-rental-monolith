import { kycAggregateStatusMeta } from "../constants";
import type { KycAggregateStatus } from "../types";

// Badge cho aggregate KYC status — dùng ở list page + header customer detail.
// Tách khỏi CustomerKycBadge (per-KYC) để 2 enum/label set không lẫn vào nhau.
export function CustomerKycAggregateBadge({
  status,
}: {
  status: KycAggregateStatus;
}) {
  const meta = kycAggregateStatusMeta[status];
  return <span className={`badge ${meta.className}`}>{meta.label}</span>;
}
