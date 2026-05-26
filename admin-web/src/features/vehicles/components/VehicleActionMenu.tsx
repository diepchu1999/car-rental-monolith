import { useEffect, useRef, useState } from "react";
import { MoreVertical } from "lucide-react";
import type {
  ListingStatusAction,
  VehicleStatusAction,
} from "../api/vehicleAPI";
import type { VehicleStatus } from "../types";

export type VehicleAction =
  | { kind: "vehicle"; action: VehicleStatusAction }
  | { kind: "listing"; action: ListingStatusAction };

type VehicleActionMenuProps = {
  status: VehicleStatus;
  hasListing: boolean;
  busy?: boolean;
  onAction: (action: VehicleAction) => void;
};

const VEHICLE_ACTION_LABELS: Record<VehicleStatusAction, string> = {
  activate: "Kích hoạt xe",
  suspend: "Khóa xe",
  deactivate: "Ngưng hoạt động",
};

const LISTING_ACTION_LABELS: Record<ListingStatusAction, string> = {
  publish: "Đăng listing",
  pause: "Tạm dừng listing",
  archive: "Lưu trữ listing",
  draft: "Đưa về nháp",
};

const DANGEROUS_ACTIONS = new Set<VehicleStatusAction | ListingStatusAction>([
  "suspend",
  "deactivate",
  "archive",
]);

export function VehicleActionMenu({
  status,
  hasListing,
  busy = false,
  onAction,
}: VehicleActionMenuProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    function onDocClick(event: MouseEvent) {
      if (!ref.current?.contains(event.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", onDocClick);
    return () => document.removeEventListener("mousedown", onDocClick);
  }, [open]);

  const vehicleActions = pickVehicleActions(status);

  function handleSelect(action: VehicleAction) {
    setOpen(false);
    const label =
      action.kind === "vehicle"
        ? VEHICLE_ACTION_LABELS[action.action]
        : LISTING_ACTION_LABELS[action.action];
    if (DANGEROUS_ACTIONS.has(action.action)) {
      if (!confirm(`Bạn chắc chắn muốn "${label}"?`)) return;
    }
    onAction(action);
  }

  return (
    <div className="vehicle-action-menu" ref={ref}>
      <button
        className="btn btn-secondary btn-sm btn-icon"
        type="button"
        disabled={busy}
        aria-label="Thao tác"
        onClick={(event) => {
          event.stopPropagation();
          setOpen((v) => !v);
        }}
      >
        <MoreVertical size={14} />
      </button>
      {open ? (
        <div className="vehicle-action-popover" role="menu">
          <div className="vehicle-action-section">Trạng thái xe</div>
          {vehicleActions.length === 0 ? (
            <div className="vehicle-action-empty">
              Không có thao tác hợp lệ.
            </div>
          ) : (
            vehicleActions.map((action) => (
              <button
                key={action}
                className="vehicle-action-item"
                type="button"
                onClick={() => handleSelect({ kind: "vehicle", action })}
              >
                {VEHICLE_ACTION_LABELS[action]}
              </button>
            ))
          )}

          {hasListing ? (
            <>
              <div className="vehicle-action-section">Listing</div>
              {(Object.keys(LISTING_ACTION_LABELS) as ListingStatusAction[]).map(
                (action) => (
                  <button
                    key={action}
                    className="vehicle-action-item"
                    type="button"
                    onClick={() => handleSelect({ kind: "listing", action })}
                  >
                    {LISTING_ACTION_LABELS[action]}
                  </button>
                ),
              )}
            </>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}

function pickVehicleActions(status: VehicleStatus): VehicleStatusAction[] {
  switch (status) {
    case "ACTIVE":
      return ["suspend", "deactivate"];
    case "INACTIVE":
      return ["activate", "suspend"];
    case "SUSPENDED":
      return ["activate", "deactivate"];
    case "DRAFT":
    case "PENDING_REVIEW":
      return ["activate", "suspend"];
    default:
      return [];
  }
}
