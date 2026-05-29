import { useState } from "react";
import { ChevronDown, Menu } from "lucide-react";
import { Link, NavLink } from "react-router-dom";
import {
  adminNavGroups,
  type AdminNavGroup,
  type AdminNavItem,
} from "../navigation/adminNavigation";
import { LogoMark } from "./LogoMark";

type SidebarProps = {
  activePage: string;
  collapsed: boolean;
  mobileOpen: boolean;
  onCloseMobile: () => void;
  onExpandCollapsed: () => void;
  onToggleCollapsed: () => void;
};

export function Sidebar({
  activePage,
  collapsed,
  mobileOpen,
  onCloseMobile,
  onExpandCollapsed,
  onToggleCollapsed,
}: SidebarProps) {
  return (
    <>
      <aside
        className={`sidebar ${collapsed ? "collapsed" : ""} ${mobileOpen ? "open" : ""}`}
        id="sidebar"
      >
        <SidebarBrand
          collapsed={collapsed}
          onExpandCollapsed={onExpandCollapsed}
          onToggleCollapsed={onToggleCollapsed}
        />

        <nav className="nav-section">
          {adminNavGroups.map((group) => (
            <NavigationGroup
              key={group.group}
              activePage={activePage}
              group={group}
              onNavigate={onCloseMobile}
            />
          ))}
        </nav>

        <SidebarUserCard />
      </aside>

      <div
        className={`sidebar-overlay ${mobileOpen ? "active" : ""}`}
        onClick={onCloseMobile}
      />
    </>
  );
}

type SidebarBrandProps = {
  collapsed: boolean;
  onExpandCollapsed: () => void;
  onToggleCollapsed: () => void;
};

function SidebarBrand({
  collapsed,
  onExpandCollapsed,
  onToggleCollapsed,
}: SidebarBrandProps) {
  return (
    <div className="logo-section">
      <div className="logo-row">
        <LogoMark collapsed={collapsed} onExpandCollapsed={onExpandCollapsed} />
        <div className="logo-stack">
          <span className="logo-name">AresDrive</span>
          <span className="logo-tag">Admin Panel</span>
        </div>
      </div>
      <button
        className="sidebar-toggle"
        type="button"
        title="Toggle sidebar"
        onClick={onToggleCollapsed}
      >
        <Menu size={20} />
      </button>
    </div>
  );
}

type NavigationGroupProps = {
  activePage: string;
  group: AdminNavGroup;
  onNavigate: () => void;
};

function NavigationGroup({ activePage, group, onNavigate }: NavigationGroupProps) {
  return (
    <div>
      <div className="nav-label">{group.group}</div>
      {group.items.map((item) => (
        <NavigationItem
          key={item.id}
          active={item.id === activePage}
          item={item}
          onNavigate={onNavigate}
        />
      ))}
    </div>
  );
}

type NavigationItemProps = {
  active: boolean;
  item: AdminNavItem;
  onNavigate: () => void;
};

function NavigationItem({ active, item, onNavigate }: NavigationItemProps) {
  const Icon = item.icon;
  const hasChildren = !!item.children?.length;
  // Mở sẵn khi đang ở trong section (vào thẳng bằng URL hoặc sau khi chọn mục
  // con). Ngoài ra việc đóng/mở do user bấm vào nút Fleet điều khiển.
  const [expanded, setExpanded] = useState(active);

  // Item có sub-nav: nút Fleet CHỈ đóng/mở, không điều hướng, không chọn mục
  // con mặc định. User tự bấm "Chi nhánh" / "Xe công ty".
  if (hasChildren) {
    return (
      <>
        <button
          type="button"
          className={`nav-item nav-item-toggle ${active ? "active" : ""}`}
          aria-expanded={expanded}
          onClick={() => setExpanded((value) => !value)}
        >
          <Icon className="nav-icon" size={20} />
          <span className="nav-text">{item.label}</span>
          <ChevronDown
            className={`nav-chevron ${expanded ? "open" : ""}`}
            size={14}
          />
        </button>

        {expanded ? (
          <div className="nav-subnav">
            {item.children!.map((child) => (
              <NavLink
                key={child.id}
                to={child.href}
                className={({ isActive }) =>
                  `nav-subitem ${isActive ? "active" : ""}`
                }
                onClick={onNavigate}
              >
                {child.label}
              </NavLink>
            ))}
          </div>
        ) : null}
      </>
    );
  }

  return (
    <Link
      to={item.href}
      className={`nav-item ${active ? "active" : ""}`}
      onClick={onNavigate}
    >
      <Icon className="nav-icon" size={20} />
      <span className="nav-text">{item.label}</span>
      {item.badge ? <span className="nav-badge">{item.badge}</span> : null}
    </Link>
  );
}

function SidebarUserCard() {
  return (
    <div className="sidebar-footer">
      <Link to="/profile" className="user-card">
        <div className="user-avatar">A</div>
        <div className="user-stack">
          <span className="user-name">Ares</span>
          <span className="user-role">Super Admin</span>
        </div>
      </Link>
    </div>
  );
}
