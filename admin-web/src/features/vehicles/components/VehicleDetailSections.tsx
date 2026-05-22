import { type ReactNode } from "react";
import type {
  AdminVehicleDetail,
  AdminVehicleDetailAvailabilityBlock,
  AdminVehicleDetailFeature,
  AdminVehicleDetailImage,
  AdminVehicleDetailRecentBooking,
} from "../types";
import {
  vehicleFuelTypeLabel,
  vehicleSourceLabel,
  vehicleStatusLabel,
  vehicleTransmissionLabel,
} from "../constants";
import { resolveVehicleImageUrl } from "../api/vehicleAPI";

type VehicleDetailSectionsProps = {
  vehicle: AdminVehicleDetail;
};

const currencyFormatter = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

const dateTimeFormatter = new Intl.DateTimeFormat("vi-VN", {
  year: "numeric",
  month: "2-digit",
  day: "2-digit",
  hour: "2-digit",
  minute: "2-digit",
});

export function VehicleDetailSections({ vehicle }: VehicleDetailSectionsProps) {
  return (
    <div className="vehicle-detail-body">
      <Section title="Tổng quan">
        <Field label="Hãng xe" value={vehicle.brand} />
        <Field label="Model" value={vehicle.model} />
        <Field label="Phiên bản" value={vehicle.version} />
        <Field label="Biển số" value={vehicle.licensePlate} />
        <Field label="Số chỗ" value={`${vehicle.seats} chỗ`} />
        <Field label="Năm SX" value={vehicle.manufactureYear} />
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
          value={
            vehicleTransmissionLabel[vehicle.transmission] ??
            vehicle.transmission
          }
        />
        <Field
          label="Nhiên liệu"
          value={vehicleFuelTypeLabel[vehicle.fuelType] ?? vehicle.fuelType}
        />
      </Section>

      <OwnerFleetSection vehicle={vehicle} />
      <ListingSection vehicle={vehicle} />
      <ImagesSection images={vehicle.images} />
      <FeaturesSection features={vehicle.features} />
      <PricingSection vehicle={vehicle} />
      <AvailabilitySection blocks={vehicle.upcomingAvailabilityBlocks} />
      <RecentBookingsSection bookings={vehicle.recentBookings} />

      <Section title="Hệ thống">
        <Field label="ID" value={vehicle.id} />
        <Field label="Tạo lúc" value={formatDateTime(vehicle.createdAt)} />
        <Field label="Cập nhật lúc" value={formatDateTime(vehicle.updatedAt)} />
      </Section>
    </div>
  );
}

function OwnerFleetSection({ vehicle }: { vehicle: AdminVehicleDetail }) {
  const isHostOwned = vehicle.source === "HOST_OWNED";
  const title = isHostOwned ? "Chủ xe (Host)" : "Fleet · Chi nhánh";

  return (
    <Section title={title}>
      {isHostOwned ? (
        <>
          <Field label="ID khách hàng" value={vehicle.owner?.customerId} />
          <Field label="Họ tên" value={vehicle.owner?.fullName} />
          <Field label="Mã host" value={vehicle.owner?.hostCode} />
          <Field
            label="Tên hiển thị host"
            value={vehicle.owner?.hostDisplayName}
          />
          <Field label="SĐT" value={vehicle.owner?.phone} />
          <Field label="Email" value={vehicle.owner?.email} />
        </>
      ) : (
        <>
          <Field label="ID fleet vehicle" value={vehicle.fleet?.fleetVehicleId} />
          <Field label="Mã tài sản" value={vehicle.fleet?.assetCode} />
          <Field label="Trạng thái fleet" value={vehicle.fleet?.assetStatus} />
          <Field label="Chi nhánh" value={vehicle.fleet?.branchName} />
          <Field label="Thành phố CN" value={vehicle.fleet?.branchCity} />
        </>
      )}
    </Section>
  );
}

function ListingSection({ vehicle }: { vehicle: AdminVehicleDetail }) {
  const listing = vehicle.listing;
  if (!listing) {
    return (
      <Section title="Listing">
        <EmptyHint message="Xe chưa được tạo listing." />
      </Section>
    );
  }

  return (
    <Section title="Listing">
      <Field label="Tiêu đề" value={listing.title} />
      <Field label="Trạng thái" value={listing.status} />
      <Field label="Tỉnh / Thành phố" value={listing.provinceName ?? listing.city} />
      <Field label="Xã / Phường / Đặc khu" value={listing.communeName} />
      {listing.district ? (
        <Field label="Quận / Huyện (cũ)" value={listing.district} />
      ) : null}
      <Field label="Địa chỉ chi tiết" value={listing.pickupAddress} />
      <Field
        label="Giá ngày cơ bản"
        value={formatCurrency(listing.baseDailyRate, listing.currency)}
      />
      <Field
        label="Đặt nhanh"
        value={listing.instantBookingEnabled ? "Có" : "Không"}
      />
      <Field
        label="Giao xe tận nơi"
        value={listing.deliveryEnabled ? "Có" : "Không"}
      />
      <Field label="Phát hành lúc" value={formatDateTime(listing.publishedAt)} />
    </Section>
  );
}

function ImagesSection({ images }: { images: AdminVehicleDetailImage[] }) {
  return (
    <Section title={`Ảnh (${images.length})`}>
      {images.length === 0 ? (
        <EmptyHint message="Chưa có ảnh." />
      ) : (
        <div className="vehicle-image-grid">
          {images.map((image) => (
            <div className="vehicle-image-tile" key={image.id}>
              <img src={resolveVehicleImageUrl(image.fileUrl)} alt={image.id} loading="lazy" />
              {image.cover ? (
                <span className="vehicle-image-tag">Ảnh bìa</span>
              ) : null}
            </div>
          ))}
        </div>
      )}
    </Section>
  );
}

function FeaturesSection({
  features,
}: {
  features: AdminVehicleDetailFeature[];
}) {
  return (
    <Section title={`Tính năng (${features.length})`}>
      {features.length === 0 ? (
        <EmptyHint message="Chưa khai báo tính năng." />
      ) : (
        <div className="vehicle-feature-list">
          {features.map((feature) => (
            <span className="vehicle-feature-tag" key={feature.id}>
              {feature.name}
            </span>
          ))}
        </div>
      )}
    </Section>
  );
}

function PricingSection({ vehicle }: { vehicle: AdminVehicleDetail }) {
  const plan = vehicle.activePricePlan;
  if (!plan) {
    return (
      <Section title="Bảng giá">
        <EmptyHint message="Chưa có price plan đang hoạt động." />
      </Section>
    );
  }

  return (
    <Section title="Bảng giá">
      <Field label="Tên" value={plan.name} />
      <Field
        label="Giá ngày"
        value={formatCurrency(plan.baseDailyRate, plan.currency)}
      />
      <Field
        label="Giá giờ"
        value={formatCurrency(plan.hourlyRate, plan.currency)}
      />
      <Field
        label="Hệ số cuối tuần"
        value={plan.weekendMultiplier ?? undefined}
      />
      <Field
        label="Tiền cọc"
        value={formatCurrency(plan.depositAmount, plan.currency)}
      />
      <Field label="Hiệu lực từ" value={formatDateTime(plan.validFrom)} />
      <Field label="Đến" value={formatDateTime(plan.validTo)} />
    </Section>
  );
}

function AvailabilitySection({
  blocks,
}: {
  blocks: AdminVehicleDetailAvailabilityBlock[];
}) {
  return (
    <Section title={`Lịch bận (${blocks.length})`}>
      {blocks.length === 0 ? (
        <EmptyHint message="Không có khung bận sắp tới." />
      ) : (
        <div className="vehicle-block-table">
          {blocks.map((block) => (
            <div className="vehicle-block-row" key={block.id}>
              <div className="vehicle-block-period">
                <strong>{formatDateTime(block.startAt)}</strong>
                <span>→</span>
                <strong>{formatDateTime(block.endAt)}</strong>
              </div>
              <span className="badge badge-pending">{block.reason}</span>
              {block.note ? (
                <span className="vehicle-block-note">{block.note}</span>
              ) : null}
            </div>
          ))}
        </div>
      )}
    </Section>
  );
}

function RecentBookingsSection({
  bookings,
}: {
  bookings: AdminVehicleDetailRecentBooking[];
}) {
  return (
    <Section title={`Booking gần đây (${bookings.length})`}>
      {bookings.length === 0 ? (
        <EmptyHint message="Chưa phát sinh booking nào." />
      ) : (
        <div className="vehicle-booking-table">
          {bookings.map((booking) => (
            <div className="vehicle-booking-row" key={booking.id}>
              <div className="vehicle-booking-code">{booking.bookingCode}</div>
              <div className="vehicle-booking-period">
                {formatDateTime(booking.startAt)} →{" "}
                {formatDateTime(booking.endAt)}
              </div>
              <div className="vehicle-booking-total">
                {formatCurrency(booking.totalAmount, booking.currency)}
              </div>
              <span className="badge badge-pending">{booking.status}</span>
            </div>
          ))}
        </div>
      )}
    </Section>
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

function EmptyHint({ message }: { message: string }) {
  return <div className="vehicle-detail-empty-hint">{message}</div>;
}

function formatDateTime(value: string | null | undefined): string | null {
  if (!value) return null;
  try {
    return dateTimeFormatter.format(new Date(value));
  } catch {
    return value;
  }
}

function formatCurrency(
  value: number | null | undefined,
  currency: string | null | undefined,
): string | null {
  if (value === null || value === undefined) return null;
  if (currency && currency !== "VND") {
    return `${value.toLocaleString("vi-VN")} ${currency}`;
  }
  return currencyFormatter.format(value);
}
