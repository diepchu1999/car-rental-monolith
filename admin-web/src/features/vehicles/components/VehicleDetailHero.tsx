import { CarFront } from "lucide-react";
import type { Vehicle } from "../types";
import {
  vehicleFuelTypeLabel,
  vehicleSourceClassName,
  vehicleSourceLabel,
  vehicleStatusClassName,
  vehicleStatusLabel,
  vehicleTransmissionLabel,
} from "../constants";

type VehicleDetailHeroProps = {
  vehicle: Vehicle;
};

export function VehicleDetailHero({ vehicle }: VehicleDetailHeroProps) {
  const vehicleName = [vehicle.brand, vehicle.model, vehicle.version]
    .filter(Boolean)
    .join(" ");

  return (
    <div className="vehicle-detail-hero">
      <div className="vehicle-detail-art">
        <CarFront size={112} strokeWidth={1.2} />
      </div>
      <div>
        <div className="vehicle-detail-title-row">
          <div className="vehicle-detail-title">
            <h3>{vehicleName}</h3>
            <p>
              {vehicle.licensePlate} · {vehicle.manufactureYear}
            </p>
          </div>
          <div className="vehicle-detail-badges">
            <span className={`tag ${vehicleSourceClassName[vehicle.source]}`}>
              {vehicleSourceLabel[vehicle.source] ?? vehicle.source}
            </span>
            <span className={`badge ${vehicleStatusClassName[vehicle.status]}`}>
              {vehicleStatusLabel[vehicle.status] ?? vehicle.status}
            </span>
          </div>
        </div>
        <div className="vehicle-kpi-row">
          <KpiCell label="Số chỗ" value={`${vehicle.seats} chỗ`} />
          <KpiCell
            label="Hộp số"
            value={vehicleTransmissionLabel[vehicle.transmission] ?? vehicle.transmission}
          />
          <KpiCell
            label="Nhiên liệu"
            value={vehicleFuelTypeLabel[vehicle.fuelType] ?? vehicle.fuelType}
          />
          <KpiCell label="Năm SX" value={String(vehicle.manufactureYear)} />
        </div>
      </div>
    </div>
  );
}

type KpiCellProps = {
  label: string;
  value: string;
};

function KpiCell({ label, value }: KpiCellProps) {
  return (
    <div className="vehicle-kpi">
      <label>{label}</label>
      <strong>{value}</strong>
    </div>
  );
}
