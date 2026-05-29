import type { ReactNode } from "react";
import "./fleet.css";

// Khung khu vực Fleet. Sub-nav (Chi nhánh / Xe công ty) nằm ở sidebar trái
// (xem adminNavigation.children), nên ở đây chỉ là wrapper nạp fleet.css +
// render nội dung trang.
export function FleetLayoutPage({ children }: { children: ReactNode }) {
  return <>{children}</>;
}
