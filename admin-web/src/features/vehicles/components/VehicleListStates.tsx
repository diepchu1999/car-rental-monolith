type VehicleErrorStateProps = {
  onRetry: () => void;
  message?: string;
};

export function VehicleLoadingState() {
  return (
    <div className="card">
      <div className="card-body vehicle-state">Đang tải danh sách xe...</div>
    </div>
  );
}

export function VehicleErrorState({ onRetry, message }: VehicleErrorStateProps) {
  return (
    <div className="card">
      <div className="card-body vehicle-state">
        <div>
          <div className="vehicle-state-title">Không tải được danh sách xe</div>
          <div className="vehicle-state-description">
            {message ?? "Kiểm tra backend đang chạy ở port 8080 và CORS đã được bật."}
          </div>
        </div>
        <button className="btn btn-secondary btn-sm" type="button" onClick={onRetry}>
          Thử lại
        </button>
      </div>
    </div>
  );
}

export function VehicleEmptyState() {
  return (
    <div className="card">
      <div className="card-body vehicle-state">
        <div>
          <div className="vehicle-state-title">Chưa có xe phù hợp</div>
          <div className="vehicle-state-description">
            Thử đổi bộ lọc hoặc thêm data test trong bảng vehicle.vehicles.
          </div>
        </div>
      </div>
    </div>
  );
}
