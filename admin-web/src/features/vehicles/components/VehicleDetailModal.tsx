import { useEffect } from "react";
import { X } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { getVehicleById } from "../api/vehicleAPI";
import { VehicleDetailHero } from "./VehicleDetailHero";
import { VehicleDetailSections } from "./VehicleDetailSections";

type VehicleDetailModalProps = {
  vehicleId: string | null;
  onClose: () => void;
};

export function VehicleDetailModal({ vehicleId, onClose }: VehicleDetailModalProps) {
  const open = vehicleId !== null;

  const vehicleQuery = useQuery({
    queryKey: ["vehicles", "detail", vehicleId],
    queryFn: () => getVehicleById(vehicleId!),
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

  function handleBackdropClick(event: React.MouseEvent<HTMLDivElement>) {
    if (event.target === event.currentTarget) onClose();
  }

  return (
    <div className="modal-overlay active" onClick={handleBackdropClick}>
      <div className="modal vehicle-detail-modal">
        <div className="modal-header">
          <h3 className="modal-title">Chi tiết xe</h3>
          <button className="modal-close" type="button" onClick={onClose} title="Đóng">
            <X size={16} />
          </button>
        </div>

        {vehicleQuery.isLoading ? (
          <div className="modal-body vehicle-detail-state">Đang tải chi tiết xe...</div>
        ) : null}

        {vehicleQuery.isError ? (
          <div className="modal-body vehicle-detail-state">
            <div>
              <div className="vehicle-detail-state-title">Không tải được chi tiết xe</div>
              <div className="vehicle-detail-state-description">
                Xe có thể không tồn tại hoặc backend đang gặp sự cố.
              </div>
            </div>
            <button
              className="btn btn-secondary btn-sm"
              type="button"
              onClick={() => vehicleQuery.refetch()}
            >
              Thử lại
            </button>
          </div>
        ) : null}

        {vehicleQuery.data ? (
          <>
            <VehicleDetailHero vehicle={vehicleQuery.data} />
            <VehicleDetailSections vehicle={vehicleQuery.data} />
          </>
        ) : null}

        <div className="modal-footer">
          <button className="btn btn-secondary" type="button" onClick={onClose}>
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
}
