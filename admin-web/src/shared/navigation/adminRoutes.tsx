import { lazy, type ComponentType } from "react";

type LazyImporter = () => Promise<Record<string, ComponentType>>;

function lazyNamedPage(importer: LazyImporter, exportName: string) {
  return lazy(() =>
    importer().then((module) => ({
      default: module[exportName],
    })),
  );
}

export type AdminRoute = {
  path: string;
  activePage: string;
  title: string;
  subtitle?: string;
  Page: ComponentType;
};

export const defaultAdminRoutePath = "/vehicles";

export const adminRoutes: AdminRoute[] = [
  {
    path: "/vehicles",
    activePage: "vehicles",
    title: "Vehicles",
    subtitle: "Quản lý đội xe",
    Page: lazyNamedPage(
      () => import("../../features/vehicles/pages/VehicleListPage"),
      "VehicleListPage",
    ),
  },
  {
    path: "/customers",
    activePage: "customers",
    title: "Customers",
    subtitle: "Quản lý khách hàng",
    Page: lazyNamedPage(
        () => import("../../features/customers/pages/CustomerListPage"),
        "CustomerListPage",
    ),
  },
  {
    path: "/drivers",
    activePage: "drivers",
    title: "Drivers",
    subtitle: "Quản lý tài xế",
    Page: lazyNamedPage(
      () => import("../../features/drivers/pages/DriverListPage"),
      "DriverListPage",
    ),
  },
  {
    // Fleet là sub-nav: nav chính "Fleet" trỏ /fleet rồi redirect sang đây.
    path: "/fleet/branches",
    activePage: "fleet",
    title: "Fleet",
    subtitle: "Quản lý chi nhánh",
    Page: lazyNamedPage(
      () => import("../../features/fleet/pages/FleetBranchesPage"),
      "FleetBranchesPage",
    ),
  },
  {
    path: "/fleet/vehicles",
    activePage: "fleet",
    title: "Fleet",
    subtitle: "Xe công ty",
    Page: lazyNamedPage(
      () => import("../../features/fleet/pages/FleetVehiclesPage"),
      "FleetVehiclesPage",
    ),
  },
];
