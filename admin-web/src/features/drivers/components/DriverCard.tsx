import type { ReactNode } from "react";
import { Award, BadgeCheck, CarFront, Eye, Pencil, Star } from "lucide-react";
import type { Driver } from "../types";
import { driverStatusMeta } from "../constants";
import { getDriverInitials } from "../utils";

type DriverCardProps = {
  driver: Driver;
  index: number;
};

export function DriverCard({ driver, index }: DriverCardProps) {
  const status = driverStatusMeta[driver.status];

  return (
    <article
      className="driver-card animate-fade-up"
      style={{ animationDelay: `${index * 0.04}s` }}
    >
      <div className="driver-top">
        <div className="driver-avatar">{getDriverInitials(driver.name)}</div>
        <div className="driver-heading">
          <div className="driver-name">{driver.name}</div>
          <div className="driver-id">
            {driver.id} · {driver.phone}
          </div>
        </div>
        <span className={`badge ${status.className}`}>{status.label}</span>
      </div>

      <div className="driver-meta">
        <DriverMeta icon={<BadgeCheck size={14} />} label="GPLX" value={`Hạng ${driver.license}`} />
        <DriverMeta icon={<Award size={14} />} label="Kinh nghiệm" value={`${driver.exp} năm`} />
        <DriverMeta icon={<CarFront size={14} />} label="Chuyến đi" value={driver.trips} />
        <DriverMeta icon={<Star size={14} />} label="Đánh giá" value={driver.rating.toFixed(1)} />
      </div>

      <div className="driver-license">
        <span>GPLX số</span>
        <strong>{driver.licenseNo}</strong>
      </div>

      <div className="driver-actions">
        <button className="btn btn-secondary btn-sm driver-detail-btn" type="button">
          <Eye size={14} />
          Xem chi tiết
        </button>
        <button className="btn btn-secondary btn-sm btn-icon" type="button" title="Chỉnh sửa">
          <Pencil size={14} />
        </button>
      </div>
    </article>
  );
}

type DriverMetaProps = {
  icon: ReactNode;
  label: string;
  value: string | number;
};

function DriverMeta({ icon, label, value }: DriverMetaProps) {
  return (
    <div className="driver-meta-item">
      <label>
        {icon}
        {label}
      </label>
      <span>{value}</span>
    </div>
  );
}
