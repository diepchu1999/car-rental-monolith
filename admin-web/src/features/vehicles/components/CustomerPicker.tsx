import { useEffect, useRef, useState } from "react";
import { Search } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import {
  searchCustomers,
  type CustomerSummary,
} from "../../customers/api/customerAPI";

type CustomerPickerProps = {
  value: CustomerSummary | null;
  onChange: (customer: CustomerSummary | null) => void;
  disabled?: boolean;
};

// Debounced search dropdown. Closes on outside click. Limited to 10 results
// so the menu stays scannable.
export function CustomerPicker({
  value,
  onChange,
  disabled = false,
}: CustomerPickerProps) {
  const [search, setSearch] = useState("");
  const [debounced, setDebounced] = useState("");
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(search), 250);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    if (!open) return;
    function onDocClick(event: MouseEvent) {
      if (!ref.current?.contains(event.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", onDocClick);
    return () => document.removeEventListener("mousedown", onDocClick);
  }, [open]);

  const searchQuery = useQuery({
    queryKey: ["customers", "search", debounced],
    queryFn: () => searchCustomers({ q: debounced, size: 10 }),
    enabled: open && debounced.length >= 1,
  });

  const items = searchQuery.data?.items ?? [];

  return (
    <div className="customer-picker" ref={ref}>
      {value ? (
        <div className="picker-selected">
          <div>
            <div className="picker-selected-name">{value.fullName}</div>
            <div className="picker-selected-meta">
              {[value.hostCode, value.phone, value.email]
                .filter(Boolean)
                .join(" · ")}
            </div>
          </div>
          {!disabled ? (
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              onClick={() => onChange(null)}
            >
              Đổi
            </button>
          ) : null}
        </div>
      ) : (
        <>
          <div className="search-box">
            <Search size={14} />
            <input
              type="text"
              placeholder="Tìm khách hàng theo tên, SĐT, email, mã host..."
              value={search}
              disabled={disabled}
              onChange={(event) => {
                setSearch(event.target.value);
                setOpen(true);
              }}
              onFocus={() => setOpen(true)}
            />
          </div>
          {open && debounced ? (
            <div className="picker-dropdown">
              {searchQuery.isLoading ? (
                <div className="picker-dropdown-empty">Đang tìm...</div>
              ) : items.length === 0 ? (
                <div className="picker-dropdown-empty">
                  Không tìm thấy khách hàng.
                </div>
              ) : (
                items.map((customer) => (
                  <button
                    key={customer.id}
                    type="button"
                    className="picker-dropdown-item"
                    onClick={() => {
                      onChange(customer);
                      setOpen(false);
                      setSearch("");
                    }}
                  >
                    <div className="picker-dropdown-name">
                      {customer.fullName}
                    </div>
                    <div className="picker-dropdown-meta">
                      {[customer.hostCode, customer.phone, customer.email]
                        .filter(Boolean)
                        .join(" · ")}
                    </div>
                  </button>
                ))
              )}
            </div>
          ) : null}
        </>
      )}
    </div>
  );
}
