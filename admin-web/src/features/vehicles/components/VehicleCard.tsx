import type { ReactNode } from "react";
import {
  Building2,
  CarFront,
  Eye,
  Fuel,
  Gauge,
  MapPin,
  Receipt,
  User,
  UsersRound,
} from "lucide-react";
import type { AdminVehicleListItem } from "../types";
import {
  vehicleFuelTypeLabel,
  vehicleSourceClassName,
  vehicleSourceLabel,
  vehicleStatusClassName,
  vehicleStatusLabel,
  vehicleTransmissionLabel,
} from "../constants";
import { resolveVehicleImageUrl } from "../api/vehicleAPI";
import { useVehicleActions } from "../hooks/useVehicleActions";
import { VehicleActionMenu } from "./VehicleActionMenu";

type VehicleViewMode = "grid" | "list";

type VehicleCardProps = {
  vehicle: AdminVehicleListItem;
  viewMode?: VehicleViewMode;
  onViewDetail: (vehicleId: string) => void;
};

const currencyFormatter = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

export function VehicleCard({
  vehicle,
  viewMode = "grid",
  onViewDetail,
}: VehicleCardProps) {
  const vehicleName = [vehicle.brand, vehicle.model, vehicle.version]
    .filter(Boolean)
    .join(" ");

  const isHostOwned = vehicle.source === "HOST_OWNED";
  const ownerLine = isHostOwned
    ? vehicle.ownerCustomerName ?? vehicle.hostCode ?? "Chủ xe chưa rõ"
    : vehicle.assetCode
      ? `${vehicle.assetCode}${vehicle.branchName ? ` · ${vehicle.branchName}` : ""}`
      : vehicle.branchName ?? "AresDrive fleet";
  const ownerIcon = isHostOwned ? <User size={13} /> : <Building2 size={13} />;

  const location = [vehicle.city, vehicle.district].filter(Boolean).join(", ");
  const dailyRate = vehicle.baseDailyRate
    ? currencyFormatter.format(vehicle.baseDailyRate)
    : null;

  const actions = useVehicleActions(vehicle.id);

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
        {vehicle.coverImageUrl ? (
          <img src={resolveVehicleImageUrl(vehicle.coverImageUrl)} alt={vehicleName} loading="lazy" />
        ) : (
          <CarFront size={72} strokeWidth={1.2} />
        )}
        <span
          className={`vehicle-source tag ${vehicleSourceClassName[vehicle.source]}`}
        >
          {vehicleSourceLabel[vehicle.source] ?? vehicle.source}
        </span>
        <span
          className={`vehicle-status-dot badge ${vehicleStatusClassName[vehicle.status]}`}
        >
          {vehicleStatusLabel[vehicle.status] ?? vehicle.status}
        </span>
      </div>

      <div className="vehicle-info">
        <h4>{vehicleName}</h4>
        <div className="vehicle-meta">
          {vehicle.licensePlate} · {vehicle.manufactureYear}
        </div>

        <div className="vehicle-owner-line">
          {ownerIcon}
          <span>{ownerLine}</span>
        </div>

        {location ? (
          <div className="vehicle-owner-line vehicle-owner-line-muted">
            <MapPin size={13} />
            <span>{location}</span>
          </div>
        ) : null}

        <div className="vehicle-specs">
          <VehicleSpec
            icon={<UsersRound size={14} />}
            label={`${vehicle.seats} chỗ`}
          />
          <VehicleSpec
            icon={<Gauge size={14} />}
            label={
              vehicleTransmissionLabel[vehicle.transmission] ??
              vehicle.transmission
            }
          />
          <VehicleSpec
            icon={<Fuel size={14} />}
            label={vehicleFuelTypeLabel[vehicle.fuelType] ?? vehicle.fuelType}
          />
        </div>

        <div className="vehicle-stats-row">
          {dailyRate ? (
            <div className="vehicle-stat-pill">
              <Receipt size={12} />
              <span>{dailyRate}/ngày</span>
            </div>
          ) : null}
          <div className="vehicle-stat-pill vehicle-stat-pill-muted">
            {vehicle.bookingCount} booking
          </div>
          {vehicle.featureCount > 0 ? (
            <div className="vehicle-stat-pill vehicle-stat-pill-muted">
              {vehicle.featureCount} tính năng
            </div>
          ) : null}
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
          <VehicleActionMenu
            status={vehicle.status}
            hasListing={vehicle.listingStatus != null}
            busy={actions.isPending}
            onAction={actions.dispatch}
          />
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
