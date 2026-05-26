import {customerStats} from "../constants";
import {CustomerStatIcons} from "./CustomerStatIcons";

export function CustomerStats() {
    return (
        <>
            <div className="stats-grid customer-stats-grid">
                {customerStats.map((stat, index) => (
                    <div className="stat-card animate-fade-up">
                        <div className="stat-header"  style={{ animationDelay: `${index * 0.05}s` }}>
                            <div className="stat-icon"
                                 style={{ background: stat.style }}>
                                <CustomerStatIcons type = {stat.type }/>
                            </div>
                        </div>
                        <div className="stat-value">{stat.value}</div>
                        <div className="stat-label">{stat.label}</div>
                    </div>
                ))}
            </div>
        </>
    );
}