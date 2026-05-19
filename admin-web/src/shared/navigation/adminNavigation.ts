import type { ComponentType } from "react";
import {
  CalendarCheck,
  Car,
  CreditCard,
  LayoutDashboard,
  Settings,
  Star,
  Users,
  Warehouse,
  UserRoundCheck,
} from "lucide-react";

export type AdminNavItem = {
  id: string;
  label: string;
  href: string;
  badge?: string;
  icon: ComponentType<{ className?: string; size?: number }>;
};

export type AdminNavGroup = {
  group: string;
  items: AdminNavItem[];
};

// Keep navigation metadata centralized so routes, badges, and future RBAC rules
// can be maintained without touching the Sidebar rendering logic.
export const adminNavGroups: AdminNavGroup[] = [
  {
    group: "Main",
    items: [
      { id: "dashboard", label: "Dashboard", href: "/", icon: LayoutDashboard },
      {
        id: "bookings",
        label: "Bookings",
        href: "/bookings",
        badge: "12",
        icon: CalendarCheck,
      },
      { id: "vehicles", label: "Vehicles", href: "/vehicles", icon: Car },
      { id: "customers", label: "Customers", href: "/customers", icon: Users },
      { id: "drivers", label: "Drivers", href: "/drivers", icon: UserRoundCheck },
    ],
  },
  {
    group: "Management",
    items: [
      { id: "fleet", label: "Fleet", href: "/fleet", icon: Warehouse },
      { id: "payments", label: "Payments", href: "/payments", icon: CreditCard },
      { id: "reviews", label: "Reviews", href: "/reviews", icon: Star },
    ],
  },
  {
    group: "System",
    items: [{ id: "settings", label: "Settings", href: "/settings", icon: Settings }],
  },
];
