import type { CSSProperties } from "react";

export type ChecklistKey =
  | "matchesProfile"
  | "imageSharp"
  | "documentValid"
  | "noTampering";

export type ChecklistState = Record<ChecklistKey, boolean>;

export const INITIAL_CHECKLIST: ChecklistState = {
  matchesProfile: false,
  imageSharp: false,
  documentValid: false,
  noTampering: false,
};

const ITEMS: { key: ChecklistKey; label: string }[] = [
  { key: "matchesProfile", label: "Thông tin trùng với hồ sơ" },
  { key: "imageSharp", label: "Ảnh rõ nét" },
  { key: "documentValid", label: "Giấy tờ còn hạn" },
  { key: "noTampering", label: "Không có dấu hiệu chỉnh sửa" },
];

type Props = {
  state: ChecklistState;
  onChange: (next: ChecklistState) => void;
  disabled?: boolean;
};

export function KycReviewChecklist({ state, onChange, disabled }: Props) {
  return (
    <ul className="kyc-checklist" style={listStyle}>
      {ITEMS.map((item) => (
        <li key={item.key} className="kyc-checklist-item">
          <label>
            <input
              type="checkbox"
              checked={state[item.key]}
              disabled={disabled}
              onChange={(e) => onChange({ ...state, [item.key]: e.target.checked })}
            />
            <span>{item.label}</span>
          </label>
        </li>
      ))}
    </ul>
  );
}

const listStyle: CSSProperties = { listStyle: "none", padding: 0, margin: 0 };
