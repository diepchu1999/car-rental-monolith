import { useEffect, useState, type ReactNode } from "react";
import { X } from "lucide-react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createBranch, updateBranch } from "../api/fleetAPI";
import { showToast } from "../notify";
import type { Branch, BranchInput, BranchStatus } from "../types";

type BranchFormModalProps = {
  open: boolean;
  // null => tạo mới; có giá trị => sửa.
  branch: Branch | null;
  onClose: () => void;
  onSaved?: (branch: Branch) => void;
};

type FormState = {
  code: string;
  name: string;
  city: string;
  address: string;
  phone: string;
  status: BranchStatus;
};

const EMPTY_FORM: FormState = {
  code: "",
  name: "",
  city: "",
  address: "",
  phone: "",
  status: "ACTIVE",
};

const PHONE_PATTERN = /^0\d{9,10}$/;

export function BranchFormModal({
  open,
  branch,
  onClose,
  onSaved,
}: BranchFormModalProps) {
  const queryClient = useQueryClient();
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const isEdit = branch !== null;

  // Đồng bộ form mỗi khi mở: prefill khi sửa, reset khi tạo.
  useEffect(() => {
    if (!open) return;
    setErrors({});
    setForm(
      branch
        ? {
            code: branch.code,
            name: branch.name,
            city: branch.city ?? "",
            address: branch.address ?? "",
            phone: branch.phone ?? "",
            status: branch.status,
          }
        : EMPTY_FORM,
    );
  }, [open, branch]);

  const mutation = useMutation({
    mutationFn: (payload: BranchInput) =>
      isEdit ? updateBranch(branch!.id, payload) : createBranch(payload),
    onSuccess: (saved) => {
      queryClient.invalidateQueries({ queryKey: ["fleet", "branches"] });
      showToast(isEdit ? "Đã cập nhật chi nhánh" : "Đã tạo chi nhánh", "success");
      onClose();
      onSaved?.(saved);
    },
    onError: () => {
      showToast(
        isEdit ? "Không thể cập nhật chi nhánh" : "Không thể tạo chi nhánh",
        "error",
      );
    },
  });

  if (!open) return null;

  function setField<K extends keyof FormState>(key: K, value: FormState[K]) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  function validate(): Record<string, string> {
    const next: Record<string, string> = {};
    if (!form.code.trim()) next.code = "Vui lòng nhập mã chi nhánh.";
    if (!form.name.trim()) next.name = "Vui lòng nhập tên chi nhánh.";
    if (!form.city.trim()) next.city = "Vui lòng nhập thành phố.";
    if (!form.address.trim()) next.address = "Vui lòng nhập địa chỉ.";
    if (form.phone.trim() && !PHONE_PATTERN.test(form.phone.trim())) {
      next.phone = "Số điện thoại không hợp lệ (bắt đầu bằng 0, 10-11 số).";
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
    mutation.mutate({
      code: form.code.trim(),
      name: form.name.trim(),
      city: form.city.trim(),
      address: form.address.trim(),
      phone: form.phone.trim(),
      status: form.status,
    });
  }

  function handleBackdropClick(event: React.MouseEvent<HTMLDivElement>) {
    if (event.target === event.currentTarget) onClose();
  }

  return (
    <div className="modal-overlay active" onClick={handleBackdropClick}>
      <div className="modal branch-form-modal">
        <div className="modal-header">
          <h3 className="modal-title">
            {isEdit ? "Sửa chi nhánh" : "Thêm chi nhánh"}
          </h3>
          <button type="button" className="modal-close" onClick={onClose} title="Đóng">
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="modal-body branch-form-body">
            <div className="branch-form-grid">
              <Field label="Mã chi nhánh *" error={errors.code}>
                <input
                  className="form-input"
                  value={form.code}
                  placeholder="HCM-Q1"
                  disabled={isEdit}
                  onChange={(e) => setField("code", e.target.value)}
                />
              </Field>
              <Field label="Tên chi nhánh *" error={errors.name}>
                <input
                  className="form-input"
                  value={form.name}
                  placeholder="Chi nhánh Quận 1"
                  onChange={(e) => setField("name", e.target.value)}
                />
              </Field>
              <Field label="Thành phố *" error={errors.city}>
                <input
                  className="form-input"
                  value={form.city}
                  placeholder="Hồ Chí Minh"
                  onChange={(e) => setField("city", e.target.value)}
                />
              </Field>
              <Field label="Số điện thoại" error={errors.phone}>
                <input
                  className="form-input"
                  value={form.phone}
                  placeholder="0901234567"
                  onChange={(e) => setField("phone", e.target.value)}
                />
              </Field>
              <Field label="Địa chỉ *" error={errors.address} full>
                <input
                  className="form-input"
                  value={form.address}
                  placeholder="12 Nguyễn Huệ, Phường Bến Nghé"
                  onChange={(e) => setField("address", e.target.value)}
                />
              </Field>
              <Field label="Trạng thái">
                <select
                  className="form-select"
                  value={form.status}
                  onChange={(e) => setField("status", e.target.value as BranchStatus)}
                >
                  <option value="ACTIVE">Đang hoạt động</option>
                  <option value="INACTIVE">Ngừng hoạt động</option>
                </select>
              </Field>
            </div>
          </div>

          <div className="modal-footer">
            <button
              className="btn btn-secondary"
              type="button"
              onClick={onClose}
              disabled={mutation.isPending}
            >
              Hủy
            </button>
            <button className="btn btn-primary" type="submit" disabled={mutation.isPending}>
              {mutation.isPending
                ? "Đang lưu..."
                : isEdit
                  ? "Lưu thay đổi"
                  : "Thêm chi nhánh"}
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
  children: ReactNode;
};

function Field({ label, error, full, children }: FieldProps) {
  return (
    <label className={`branch-form-field ${full ? "branch-form-field-full" : ""}`}>
      <span>{label}</span>
      {children}
      {error ? <span className="branch-form-field-error">{error}</span> : null}
    </label>
  );
}
