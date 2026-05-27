import { customerStatusMeta } from "../constants";
import type { CustomerStatus } from "../types";

export function CustomerStatusBadge({ status }: { status: CustomerStatus }) {
  const meta = customerStatusMeta[status];
  return <span className={`badge ${meta.className}`}>{meta.label}</span>;
}
