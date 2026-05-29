import { Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AdminLayout } from "./shared/layouts/AdminLayout";
import {
  adminRoutes,
  defaultAdminRoutePath,
  type AdminRoute,
} from "./shared/navigation/adminRoutes";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to={defaultAdminRoutePath} replace />} />
      {/* Fleet là khu vực có sub-nav; /fleet mặc định vào tab Chi nhánh. */}
      <Route path="/fleet" element={<Navigate to="/fleet/branches" replace />} />
      {adminRoutes.map((route) => (
        <Route
          key={route.path}
          path={route.path}
          element={<AdminRouteView route={route} />}
        />
      ))}
      <Route path="*" element={<Navigate to={defaultAdminRoutePath} replace />} />
    </Routes>
  );
}

type AdminRouteViewProps = {
  route: AdminRoute;
};

function AdminRouteView({ route }: AdminRouteViewProps) {
  const Page = route.Page;

  return (
    <AdminLayout
      activePage={route.activePage}
      title={route.title}
      subtitle={route.subtitle}
    >
      <Suspense fallback={<PageLoadingState />}>
        <Page />
      </Suspense>
    </AdminLayout>
  );
}

function PageLoadingState() {
  return (
    <div className="card">
      <div className="card-body text-secondary">Đang tải trang...</div>
    </div>
  );
}
