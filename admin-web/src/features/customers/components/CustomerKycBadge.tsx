import { kycStatusMeta } from "../constants";
import type { KycStatus } from "../types";

export function CustomerKycBadge({ status }: { status?: KycStatus | null }) {
  if (!status) {
    return <span className="badge badge-draft">Chưa có</span>;
  }
  const meta = kycStatusMeta[status];
  return <span className={`badge ${meta.className}`}>{meta.label}</span>;
}
