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
    path: "/drivers",
    activePage: "drivers",
    title: "Drivers",
    subtitle: "Quản lý tài xế",
    Page: lazyNamedPage(
      () => import("../../features/drivers/pages/DriverListPage"),
      "DriverListPage",
    ),
  },
];
