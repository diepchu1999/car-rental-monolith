import { driverStats } from "../constants";

export function DriverStats() {
  return (
    <div className="stats-grid driver-stats-grid">
      {driverStats.map((stat, index) => (
        <div
          className="stat-card animate-fade-up"
          key={stat.label}
          style={{ animationDelay: `${index * 0.05}s` }}
        >
          <div className={`stat-value driver-stat-value ${stat.className}`}>
            {stat.value}
          </div>
          <div className="stat-label">{stat.label}</div>
        </div>
      ))}
    </div>
  );
}
