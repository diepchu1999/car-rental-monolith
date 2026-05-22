import { useEffect, useRef, useState } from "react";
import { Plus, Trash2, Upload, X } from "lucide-react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  resolveVehicleImageUrl,
  syncFeatures,
  syncImages,
  updateListing,
  uploadVehicleImage,
  upsertPricePlan,
  type UpdateListingPayload,
} from "../api/vehicleAPI";
import { AddressSelect } from "../../locations/components/AddressSelect";
import type { AdminVehicleDetail } from "../types";

type Tab = "listing" | "images" | "features" | "pricing";

type VehicleEditModalProps = {
  vehicle: AdminVehicleDetail | null;
  onClose: () => void;
};

const TABS: Array<{ value: Tab; label: string }> = [
  { value: "listing", label: "Listing" },
  { value: "images", label: "Ảnh" },
  { value: "features", label: "Tính năng" },
  { value: "pricing", label: "Bảng giá" },
];

export function VehicleEditModal({ vehicle, onClose }: VehicleEditModalProps) {
  const [tab, setTab] = useState<Tab>("listing");
  if (!vehicle) return null;

  return (
    <div className="modal-overlay active" onClick={onClose}>
      <div
        className="modal vehicle-edit-modal"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <h3 className="modal-title">Chỉnh sửa xe</h3>
          <button
            type="button"
            className="modal-close"
            onClick={onClose}
            title="Đóng"
          >
            <X size={16} />
          </button>
        </div>

        <div className="vehicle-edit-tabs">
          {TABS.map((t) => (
            <button
              key={t.value}
              type="button"
              className={`vehicle-edit-tab ${tab === t.value ? "active" : ""}`}
              onClick={() => setTab(t.value)}
            >
              {t.label}
            </button>
          ))}
        </div>

        <div className="modal-body vehicle-edit-body">
          {tab === "listing" ? <ListingTab vehicle={vehicle} /> : null}
          {tab === "images" ? <ImagesTab vehicle={vehicle} /> : null}
          {tab === "features" ? <FeaturesTab vehicle={vehicle} /> : null}
          {tab === "pricing" ? <PricingTab vehicle={vehicle} /> : null}
        </div>
      </div>
    </div>
  );
}

function ListingTab({ vehicle }: { vehicle: AdminVehicleDetail }) {
  const queryClient = useQueryClient();
  const initial: UpdateListingPayload = {
    title: vehicle.listing?.title ?? "",
    description: vehicle.listing?.description ?? "",
    provinceCode: vehicle.listing?.provinceCode ?? "",
    communeCode: vehicle.listing?.communeCode ?? "",
    pickupAddress: vehicle.listing?.pickupAddress ?? "",
    baseDailyRate: vehicle.listing?.baseDailyRate ?? null,
    currency: vehicle.listing?.currency ?? "VND",
    instantBookingEnabled: vehicle.listing?.instantBookingEnabled ?? false,
    deliveryEnabled: vehicle.listing?.deliveryEnabled ?? false,
  };
  const [form, setForm] = useState<UpdateListingPayload>(initial);

  useEffect(() => {
    setForm(initial);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [vehicle.id]);

  const mutation = useMutation({
    mutationFn: () => updateListing(vehicle.id, form),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["vehicles"] }),
  });

  function patch(partial: Partial<UpdateListingPayload>) {
    setForm((prev) => ({ ...prev, ...partial }));
  }

  return (
    <form
      className="vehicle-edit-form"
      onSubmit={(event) => {
        event.preventDefault();
        mutation.mutate();
      }}
    >
      <FormField label="Tiêu đề">
        <input
          className="form-input"
          value={form.title ?? ""}
          onChange={(event) => patch({ title: event.target.value })}
        />
      </FormField>
      <FormField label="Mô tả">
        <textarea
          className="form-input"
          rows={3}
          value={form.description ?? ""}
          onChange={(event) => patch({ description: event.target.value })}
        />
      </FormField>
      <AddressSelect
        provinceCode={form.provinceCode ?? ""}
        communeCode={form.communeCode ?? ""}
        onChange={({ provinceCode, communeCode }) =>
          patch({ provinceCode, communeCode })
        }
        required
        legacyDistrict={vehicle.listing?.district}
      />
      <FormField label="Địa chỉ chi tiết (số nhà, đường, thôn/tổ)">
        <input
          className="form-input"
          value={form.pickupAddress ?? ""}
          onChange={(event) => patch({ pickupAddress: event.target.value })}
        />
      </FormField>
      <div className="vehicle-form-grid">
        <FormField label="Giá ngày (VND)">
          <input
            className="form-input"
            type="number"
            min={0}
            step={50_000}
            value={form.baseDailyRate ?? ""}
            onChange={(event) =>
              patch({
                baseDailyRate:
                  event.target.value === "" ? null : Number(event.target.value),
              })
            }
          />
        </FormField>
        <FormField label="Tiền tệ">
          <input
            className="form-input"
            value={form.currency ?? "VND"}
            onChange={(event) => patch({ currency: event.target.value })}
          />
        </FormField>
      </div>
      <div className="vehicle-form-grid">
        <label className="vehicle-checkbox">
          <input
            type="checkbox"
            checked={form.instantBookingEnabled ?? false}
            onChange={(event) =>
              patch({ instantBookingEnabled: event.target.checked })
            }
          />
          Cho phép đặt nhanh
        </label>
        <label className="vehicle-checkbox">
          <input
            type="checkbox"
            checked={form.deliveryEnabled ?? false}
            onChange={(event) =>
              patch({ deliveryEnabled: event.target.checked })
            }
          />
          Giao xe tận nơi
        </label>
      </div>

      <MutationFooter mutation={mutation} label="Lưu listing" />
    </form>
  );
}

function ImagesTab({ vehicle }: { vehicle: AdminVehicleDetail }) {
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [items, setItems] = useState(() =>
    vehicle.images.map((image) => ({
      fileUrl: image.fileUrl,
      sortOrder: image.sortOrder,
      cover: image.cover,
    })),
  );
  const [uploadError, setUploadError] = useState<string | null>(null);

  useEffect(() => {
    setItems(
      vehicle.images.map((image) => ({
        fileUrl: image.fileUrl,
        sortOrder: image.sortOrder,
        cover: image.cover,
      })),
    );
  }, [vehicle.id, vehicle.images]);

  const uploadMutation = useMutation({
    mutationFn: uploadVehicleImage,
    onSuccess: (result) => {
      setItems((prev) => [
        ...prev,
        {
          fileUrl: result.fileName,
          sortOrder: prev.length,
          cover: prev.length === 0,
        },
      ]);
    },
    onError: () => setUploadError("Tải ảnh lên thất bại. Vui lòng thử lại."),
  });

  const mutation = useMutation({
    mutationFn: () => syncImages(vehicle.id, { images: items }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["vehicles"] }),
  });

  function handleFilesSelected(event: React.ChangeEvent<HTMLInputElement>) {
    setUploadError(null);
    Array.from(event.target.files ?? []).forEach((file) =>
      uploadMutation.mutate(file),
    );
    event.target.value = "";
  }

  function removeImage(index: number) {
    setItems((prev) => prev.filter((_, i) => i !== index));
  }

  function patchItem(index: number, partial: Partial<typeof items[number]>) {
    setItems((prev) =>
      prev.map((item, i) => (i === index ? { ...item, ...partial } : item)),
    );
  }

  function setCover(index: number) {
    setItems((prev) =>
      prev.map((item, i) => ({ ...item, cover: i === index })),
    );
  }

  return (
    <form
      className="vehicle-edit-form"
      onSubmit={(event) => {
        event.preventDefault();
        mutation.mutate();
      }}
    >
      <div className="vehicle-edit-image-list">
        {items.map((item, index) => (
          <div className="vehicle-edit-image-row" key={index}>
            <img
              className="vehicle-edit-image-thumb"
              src={resolveVehicleImageUrl(item.fileUrl)}
              alt=""
              loading="lazy"
            />
            <input
              className="form-input vehicle-edit-image-order"
              type="number"
              min={0}
              value={item.sortOrder}
              onChange={(event) =>
                patchItem(index, { sortOrder: Number(event.target.value) })
              }
            />
            <label className="vehicle-checkbox">
              <input
                type="radio"
                name="cover"
                checked={item.cover}
                onChange={() => setCover(index)}
              />
              Bìa
            </label>
            <button
              type="button"
              className="btn btn-secondary btn-icon btn-sm"
              onClick={() => removeImage(index)}
              title="Xóa"
            >
              <Trash2 size={14} />
            </button>
          </div>
        ))}
        {items.length === 0 ? (
          <div className="vehicle-edit-empty">Chưa có ảnh nào.</div>
        ) : null}
      </div>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        multiple
        style={{ display: "none" }}
        onChange={handleFilesSelected}
      />
      <button
        type="button"
        className="btn btn-secondary btn-sm"
        onClick={() => fileInputRef.current?.click()}
        disabled={uploadMutation.isPending}
      >
        {uploadMutation.isPending ? <Upload size={14} /> : <Plus size={14} />}
        {uploadMutation.isPending ? "Đang tải lên..." : "Tải ảnh lên"}
      </button>
      {uploadError ? (
        <div className="vehicle-form-error">{uploadError}</div>
      ) : null}

      <MutationFooter mutation={mutation} label="Lưu ảnh" />
    </form>
  );
}

function FeaturesTab({ vehicle }: { vehicle: AdminVehicleDetail }) {
  const queryClient = useQueryClient();
  const [items, setItems] = useState(() =>
    vehicle.features.map((feature) => ({
      code: feature.code,
      name: feature.name,
    })),
  );

  useEffect(() => {
    setItems(
      vehicle.features.map((feature) => ({
        code: feature.code,
        name: feature.name,
      })),
    );
  }, [vehicle.id, vehicle.features]);

  const mutation = useMutation({
    mutationFn: () => syncFeatures(vehicle.id, { features: items }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["vehicles"] }),
  });

  function addFeature() {
    setItems((prev) => [...prev, { code: "", name: "" }]);
  }

  function removeFeature(index: number) {
    setItems((prev) => prev.filter((_, i) => i !== index));
  }

  function patchItem(index: number, partial: Partial<{ code: string; name: string }>) {
    setItems((prev) =>
      prev.map((item, i) => (i === index ? { ...item, ...partial } : item)),
    );
  }

  return (
    <form
      className="vehicle-edit-form"
      onSubmit={(event) => {
        event.preventDefault();
        mutation.mutate();
      }}
    >
      <div className="vehicle-edit-image-list">
        {items.map((item, index) => (
          <div className="vehicle-edit-image-row" key={index}>
            <input
              className="form-input"
              placeholder="Code"
              value={item.code}
              onChange={(event) => patchItem(index, { code: event.target.value })}
              required
            />
            <input
              className="form-input"
              placeholder="Tên hiển thị"
              value={item.name}
              onChange={(event) => patchItem(index, { name: event.target.value })}
              required
            />
            <button
              type="button"
              className="btn btn-secondary btn-icon btn-sm"
              onClick={() => removeFeature(index)}
              title="Xóa"
            >
              <Trash2 size={14} />
            </button>
          </div>
        ))}
        {items.length === 0 ? (
          <div className="vehicle-edit-empty">Chưa có tính năng nào.</div>
        ) : null}
      </div>

      <button
        type="button"
        className="btn btn-secondary btn-sm"
        onClick={addFeature}
      >
        <Plus size={14} />
        Thêm tính năng
      </button>

      <MutationFooter mutation={mutation} label="Lưu tính năng" />
    </form>
  );
}

function PricingTab({ vehicle }: { vehicle: AdminVehicleDetail }) {
  const queryClient = useQueryClient();
  const plan = vehicle.activePricePlan;
  const [form, setForm] = useState({
    name: plan?.name ?? "Default plan",
    baseDailyRate: plan?.baseDailyRate ?? 0,
    hourlyRate: plan?.hourlyRate ?? null,
    weekendMultiplier: plan?.weekendMultiplier ?? 1,
    depositAmount: plan?.depositAmount ?? 0,
    currency: plan?.currency ?? "VND",
  });

  useEffect(() => {
    setForm({
      name: plan?.name ?? "Default plan",
      baseDailyRate: plan?.baseDailyRate ?? 0,
      hourlyRate: plan?.hourlyRate ?? null,
      weekendMultiplier: plan?.weekendMultiplier ?? 1,
      depositAmount: plan?.depositAmount ?? 0,
      currency: plan?.currency ?? "VND",
    });
  }, [plan]);

  const mutation = useMutation({
    mutationFn: () =>
      upsertPricePlan(vehicle.id, {
        name: form.name,
        baseDailyRate: Number(form.baseDailyRate),
        hourlyRate: form.hourlyRate === null ? null : Number(form.hourlyRate),
        weekendMultiplier:
          form.weekendMultiplier === null ? null : Number(form.weekendMultiplier),
        depositAmount:
          form.depositAmount === null ? null : Number(form.depositAmount),
        currency: form.currency,
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["vehicles"] }),
  });

  function patch(partial: Partial<typeof form>) {
    setForm((prev) => ({ ...prev, ...partial }));
  }

  return (
    <form
      className="vehicle-edit-form"
      onSubmit={(event) => {
        event.preventDefault();
        mutation.mutate();
      }}
    >
      <FormField label="Tên bảng giá">
        <input
          className="form-input"
          value={form.name}
          onChange={(event) => patch({ name: event.target.value })}
          required
        />
      </FormField>
      <div className="vehicle-form-grid">
        <FormField label="Giá ngày *">
          <input
            className="form-input"
            type="number"
            min={0}
            step={50_000}
            value={form.baseDailyRate}
            onChange={(event) =>
              patch({ baseDailyRate: Number(event.target.value) })
            }
            required
          />
        </FormField>
        <FormField label="Giá giờ">
          <input
            className="form-input"
            type="number"
            min={0}
            step={10_000}
            value={form.hourlyRate ?? ""}
            onChange={(event) =>
              patch({
                hourlyRate:
                  event.target.value === "" ? null : Number(event.target.value),
              })
            }
          />
        </FormField>
        <FormField label="Hệ số cuối tuần">
          <input
            className="form-input"
            type="number"
            min={1}
            step={0.1}
            value={form.weekendMultiplier ?? 1}
            onChange={(event) =>
              patch({ weekendMultiplier: Number(event.target.value) })
            }
          />
        </FormField>
        <FormField label="Tiền cọc">
          <input
            className="form-input"
            type="number"
            min={0}
            step={100_000}
            value={form.depositAmount ?? 0}
            onChange={(event) =>
              patch({ depositAmount: Number(event.target.value) })
            }
          />
        </FormField>
      </div>

      <MutationFooter mutation={mutation} label="Lưu bảng giá" />
    </form>
  );
}

function FormField({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <label className="vehicle-form-field">
      <span>{label}</span>
      {children}
    </label>
  );
}

function MutationFooter({
  mutation,
  label,
}: {
  mutation: { isPending: boolean; isSuccess: boolean; error: unknown };
  label: string;
}) {
  const errorMessage = mutation.error
    ? extractErrorMessage(mutation.error)
    : null;

  return (
    <div className="vehicle-edit-footer">
      {errorMessage ? (
        <div className="vehicle-form-error">{errorMessage}</div>
      ) : null}
      {mutation.isSuccess ? (
        <div className="vehicle-edit-success">Đã lưu thay đổi.</div>
      ) : null}
      <button
        type="submit"
        className="btn btn-primary"
        disabled={mutation.isPending}
      >
        {mutation.isPending ? "Đang lưu..." : label}
      </button>
    </div>
  );
}

function extractErrorMessage(error: unknown): string {
  if (error && typeof error === "object" && "response" in error) {
    const response = (error as { response?: { data?: { message?: string } } })
      .response;
    if (response?.data?.message) return response.data.message;
  }
  if (error instanceof Error) return error.message;
  return "Có lỗi xảy ra. Thử lại.";
}
