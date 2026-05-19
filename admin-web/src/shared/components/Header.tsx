import { Bell, Clock3, Menu, Search } from "lucide-react";

type HeaderProps = {
  title: string;
  subtitle?: string;
  onOpenMobileMenu: () => void;
};

export function Header({ title, subtitle, onOpenMobileMenu }: HeaderProps) {
  return (
    <header className="header">
      <div className="header-left">
        <button className="mobile-menu-btn" type="button" onClick={onOpenMobileMenu}>
          <Menu size={20} />
        </button>
        <div>
          <span className="header-greeting">{subtitle}</span>
          <h1 className="header-title">{title}</h1>
        </div>
      </div>

      <div className="header-right">
        <div className="search-box">
          <Search size={16} />
          <input type="text" placeholder="Tìm kiếm..." />
        </div>
        <button className="header-btn" type="button" title="Notifications">
          <Bell size={18} />
          <span className="dot" />
        </button>
        <button className="header-btn" type="button" title="Recent activity">
          <Clock3 size={18} />
        </button>
      </div>
    </header>
  );
}
