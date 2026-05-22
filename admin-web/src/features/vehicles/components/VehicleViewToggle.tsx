import { LayoutGrid, List } from "lucide-react";

export type VehicleViewMode = "grid" | "list";

type VehicleViewToggleProps = {
  viewMode: VehicleViewMode;
  onChange: (viewMode: VehicleViewMode) => void;
};

export function VehicleViewToggle({ viewMode, onChange }: VehicleViewToggleProps) {
  return (
    <div className="vehicle-view-toggle" aria-label="Chọn kiểu hiển thị">
      <ViewButton
        active={viewMode === "grid"}
        title="Grid view"
        onClick={() => onChange("grid")}
      >
        <LayoutGrid size={15} />
      </ViewButton>
      <ViewButton
        active={viewMode === "list"}
        title="List view"
        onClick={() => onChange("list")}
      >
        <List size={15} />
      </ViewButton>
    </div>
  );
}

type ViewButtonProps = {
  active: boolean;
  title: string;
  onClick: () => void;
  children: React.ReactNode;
};

function ViewButton({ active, title, onClick, children }: ViewButtonProps) {
  return (
    <button
      className={`btn btn-secondary btn-sm btn-icon vehicle-view-btn ${active ? "active" : ""}`}
      type="button"
      title={title}
      aria-pressed={active}
      onClick={onClick}
    >
      {children}
    </button>
  );
}
