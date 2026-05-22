import { useState } from "react";
import { Plus } from "lucide-react";
import { DriverCard } from "../components/DriverCard";
import { DriverModal } from "../components/DriverModal";
import { DriverStats } from "../components/DriverStats";
import { mockDrivers } from "../data/mockDrivers";
import "./drivers.css";

export function DriverListPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);

  return (
    <>
      <div className="page-header">
        <div className="page-header-left">
          <h2>Tài xế</h2>
          <p>Quản lý đội ngũ tài xế cho dịch vụ có tài xế</p>
        </div>
        <button
          className="btn btn-primary btn-sm"
          type="button"
          onClick={() => setIsModalOpen(true)}
        >
          <Plus size={16} />
          Thêm tài xế
        </button>
      </div>

      <DriverStats />

      <div className="driver-grid">
        {mockDrivers.map((driver, index) => (
          <DriverCard driver={driver} key={driver.id} index={index} />
        ))}
      </div>

      <DriverModal open={isModalOpen} onClose={() => setIsModalOpen(false)} />
    </>
  );
}
