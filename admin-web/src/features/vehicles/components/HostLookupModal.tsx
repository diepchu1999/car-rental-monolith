import { useEffect, useState } from "react";
import { Check, ChevronLeft, ChevronRight, Eye, Search, X } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import {
  searchCustomers,
  type CustomerSummary,
} from "../../customers/api/customerAPI";
import { HostDetailModal } from "./HostDetailModal";

const PAGE_SIZE = 8;

type HostLookupModalProps = {
  open: boolean;
  onClose: () => void;
  onSelect: (host: CustomerSummary) => void;
};

// Popup duyệt danh sách host (chỉ active host — hostOnly=true). Cho xem chi tiết
// (tái dùng CustomerDetailModal của module customer) và chọn 1 host để auto-fill
// vào "Chủ xe" ở form tạo vehicle. Không tạo/sửa gì ở đây.
export function HostLookupModal({ open, onClose, onSelect }: HostLookupModalProps) {
  const [search, setSearch] = useState("");
  const [debounced, setDebounced] = useState("");
  const [page, setPage] = useState(1);
  const [detailId, setDetailId] = useState<string | null>(null);

  // Reset state mỗi lần đóng để lần mở sau sạch.
  useEffect(() => {
    if (!open) {
      setSearch("");
      setDebounced("");
      setPage(1);
      setDetailId(null);
    }
  }, [open]);

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebounced(search);
      setPage(1);
    }, 300);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    if (!open) return;
    function onKeyDown(event: KeyboardEvent) {
      // Esc đóng popup chi tiết trước (nếu đang mở), nếu không thì đóng lookup.
      if (event.key !== "Escape") return;
      if (detailId !== null) setDetailId(null);
      else onClose();
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [open, onClose, detailId]);

  const query = useQuery({
    queryKey: ["customers", "search", "host-lookup", debounced, page],
    queryFn: () =>
      searchCustomers({ q: debounced, page, size: PAGE_SIZE, hostOnly: true }),
    enabled: open,
  });

  if (!open) return null;

  const data = query.data;
  const items = data?.items ?? [];

  function handleBackdropClick(event: React.MouseEvent<HTMLDivElement>) {
    if (event.target === event.currentTarget) onClose();
  }

  return (
    <div className="modal-overlay active" onClick={handleBackdropClick}>
      <div className="modal host-lookup-modal">
        <div className="modal-header">
          <h3 className="modal-title">Chọn chủ xe (host)</h3>
          <button type="button" className="modal-close" onClick={onClose} title="Đóng">
            <X size={16} />
          </button>
        </div>

        <div className="modal-body host-lookup-body">
          <div className="search-box host-lookup-search">
            <Search size={14} />
            <input
              type="text"
              placeholder="Tìm host theo tên, SĐT, email, mã host..."
              value={search}
              autoFocus
              onChange={(event) => setSearch(event.target.value)}
            />
          </div>

          <div className="table-wrapper host-lookup-table-wrapper">
            <table className="host-lookup-table">
              <thead>
                <tr>
                  <th>Chủ xe</th>
                  <th>Mã host</th>
                  <th>Liên hệ</th>
                  <th>Trạng thái</th>
                  <th className="host-lookup-col-action" />
                </tr>
              </thead>
              <tbody>
                {query.isLoading ? (
                  <tr>
                    <td colSpan={5} className="host-lookup-state">
                      Đang tải...
                    </td>
                  </tr>
                ) : items.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="host-lookup-state">
                      Không tìm thấy host.
                    </td>
                  </tr>
                ) : (
                  items.map((host) => (
                    <tr key={host.id} onDoubleClick={() => onSelect(host)}>
                      <td>
                        <div className="host-lookup-name">{host.fullName}</div>
                        <div className="host-lookup-id">{host.id}</div>
                      </td>
                      <td>
                        {host.hostCode ? (
                          <span className="host-lookup-code">{host.hostCode}</span>
                        ) : (
                          <span className="host-lookup-muted">—</span>
                        )}
                      </td>
                      <td>
                        <div className="host-lookup-contact">
                          <span>{host.phone ?? "—"}</span>
                          <span className="host-lookup-muted">{host.email ?? "—"}</span>
                        </div>
                      </td>
                      <td>{host.status}</td>
                      <td className="host-lookup-col-action">
                        <div className="host-lookup-actions">
                          <button
                            type="button"
                            className="btn btn-secondary btn-sm"
                            onClick={() => setDetailId(host.id)}
                          >
                            <Eye size={14} />
                            Chi tiết
                          </button>
                          <button
                            type="button"
                            className="btn btn-primary btn-sm"
                            onClick={() => onSelect(host)}
                          >
                            <Check size={14} />
                            Chọn
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        <div className="modal-footer host-lookup-footer">
          <span className="host-lookup-total">
            {data ? `${data.total} host` : ""}
          </span>
          <div className="host-lookup-pager">
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              disabled={!data?.hasPrevious}
              onClick={() => setPage((p) => Math.max(1, p - 1))}
            >
              <ChevronLeft size={14} />
            </button>
            <span className="host-lookup-page">
              {data ? `${data.page} / ${data.totalPages}` : "—"}
            </span>
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              disabled={!data?.hasNext}
              onClick={() => setPage((p) => p + 1)}
            >
              <ChevronRight size={14} />
            </button>
          </div>
        </div>
      </div>

      {/* Modal chi tiết host read-only (CSS tự chứa) — render trong overlay lookup,
          z-index cao hơn nhờ xuất hiện sau trong DOM. */}
      <HostDetailModal customerId={detailId} onClose={() => setDetailId(null)} />
    </div>
  );
}
