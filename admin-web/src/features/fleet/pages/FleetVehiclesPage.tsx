import { Construction } from "lucide-react";
import { FleetLayoutPage } from "./FleetLayoutPage";

// Placeholder tab "Xe công ty". Sẽ build đầy đủ sau (đã có searchFleetVehicles
// trong fleetAPI để nối khi triển khai).
export function FleetVehiclesPage() {
  return (
    <FleetLayoutPage>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Xe công ty</h2>
          <p>Danh sách xe thuộc đội xe công ty (AresDrive)</p>
        </div>
      </div>

      <div className="card">
        <div className="card-body empty-state">
          <Construction size={40} strokeWidth={1.2} />
          <h3>Đang phát triển</h3>
          <p>Màn hình quản lý xe công ty sẽ sớm có mặt tại đây.</p>
        </div>
      </div>
    </FleetLayoutPage>
  );
}
