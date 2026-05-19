import type { KeyboardEvent } from "react";

type LogoMarkProps = {
  collapsed: boolean;
  onExpandCollapsed: () => void;
};

export function LogoMark({ collapsed, onExpandCollapsed }: LogoMarkProps) {
  function handleClick() {
    if (collapsed) {
      onExpandCollapsed();
    }
  }

  function handleKeyDown(event: KeyboardEvent<SVGSVGElement>) {
    if (!collapsed) return;
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      onExpandCollapsed();
    }
  }

  return (
    <svg
      className="logo-mark"
      viewBox="0 0 42 42"
      fill="none"
      role={collapsed ? "button" : "img"}
      aria-label={collapsed ? "Expand sidebar" : "AresDrive"}
      tabIndex={collapsed ? 0 : -1}
      onClick={handleClick}
      onKeyDown={handleKeyDown}
      style={{ cursor: collapsed ? "pointer" : "default" }}
    >
      <path
        d="M21 3L37 10V22C37 31.5 30 37.5 21 40C12 37.5 5 31.5 5 22V10L21 3Z"
        fill="url(#sg)"
        stroke="#C8A45C"
        strokeWidth="1.2"
      />
      <path d="M21 12L14 30H17.5L19 26H23L24.5 30H28L21 12Z" fill="#0B0E11" />
      <path d="M20 22L21 16L22 22H20Z" fill="#C8A45C" />
      <line
        x1="21"
        y1="30"
        x2="21"
        y2="34"
        stroke="#C8A45C"
        strokeWidth="1"
        strokeDasharray="1.5 1.5"
        opacity="0.5"
      />
      <defs>
        <linearGradient id="sg" x1="21" y1="3" x2="21" y2="40">
          <stop offset="0%" stopColor="#E4C97A" />
          <stop offset="100%" stopColor="#8B6914" />
        </linearGradient>
      </defs>
    </svg>
  );
}
