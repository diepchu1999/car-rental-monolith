import {customerStats} from "../constants";

export function CustomerStats() {
    return (
        <>
            <div className="stats-grid customer-stats-grid">
             {/*   {customerStats.map((stat, index) => (
                    <div className="stat-card animate-fade-up">
                        <div className="stat-header"  style={{ animationDelay: `${index * 0.05}s` }}>
                            <div className="stat-icon"
                        </div>
                    </div>
                ))}*/}
                <div className="stat-card animate-fade-up">
                    <div className="stat-header">
                        <div
                            className="stat-icon"
                            style={{ background: "var(--success-dim)" }}
                        >
                            <svg width="20" height="20" fill="none" viewBox="0 0 20 20">
                                <circle cx="10" cy="8" r="4" stroke="#3ECF8E" strokeWidth="1.2" />
                                <path
                                    d="M4 17c0-3.5 2.5-5 6-5s6 1.5 6 5"
                                    stroke="#3ECF8E"
                                    strokeWidth="1.2"
                                    strokeLinecap="round"
                                />
                            </svg>
                        </div>
                        <span className="stat-trend up">▲ 5.2%</span>
                    </div>
                    <div className="stat-value">4,320</div>
                    <div className="stat-label">Người thuê xe</div>
                </div>

                <div
                    className="stat-card animate-fade-up"
                    style={{ animationDelay: ".05s" }}
                >
                    <div className="stat-header">
                        <div
                            className="stat-icon"
                            style={{ background: "var(--accent-dim)" }}
                        >
                            <svg width="20" height="20" fill="none" viewBox="0 0 20 20">
                                <path
                                    d="M4 16V8l6-4 6 4v8"
                                    stroke="#C8A45C"
                                    strokeWidth="1.2"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                />
                                <rect
                                    x="7"
                                    y="12"
                                    width="6"
                                    height="4"
                                    rx=".5"
                                    stroke="#C8A45C"
                                    strokeWidth="1"
                                />
                            </svg>
                        </div>
                        <span className="stat-trend up">▲ 12%</span>
                    </div>
                    <div className="stat-value">1,571</div>
                    <div className="stat-label">Chủ xe (Host)</div>
                </div>

                <div
                    className="stat-card animate-fade-up"
                    style={{ animationDelay: ".1s" }}
                >
                    <div className="stat-header">
                        <div
                            className="stat-icon"
                            style={{ background: "var(--info-dim)" }}
                        >
                            <svg width="20" height="20" fill="none" viewBox="0 0 20 20">
                                <circle cx="7" cy="8" r="3" stroke="#5BA4F5" strokeWidth="1.2" />
                                <circle cx="14" cy="8" r="3" stroke="#5BA4F5" strokeWidth="1.2" />
                                <path
                                    d="M1 17c0-3 2-4.5 6-4.5M13 17c0-3-2-4.5-6-4.5"
                                    stroke="#5BA4F5"
                                    strokeWidth="1"
                                    strokeLinecap="round"
                                />
                            </svg>
                        </div>
                    </div>
                    <div className="stat-value">892</div>
                    <div className="stat-label">Cả hai vai trò</div>
                </div>
            </div>
        </>
    );
}