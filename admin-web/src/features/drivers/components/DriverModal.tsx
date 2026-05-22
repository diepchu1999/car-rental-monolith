import { UserCheck, X } from "lucide-react";

type DriverModalProps = {
  open: boolean;
  onClose: () => void;
};

export function DriverModal({ open, onClose }: DriverModalProps) {
  if (!open) return null;

  function handleBackdropClick(event: React.MouseEvent<HTMLDivElement>) {
    if (event.target === event.currentTarget) {
      onClose();
    }
  }

  return (
    <div className="modal-overlay active" onClick={handleBackdropClick}>
      <div className="modal">
        <div className="modal-header">
          <h3 className="modal-title">Thêm tài xế</h3>
          <button className="modal-close" type="button" onClick={onClose} title="Đóng">
            <X size={16} />
          </button>
        </div>
        <div className="modal-body">
          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="driver-name">
                Họ tên
              </label>
              <input className="form-input" id="driver-name" defaultValue="Nguyễn Minh Khoa" />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="driver-phone">
                SĐT
              </label>
              <input className="form-input" id="driver-phone" defaultValue="0907777888" />
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="driver-license-no">
                GPLX số
              </label>
              <input className="form-input" id="driver-license-no" defaultValue="79B2-778899" />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="driver-license-class">
                Hạng GPLX
              </label>
              <select className="form-select" id="driver-license-class" defaultValue="B2">
                <option>B1</option>
                <option>B2</option>
                <option>C</option>
                <option>D</option>
              </select>
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label className="form-label" htmlFor="driver-exp">
                Kinh nghiệm (năm)
              </label>
              <input className="form-input" id="driver-exp" type="number" defaultValue="3" />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="driver-status">
                Trạng thái
              </label>
              <select className="form-select" id="driver-status" defaultValue="available">
                <option value="available">Sẵn sàng</option>
                <option value="driving">Đang chạy</option>
                <option value="off">Nghỉ phép</option>
              </select>
            </div>
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-secondary" type="button" onClick={onClose}>
            Hủy
          </button>
          <button className="btn btn-primary" type="button" onClick={onClose}>
            <UserCheck size={16} />
            Thêm
          </button>
        </div>
      </div>
    </div>
  );
}
