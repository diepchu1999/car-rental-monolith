import type { ReactNode } from "react";
import { CarFront, Eye, Fuel, Gauge, UsersRound } from "lucide-react";
import type { Vehicle } from "../types";
import {
  vehicleFuelTypeLabel,
  vehicleSourceClassName,
  vehicleSourceLabel,
  vehicleStatusClassName,
  vehicleStatusLabel,
  vehicleTransmissionLabel,
} from "../constants";

type VehicleViewMode = "grid" | "list";

type VehicleCardProps = {
  vehicle: Vehicle;
  viewMode?: VehicleViewMode;
  onViewDetail: (vehicleId: string) => void;
};

export function VehicleCard({
  vehicle,
  viewMode = "grid",
  onViewDetail,
}: VehicleCardProps) {
  const vehicleName = [vehicle.brand, vehicle.model, vehicle.version]
    .filter(Boolean)
    .join(" ");

  function handleOpenDetail() {
    onViewDetail(vehicle.id);
  }

  return (
    <article
      className={`vehicle-card vehicle-card-${viewMode} animate-fade-up`}
      title="Click đúp để xem chi tiết"
      onDoubleClick={handleOpenDetail}
    >
      <div className="vehicle-img">
        <CarFront size={72} strokeWidth={1.2} />
        <span className={`vehicle-source tag ${vehicleSourceClassName[vehicle.source]}`}>
          {vehicleSourceLabel[vehicle.source] ?? vehicle.source}
        </span>
        <span className={`vehicle-status-dot badge ${vehicleStatusClassName[vehicle.status]}`}>
          {vehicleStatusLabel[vehicle.status] ?? vehicle.status}
        </span>
      </div>

      <div className="vehicle-info">
        <h4>{vehicleName}</h4>
        <div className="vehicle-meta">
          {vehicle.licensePlate} · {vehicle.manufactureYear}
        </div>

        <div className="vehicle-specs">
          <VehicleSpec icon={<UsersRound size={14} />} label={`${vehicle.seats} chỗ`} />
          <VehicleSpec
            icon={<Gauge size={14} />}
            label={vehicleTransmissionLabel[vehicle.transmission] ?? vehicle.transmission}
          />
          <VehicleSpec
            icon={<Fuel size={14} />}
            label={vehicleFuelTypeLabel[vehicle.fuelType] ?? vehicle.fuelType}
          />
        </div>

        <div className="vehicle-card-actions">
          <button
            className="btn btn-secondary btn-sm"
            type="button"
            onClick={handleOpenDetail}
          >
            <Eye size={14} />
            Xem chi tiết
          </button>
        </div>
      </div>
    </article>
  );
}

type VehicleSpecProps = {
  icon: ReactNode;
  label: string;
};

function VehicleSpec({ icon, label }: VehicleSpecProps) {
  return (
    <span className="vehicle-spec">
      {icon}
      {label}
    </span>
  );
}
