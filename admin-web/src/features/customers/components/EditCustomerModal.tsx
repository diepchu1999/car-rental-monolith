import { useEffect, useState } from "react";
import { Save, X } from "lucide-react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateCustomer } from "../api/customerAPI";
import { genderLabel } from "../constants";
import { showToast } from "../notify";
import type {
  AdminCustomer,
  Gender,
  UpdateCustomerRequest,
} from "../types";

type Props = {
  open: boolean;
  customer: AdminCustomer | null;
  onClose: () => void;
  onUpdated?: (next: AdminCustomer) => void;
};

type FormState = {
  fullName: string;
  phone: string;
  email: string;
  dateOfBirth: string;
  gender: Gender | "";
};

const EMPTY: FormState = {
  fullName: "",
  phone: "",
  email: "",
  dateOfBirth: "",
  gender: "",
};

// Form sửa thông tin cá nhân — KHÔNG có field KYC, host, status, address.
// Đó là rule nghiệp vụ: admin không sửa được KYC qua form customer.
export function EditCustomerModal({ open, customer, onClose, onUpdated }: Props) {
  const queryClient = useQueryClient();
  const [form, setForm] = useState<FormState>(EMPTY);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open || !customer) return;
    setForm({
      fullName: customer.fullName,
      phone: customer.phone ?? "",
      email: customer.email ?? "",
      dateOfBirth: customer.dateOfBirth ?? "",
      gender: (customer.gender ?? "") as Gender | "",
    });
    setError(null);
  }, [open, customer]);

  useEffect(() => {
    if (!open) return;
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") onClose();
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [open, onClose]);

  const mutation = useMutation({
    mutationFn: (payload: UpdateCustomerRequest) =>
      updateCustomer(customer!.id, payload),
    onSuccess: (next) => {
      queryClient.invalidateQueries({ queryKey: ["customers"] });
      queryClient.invalidateQueries({
        queryKey: ["customers", "detail", customer?.id],
      });
      showToast("Đã cập nhật thông tin khách hàng", "success");
      onUpdated?.(next);
      onClose();
    },
    onError: (e: unknown) => {
      const message =
        (e as { response?: { data?: { message?: string } } })?.response?.data
          ?.message ?? "Không thể cập nhật thông tin khách hàng";
      setError(message);
      showToast(message, "error");
    },
  });

  if (!open || !customer) return null;

  function handleBackdropClick(event: React.MouseEvent<HTMLDivElement>) {
    if (event.target === event.currentTarget) onClose();
  }

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);
    const fullName = form.fullName.trim();
    const phone = form.phone.trim();
    if (!fullName) {
      setError("Vui lòng nhập họ tên");
      return;
    }
    if (!phone) {
      setError("Vui lòng nhập số điện thoại");
      return;
    }
    const payload: UpdateCustomerRequest = {
      fullName,
      phone,
      email: form.email.trim() ? form.email.trim() : null,
      dateOfBirth: form.dateOfBirth ? form.dateOfBirth : null,
      gender: form.gender || null,
    };
    mutation.mutate(payload);
  }

  return (
    <div className="modal-overlay active" onClick={handleBackdropClick}>
      <div className="modal customer-edit-modal">
        <div className="modal-header">
          <h3 className="modal-title">Sửa thông tin khách hàng</h3>
          <button type="button" className="modal-close" onClick={onClose} title="Đóng">
            <X size={16} />
          </button>
        </div>

        <form className="modal-body customer-edit-body" onSubmit={handleSubmit}>
          <div className="customer-edit-grid">
            <Field label="Họ tên" required>
              <input
                type="text"
                value={form.fullName}
                onChange={(e) =>
                  setForm((s) => ({ ...s, fullName: e.target.value }))
                }
                disabled={mutation.isPending}
                required
              />
            </Field>

            <Field label="Số điện thoại" required>
              <input
                type="tel"
                value={form.phone}
                onChange={(e) =>
                  setForm((s) => ({ ...s, phone: e.target.value }))
                }
                disabled={mutation.isPending}
                required
              />
            </Field>

            <Field label="Email">
              <input
                type="email"
                value={form.email}
                onChange={(e) =>
                  setForm((s) => ({ ...s, email: e.target.value }))
                }
                disabled={mutation.isPending}
              />
            </Field>

            <Field label="Ngày sinh">
              <input
                type="date"
                value={form.dateOfBirth}
                onChange={(e) =>
                  setForm((s) => ({ ...s, dateOfBirth: e.target.value }))
                }
                disabled={mutation.isPending}
              />
            </Field>

            <Field label="Giới tính">
              <select
                value={form.gender}
                onChange={(e) =>
                  setForm((s) => ({
                    ...s,
                    gender: e.target.value as Gender | "",
                  }))
                }
                disabled={mutation.isPending}
              >
                <option value="">— Chưa chọn —</option>
                <option value="MALE">{genderLabel.MALE}</option>
                <option value="FEMALE">{genderLabel.FEMALE}</option>
                <option value="OTHER">{genderLabel.OTHER}</option>
              </select>
            </Field>
          </div>

          <p className="customer-edit-note">
            Hồ sơ KYC chỉ được duyệt / từ chối trong popup chi tiết của từng KYC,
            không sửa được từ form này.
          </p>

          {error ? <div className="customer-edit-error">{error}</div> : null}

          <div className="modal-footer customer-edit-footer">
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={onClose}
              disabled={mutation.isPending}
            >
              Hủy
            </button>
            <button
              type="submit"
              className="btn btn-primary btn-sm"
              disabled={mutation.isPending}
            >
              <Save size={14} />
              Lưu thay đổi
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Field({
  label,
  required,
  children,
}: {
  label: string;
  required?: boolean;
  children: React.ReactNode;
}) {
  return (
    <label className="customer-edit-field">
      <span className="customer-edit-label">
        {label}
        {required ? <span className="customer-edit-required"> *</span> : null}
      </span>
      {children}
    </label>
  );
}
