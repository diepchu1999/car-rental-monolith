import { UsersRound } from "lucide-react";

type CustomerErrorStateProps = {
  onRetry: () => void;
  message?: string;
};

export function CustomerLoadingState() {
  return (
    <div className="card">
      <div className="card-body customer-state">Đang tải danh sách khách hàng...</div>
    </div>
  );
}

export function CustomerErrorState({ onRetry, message }: CustomerErrorStateProps) {
  return (
    <div className="card">
      <div className="card-body customer-state">
        <div>
          <div className="customer-state-title">Không tải được danh sách khách hàng</div>
          <div className="customer-state-description">
            {message ?? "Đã có lỗi xảy ra khi tải dữ liệu. Vui lòng thử lại."}
          </div>
        </div>
        <button className="btn btn-secondary btn-sm" type="button" onClick={onRetry}>
          Thử lại
        </button>
      </div>
    </div>
  );
}

export function CustomerEmptyState() {
  return (
    <div className="card">
      <div className="card-body empty-state">
        <UsersRound size={40} strokeWidth={1.2} />
        <h3>Không có khách hàng phù hợp</h3>
        <p>Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc.</p>
      </div>
    </div>
  );
}
