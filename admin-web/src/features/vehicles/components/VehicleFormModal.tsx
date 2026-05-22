import { useState } from "react";
import { X } from "lucide-react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createVehicle } from "../api/vehicleAPI";
import { CustomerPicker } from "./CustomerPicker";
import { listActiveBranches } from "../../fleet/api/fleetAPI";
import {
  fuelTypeOptions,
  transmissionOptions,
  vehicleSourceOptions,
} from "../constants";
import type { CustomerSummary } from "../../customers/api/customerAPI";
import type { VehicleSource } from "../types";

type VehicleFormModalProps = {
  open: boolean;
  onClose: () => void;
  onCreated?: (vehicleId: string) => void;
};

type FormState = {
  source: VehicleSource;
  assetCode: string;
  branchId: string;
  brand: string;
  model: string;
  version: string;
  manufactureYear: string;
  licensePlate: string;
  seats: string;
  transmission: string;
  fuelType: string;
};

const INITIAL_FORM: FormState = {
  source: "HOST_OWNED",
  assetCode: "",
  branchId: "",
  brand: "",
  model: "",
  version: "",
  manufactureYear: String(new Date().getFullYear()),
  licensePlate: "",
  seats: "4",
  transmission: "AUTOMATIC",
  fuelType: "GASOLINE",
};

export function VehicleFormModal({
  open,
  onClose,
  onCreated,
}: VehicleFormModalProps) {
  const [form, setForm] = useState<FormState>(INITIAL_FORM);
  const [owner, setOwner] = useState<CustomerSummary | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const branchesQuery = useQuery({
    queryKey: ["fleet", "branches"],
    queryFn: listActiveBranches,
    enabled: open && form.source === "COMPANY_OWNED",
  });
  const branches = branchesQuery.data?.items ?? [];

  const mutation = useMutation({
    mutationFn: createVehicle,
    onSuccess: (created) => {
      queryClient.invalidateQueries({ queryKey: ["vehicles"] });
      onCreated?.(created.id);
      handleClose();
    },
    onError: (error: unknown) => {
      const message = extractErrorMessage(error);
      setErrorMessage(message);
    },
  });

  if (!open) return null;

  const branchOptions = [
    {
      value: "",
      label: branchesQuery.isLoading
        ? "Đang tải chi nhánh..."
        : "— Không gán chi nhánh —",
    },
    ...branches.map((branch) => ({
      value: branch.id,
      label: `${branch.name} (${branch.city})`,
    })),
  ];

  function patch(partial: Partial<FormState>) {
    setForm((prev) => ({ ...prev, ...partial }));
  }

  function handleClose() {
    setForm(INITIAL_FORM);
    setOwner(null);
    setErrorMessage(null);
    mutation.reset();
    onClose();
  }

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setErrorMessage(null);

    if (form.source === "HOST_OWNED" && !owner) {
      setErrorMessage("Chọn chủ xe (host) trước khi lưu.");
      return;
    }
    if (form.source === "COMPANY_OWNED" && !form.assetCode.trim()) {
      setErrorMessage("Nhập mã tài sản (asset code) trước khi lưu.");
      return;
    }

    mutation.mutate({
      source: form.source,
      ownerCustomerId: form.source === "HOST_OWNED" ? owner?.id : null,
      assetCode:
        form.source === "COMPANY_OWNED" ? form.assetCode.trim() : null,
      branchId:
        form.source === "COMPANY_OWNED" && form.branchId
          ? form.branchId
          : null,
      brand: form.brand.trim(),
      model: form.model.trim(),
      version: form.version.trim() || null,
      manufactureYear: Number(form.manufactureYear),
      licensePlate: form.licensePlate.trim(),
      seats: Number(form.seats),
      transmission: form.transmission,
      fuelType: form.fuelType,
    });
  }

  return (
    <div className="modal-overlay active" onClick={handleClose}>
      <div
        className="modal vehicle-form-modal"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <h3 className="modal-title">Thêm xe mới</h3>
          <button
            type="button"
            className="modal-close"
            onClick={handleClose}
            title="Đóng"
          >
            <X size={16} />
          </button>
        </div>

        <form className="modal-body vehicle-form-body" onSubmit={handleSubmit}>
          <div className="vehicle-form-section">
            <label className="vehicle-form-label">Nguồn xe</label>
            <div className="vehicle-form-source-tabs">
              {vehicleSourceOptions
                .filter((option) => option.value !== "")
                .map((option) => (
                  <button
                    key={option.value}
                    type="button"
                    className={`vehicle-form-source-tab ${
                      form.source === option.value ? "active" : ""
                    }`}
                    onClick={() => {
                      patch({
                        source: option.value as VehicleSource,
                        assetCode: "",
                        branchId: "",
                      });
                      setOwner(null);
                    }}
                  >
                    {option.label}
                  </button>
                ))}
            </div>
          </div>

          {form.source === "HOST_OWNED" ? (
            <div className="vehicle-form-section">
              <label className="vehicle-form-label">Chủ xe (host) *</label>
              <CustomerPicker value={owner} onChange={setOwner} />
            </div>
          ) : (
            <div className="vehicle-form-section">
              <FormInput
                label="Mã tài sản (asset code) *"
                value={form.assetCode}
                onChange={(value) => patch({ assetCode: value })}
                required
              />
              <FormSelect
                label="Chi nhánh"
                value={form.branchId}
                options={branchOptions}
                onChange={(value) => patch({ branchId: value })}
              />
            </div>
          )}

          <div className="vehicle-form-grid">
            <FormInput
              label="Hãng *"
              value={form.brand}
              onChange={(value) => patch({ brand: value })}
              required
            />
            <FormInput
              label="Model *"
              value={form.model}
              onChange={(value) => patch({ model: value })}
              required
            />
            <FormInput
              label="Phiên bản"
              value={form.version}
              onChange={(value) => patch({ version: value })}
            />
            <FormInput
              label="Năm SX *"
              type="number"
              min={1990}
              max={new Date().getFullYear() + 1}
              value={form.manufactureYear}
              onChange={(value) => patch({ manufactureYear: value })}
              required
            />
            <FormInput
              label="Biển số *"
              value={form.licensePlate}
              onChange={(value) => patch({ licensePlate: value })}
              required
            />
            <FormInput
              label="Số chỗ *"
              type="number"
              min={1}
              max={64}
              value={form.seats}
              onChange={(value) => patch({ seats: value })}
              required
            />
            <FormSelect
              label="Hộp số *"
              value={form.transmission}
              options={transmissionOptions.filter((option) => option.value !== "")}
              onChange={(value) => patch({ transmission: value })}
            />
            <FormSelect
              label="Nhiên liệu *"
              value={form.fuelType}
              options={fuelTypeOptions.filter((option) => option.value !== "")}
              onChange={(value) => patch({ fuelType: value })}
            />
          </div>

          {errorMessage ? (
            <div className="vehicle-form-error">{errorMessage}</div>
          ) : null}

          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleClose}
              disabled={mutation.isPending}
            >
              Hủy
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={mutation.isPending}
            >
              {mutation.isPending ? "Đang lưu..." : "Tạo xe"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

type FormInputProps = {
  label: string;
  value: string;
  type?: string;
  required?: boolean;
  min?: number;
  max?: number;
  onChange: (value: string) => void;
};

function FormInput({
  label,
  value,
  type = "text",
  required,
  min,
  max,
  onChange,
}: FormInputProps) {
  return (
    <label className="vehicle-form-field">
      <span>{label}</span>
      <input
        className="form-input"
        type={type}
        value={value}
        required={required}
        min={min}
        max={max}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

type FormSelectProps = {
  label: string;
  value: string;
  options: Array<{ value: string; label: string }>;
  onChange: (value: string) => void;
};

function FormSelect({ label, value, options, onChange }: FormSelectProps) {
  return (
    <label className="vehicle-form-field">
      <span>{label}</span>
      <select
        className="form-select"
        value={value}
        onChange={(event) => onChange(event.target.value)}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}

function extractErrorMessage(error: unknown): string {
  if (error && typeof error === "object" && "response" in error) {
    const response = (error as { response?: { data?: { message?: string } } })
      .response;
    if (response?.data?.message) return response.data.message;
  }
  if (error instanceof Error) return error.message;
  return "Không thể tạo xe. Vui lòng thử lại.";
}
