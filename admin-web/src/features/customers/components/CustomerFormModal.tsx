import { useState } from "react";
import { X } from "lucide-react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createAdminCustomer, type CreateCustomerInput } from "../api/customerAPI";
import { AddressSelect } from "../../locations/components/AddressSelect";
import { documentTypeLabel, genderLabel, roleLabel } from "../constants";
import { showToast } from "../notify";
import type { CustomerRole, DocumentType, Gender } from "../types";

type CustomerFormModalProps = {
  open: boolean;
  onClose: () => void;
  onCreated?: (customerId: string) => void;
};

type FormState = {
  fullName: string;
  phone: string;
  email: string;
  dateOfBirth: string;
  gender: "" | Gender;
  roles: Record<CustomerRole, boolean>;
  host: { hostCode: string; displayName: string; bio: string };
  kycEnabled: boolean;
  kyc: {
    legalName: string;
    documentType: DocumentType;
    documentNumber: string;
    issuedDate: string;
    issuedPlace: string;
  };
  address: {
    label: string;
    line1: string;
    provinceCode: string;
    communeCode: string;
  };
};

const INITIAL_STATE: FormState = {
  fullName: "",
  phone: "",
  email: "",
  dateOfBirth: "",
  gender: "",
  roles: { RENTER: true, HOST: false },
  host: { hostCode: "", displayName: "", bio: "" },
  kycEnabled: false,
  kyc: {
    legalName: "",
    documentType: "NATIONAL_ID",
    documentNumber: "",
    issuedDate: "",
    issuedPlace: "",
  },
  address: {
    label: "Mặc định",
    line1: "",
    provinceCode: "",
    communeCode: "",
  },
};

const PHONE_PATTERN = /^0\d{9,10}$/;
const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

export function CustomerFormModal({
  open,
  onClose,
  onCreated,
}: CustomerFormModalProps) {
  const queryClient = useQueryClient();
  const [form, setForm] = useState<FormState>(INITIAL_STATE);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const mutation = useMutation({
    mutationFn: createAdminCustomer,
    onSuccess: (customer) => {
      queryClient.invalidateQueries({ queryKey: ["customers"] });
      showToast("Đã thêm khách hàng (demo)", "success");
      handleClose();
      onCreated?.(customer.id);
    },
    onError: () => {
      showToast("Không thể thêm khách hàng", "error");
    },
  });

  if (!open) return null;

  const isHost = form.roles.HOST;

  function setField<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  function toggleRole(role: CustomerRole) {
    setForm((prev) => ({
      ...prev,
      roles: { ...prev.roles, [role]: !prev.roles[role] },
    }));
  }

  function handleClose() {
    setForm(INITIAL_STATE);
    setErrors({});
    onClose();
  }

  function validate(): Record<string, string> {
    const next: Record<string, string> = {};

    if (!form.fullName.trim()) next.fullName = "Vui lòng nhập họ tên.";

    if (!form.phone.trim()) {
      next.phone = "Vui lòng nhập số điện thoại.";
    } else if (!PHONE_PATTERN.test(form.phone.trim())) {
      next.phone = "Số điện thoại không hợp lệ (bắt đầu bằng 0, 10-11 số).";
    }

    if (form.email.trim() && !EMAIL_PATTERN.test(form.email.trim())) {
      next.email = "Email không hợp lệ.";
    }

    if (!form.roles.RENTER && !form.roles.HOST) {
      next.roles = "Chọn ít nhất một vai trò.";
    }

    if (isHost && !form.host.displayName.trim()) {
      next.hostDisplayName = "Tên hiển thị host là bắt buộc.";
    }

    if (form.kycEnabled) {
      if (!form.kyc.legalName.trim()) {
        next.kycLegalName = "Nhập tên trên giấy tờ.";
      }
      if (!form.kyc.documentNumber.trim()) {
        next.kycDocumentNumber = "Nhập số giấy tờ.";
      }
    }

    return next;
  }

  function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    const validation = validate();
    setErrors(validation);
    if (Object.keys(validation).length > 0) {
      showToast("Vui lòng kiểm tra lại thông tin.", "warning");
      return;
    }

    const roles: CustomerRole[] = [];
    if (form.roles.RENTER) roles.push("RENTER");
    if (form.roles.HOST) roles.push("HOST");

    const payload: CreateCustomerInput = {
      fullName: form.fullName,
      phone: form.phone,
      email: form.email || undefined,
      dateOfBirth: form.dateOfBirth || undefined,
      gender: form.gender || undefined,
      roles,
      host: isHost
        ? {
            hostCode: form.host.hostCode || undefined,
            displayName: form.host.displayName,
            bio: form.host.bio || undefined,
          }
        : undefined,
      kyc: form.kycEnabled
        ? {
            legalName: form.kyc.legalName,
            documentType: form.kyc.documentType,
            documentNumber: form.kyc.documentNumber,
            issuedDate: form.kyc.issuedDate || undefined,
            issuedPlace: form.kyc.issuedPlace || undefined,
          }
        : undefined,
      address: form.address.line1.trim()
        ? {
            label: form.address.label,
            line1: form.address.line1,
            provinceCode: form.address.provinceCode || undefined,
            communeCode: form.address.communeCode || undefined,
          }
        : undefined,
    };

    mutation.mutate(payload);
  }

  function handleBackdropClick(event: React.MouseEvent<HTMLDivElement>) {
    if (event.target === event.currentTarget) handleClose();
  }

  return (
    <div className="modal-overlay active" onClick={handleBackdropClick}>
      <div className="modal customer-form-modal">
        <div className="modal-header">
          <h3 className="modal-title">Thêm khách hàng</h3>
          <button type="button" className="modal-close" onClick={handleClose} title="Đóng">
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="customer-form-body">
            {/* Thông tin cơ bản */}
            <section className="customer-form-section">
              <div className="customer-form-section-title">Thông tin cơ bản</div>
              <div className="customer-form-grid">
                <Field label="Họ tên *" error={errors.fullName}>
                  <input
                    className="form-input"
                    value={form.fullName}
                    placeholder="Nguyễn Văn A"
                    onChange={(e) => setField("fullName", e.target.value)}
                  />
                </Field>
                <Field label="Số điện thoại *" error={errors.phone}>
                  <input
                    className="form-input"
                    value={form.phone}
                    placeholder="0901234567"
                    onChange={(e) => setField("phone", e.target.value)}
                  />
                </Field>
                <Field label="Email" error={errors.email}>
                  <input
                    className="form-input"
                    type="email"
                    value={form.email}
                    placeholder="email@example.com"
                    onChange={(e) => setField("email", e.target.value)}
                  />
                </Field>
                <Field label="Ngày sinh">
                  <input
                    className="form-input"
                    type="date"
                    value={form.dateOfBirth}
                    onChange={(e) => setField("dateOfBirth", e.target.value)}
                  />
                </Field>
                <Field label="Giới tính">
                  <select
                    className="form-select"
                    value={form.gender}
                    onChange={(e) => setField("gender", e.target.value as "" | Gender)}
                  >
                    <option value="">— Chọn —</option>
                    {(Object.keys(genderLabel) as Gender[]).map((g) => (
                      <option key={g} value={g}>
                        {genderLabel[g]}
                      </option>
                    ))}
                  </select>
                </Field>
              </div>
            </section>

            {/* Vai trò */}
            <section className="customer-form-section">
              <div className="customer-form-section-title">Vai trò *</div>
              <div className="customer-role-toggle">
                {(Object.keys(roleLabel) as CustomerRole[]).map((role) => (
                  <button
                    key={role}
                    type="button"
                    className={`customer-role-tab ${form.roles[role] ? "active" : ""}`}
                    onClick={() => toggleRole(role)}
                  >
                    {roleLabel[role]}
                  </button>
                ))}
              </div>
              {errors.roles ? (
                <div className="customer-form-field-error">{errors.roles}</div>
              ) : null}
            </section>

            {/* Host profile */}
            {isHost ? (
              <section className="customer-form-section">
                <div className="customer-form-section-title">Hồ sơ Host</div>
                <div className="customer-form-grid">
                  <Field label="Mã host (để trống = tự sinh)">
                    <input
                      className="form-input"
                      value={form.host.hostCode}
                      placeholder="HOST-0001"
                      onChange={(e) =>
                        setField("host", { ...form.host, hostCode: e.target.value })
                      }
                    />
                  </Field>
                  <Field label="Tên hiển thị *" error={errors.hostDisplayName}>
                    <input
                      className="form-input"
                      value={form.host.displayName}
                      placeholder="An's Garage"
                      onChange={(e) =>
                        setField("host", { ...form.host, displayName: e.target.value })
                      }
                    />
                  </Field>
                  <Field label="Giới thiệu" full>
                    <textarea
                      className="form-textarea"
                      rows={2}
                      value={form.host.bio}
                      placeholder="Mô tả ngắn về host..."
                      onChange={(e) =>
                        setField("host", { ...form.host, bio: e.target.value })
                      }
                    />
                  </Field>
                </div>
              </section>
            ) : null}

            {/* KYC */}
            <section className="customer-form-section">
              <label className="customer-form-toggle">
                <input
                  type="checkbox"
                  checked={form.kycEnabled}
                  onChange={(e) => setField("kycEnabled", e.target.checked)}
                />
                <span className="customer-form-section-title">Tạo hồ sơ KYC</span>
              </label>
              {form.kycEnabled ? (
                <div className="customer-form-grid">
                  <Field label="Tên trên giấy tờ *" error={errors.kycLegalName}>
                    <input
                      className="form-input"
                      value={form.kyc.legalName}
                      onChange={(e) =>
                        setField("kyc", { ...form.kyc, legalName: e.target.value })
                      }
                    />
                  </Field>
                  <Field label="Loại giấy tờ">
                    <select
                      className="form-select"
                      value={form.kyc.documentType}
                      onChange={(e) =>
                        setField("kyc", {
                          ...form.kyc,
                          documentType: e.target.value as DocumentType,
                        })
                      }
                    >
                      {(Object.keys(documentTypeLabel) as DocumentType[]).map((t) => (
                        <option key={t} value={t}>
                          {documentTypeLabel[t]}
                        </option>
                      ))}
                    </select>
                  </Field>
                  <Field label="Số giấy tờ *" error={errors.kycDocumentNumber}>
                    <input
                      className="form-input"
                      value={form.kyc.documentNumber}
                      onChange={(e) =>
                        setField("kyc", { ...form.kyc, documentNumber: e.target.value })
                      }
                    />
                  </Field>
                  <Field label="Ngày cấp">
                    <input
                      className="form-input"
                      type="date"
                      value={form.kyc.issuedDate}
                      onChange={(e) =>
                        setField("kyc", { ...form.kyc, issuedDate: e.target.value })
                      }
                    />
                  </Field>
                  <Field label="Nơi cấp">
                    <input
                      className="form-input"
                      value={form.kyc.issuedPlace}
                      placeholder="Cục CS QLHC về TTXH"
                      onChange={(e) =>
                        setField("kyc", { ...form.kyc, issuedPlace: e.target.value })
                      }
                    />
                  </Field>
                </div>
              ) : null}
            </section>

            {/* Địa chỉ mặc định */}
            <section className="customer-form-section">
              <div className="customer-form-section-title">Địa chỉ mặc định</div>
              <div className="customer-form-grid">
                <Field label="Nhãn">
                  <input
                    className="form-input"
                    value={form.address.label}
                    placeholder="Nhà riêng"
                    onChange={(e) =>
                      setField("address", { ...form.address, label: e.target.value })
                    }
                  />
                </Field>
                <Field label="Địa chỉ chi tiết (số nhà, đường, thôn/tổ)" full>
                  <input
                    className="form-input"
                    value={form.address.line1}
                    placeholder="12 Nguyễn Trãi"
                    onChange={(e) =>
                      setField("address", { ...form.address, line1: e.target.value })
                    }
                  />
                </Field>
              </div>
              <AddressSelect
                provinceCode={form.address.provinceCode}
                communeCode={form.address.communeCode}
                onChange={(next) =>
                  setField("address", { ...form.address, ...next })
                }
              />
            </section>
          </div>

          <div className="modal-footer">
            <button
              className="btn btn-secondary"
              type="button"
              onClick={handleClose}
              disabled={mutation.isPending}
            >
              Hủy
            </button>
            <button className="btn btn-primary" type="submit" disabled={mutation.isPending}>
              {mutation.isPending ? "Đang lưu..." : "Thêm khách hàng"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

type FieldProps = {
  label: string;
  error?: string;
  full?: boolean;
  children: React.ReactNode;
};

function Field({ label, error, full, children }: FieldProps) {
  return (
    <label className={`customer-form-field ${full ? "customer-form-field-full" : ""}`}>
      <span>{label}</span>
      {children}
      {error ? <span className="customer-form-field-error">{error}</span> : null}
    </label>
  );
}
