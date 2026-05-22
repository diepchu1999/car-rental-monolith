import { type ReactNode, useState } from "react";
import { Header } from "../components/Header";
import { Sidebar } from "../components/Sidebar";

const SIDEBAR_COLLAPSED_STORAGE_KEY = "sidebar-collapsed";

type AdminLayoutProps = {
  activePage?: string;
  title: string;
  subtitle?: string;
  children: ReactNode;
};

function loadCollapsed(): boolean {
  return localStorage.getItem(SIDEBAR_COLLAPSED_STORAGE_KEY) === "true";
}

function persistCollapsed(collapsed: boolean) {
  localStorage.setItem(SIDEBAR_COLLAPSED_STORAGE_KEY, String(collapsed));
}

export function AdminLayout({
  activePage = "dashboard",
  title,
  subtitle,
  children,
}: AdminLayoutProps) {
  // Desktop collapse and mobile drawer are intentionally separate states; the CSS
  // for each behaves differently even though both affect the same sidebar.
  const [collapsed, setCollapsed] = useState(loadCollapsed);
  const [mobileOpen, setMobileOpen] = useState(false);

  function updateCollapsed(next: boolean) {
    setCollapsed(next);
    persistCollapsed(next);
  }

  return (
    <>
      <Sidebar
        activePage={activePage}
        collapsed={collapsed}
        mobileOpen={mobileOpen}
        onCloseMobile={() => setMobileOpen(false)}
        onExpandCollapsed={() => updateCollapsed(false)}
        onToggleCollapsed={() => updateCollapsed(!collapsed)}
      />
      <main className={`main ${collapsed ? "collapsed" : ""}`}>
        <Header
          title={title}
          subtitle={subtitle}
          onOpenMobileMenu={() => setMobileOpen(true)}
        />
        <div className="content">{children}</div>
      </main>
    </>
  );
}
