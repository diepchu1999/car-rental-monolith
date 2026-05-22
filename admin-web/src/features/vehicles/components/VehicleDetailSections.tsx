import type { ReactNode } from "react";
import type { Vehicle } from "../types";
import {
  vehicleFuelTypeLabel,
  vehicleSourceLabel,
  vehicleStatusLabel,
  vehicleTransmissionLabel,
} from "../constants";

type VehicleDetailSectionsProps = {
  vehicle: Vehicle;
};

export function VehicleDetailSections({ vehicle }: VehicleDetailSectionsProps) {
  return (
    <div className="vehicle-detail-body">
      <Section title="Thông tin cơ bản">
        <Field label="Hãng xe" value={vehicle.brand} />
        <Field label="Model" value={vehicle.model} />
        <Field label="Phiên bản" value={vehicle.version} />
        <Field label="Năm sản xuất" value={vehicle.manufactureYear} />
        <Field label="Biển số" value={vehicle.licensePlate} />
        <Field label="Số chỗ" value={`${vehicle.seats} chỗ`} />
      </Section>

      <Section title="Khai thác & trạng thái">
        <Field
          label="Nguồn"
          value={vehicleSourceLabel[vehicle.source] ?? vehicle.source}
        />
        <Field
          label="Trạng thái"
          value={vehicleStatusLabel[vehicle.status] ?? vehicle.status}
        />
        <Field
          label="Hộp số"
          value={vehicleTransmissionLabel[vehicle.transmission] ?? vehicle.transmission}
        />
        <Field
          label="Nhiên liệu"
          value={vehicleFuelTypeLabel[vehicle.fuelType] ?? vehicle.fuelType}
        />
      </Section>

      <Section title="Quan hệ sở hữu">
        <Field label="Chủ xe (host)" value={vehicle.ownerCustomerName} />
        <Field label="ID chủ xe" value={vehicle.ownerCustomerId} />
        <Field label="Mã xe trong fleet" value={vehicle.fleetVehicleId} />
      </Section>

      <Section title="Hệ thống">
        <Field label="ID" value={vehicle.id} />
        <Field label="Tạo lúc" value={formatDateTime(vehicle.createdAt)} />
        <Field label="Cập nhật lúc" value={formatDateTime(vehicle.updatedAt)} />
      </Section>
    </div>
  );
}

type SectionProps = {
  title: string;
  children: ReactNode;
};

function Section({ title, children }: SectionProps) {
  return (
    <div className="vehicle-detail-section">
      <div className="vehicle-detail-section-title">{title}</div>
      <div className="vehicle-detail-grid">{children}</div>
    </div>
  );
}

type FieldProps = {
  label: string;
  value: string | number | null | undefined;
};

function Field({ label, value }: FieldProps) {
  const isEmpty = value === null || value === undefined || value === "";

  return (
    <div className="vehicle-detail-field">
      <label>{label}</label>
      <span className={isEmpty ? "muted" : ""}>
        {isEmpty ? "—" : String(value)}
      </span>
    </div>
  );
}

function formatDateTime(value: string): string {
  try {
    return new Date(value).toLocaleString("vi-VN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch {
    return value;
  }
}
