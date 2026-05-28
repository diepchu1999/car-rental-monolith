import type { ReactNode } from "react";
import { ShieldAlert, Users, UserCheck, Warehouse } from "lucide-react";
import type { CustomerStatsSummary } from "../types";

type StatCardConfig = {
  key: keyof CustomerStatsSummary;
  label: string;
  icon: ReactNode;
  iconBg: string;
};

const numberFormatter = new Intl.NumberFormat("vi-VN");

const cards: StatCardConfig[] = [
  {
    key: "total",
    label: "Tổng khách hàng",
    icon: <Users size={20} />,
    iconBg: "var(--accent-dim)",
  },
  {
    key: "renters",
    label: "Người thuê",
    icon: <UserCheck size={20} />,
    iconBg: "var(--success-dim)",
  },
  {
    key: "hosts",
    label: "Chủ xe (Host)",
    icon: <Warehouse size={20} />,
    iconBg: "var(--info-dim)",
  },
  {
    key: "pendingOrBlocked",
    label: "Chờ KYC / Bị khóa",
    icon: <ShieldAlert size={20} />,
    iconBg: "var(--warning-dim)",
  },
];

type CustomerStatsProps = {
  stats?: CustomerStatsSummary;
  loading?: boolean;
};

export function CustomerStats({ stats, loading }: CustomerStatsProps) {
  return (
    <div className="stats-grid customer-stats-grid">
      {cards.map((card, index) => (
        <div
          key={card.key}
          className="stat-card animate-fade-up"
          style={{ animationDelay: `${index * 0.05}s` }}
        >
          <div className="stat-header">
            <div className="stat-icon" style={{ background: card.iconBg }}>
              {card.icon}
            </div>
          </div>
          <div className="stat-value">
            {loading || !stats ? "—" : numberFormatter.format(stats[card.key])}
          </div>
          <div className="stat-label">{card.label}</div>
        </div>
      ))}
    </div>
  );
}
