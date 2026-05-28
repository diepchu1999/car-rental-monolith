/* ============================================
   AresDrive Admin — Sidebar & Header Component
   Include this JS in every page
   ============================================ */

function renderSidebar(activePage) {
    const pages = [
        {
            group: "Main",
            items: [
                {
                    id: "dashboard",
                    label: "Dashboard",
                    icon: '<rect x="2" y="2" width="7" height="7" rx="2" fill="currentColor"/><rect x="11" y="2" width="7" height="4" rx="1.5" fill="currentColor" opacity=".5"/><rect x="2" y="11" width="7" height="4" rx="1.5" fill="currentColor" opacity=".5"/><rect x="11" y="8" width="7" height="10" rx="2" fill="currentColor"/>',
                    href: "index.html",
                },
                {
                    id: "bookings",
                    label: "Bookings",
                    badge: "12",
                    icon: '<rect x="2" y="4" width="16" height="12" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/><path d="M6 9h8M6 12h5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>',
                    href: "bookings.html",
                },
                {
                    id: "vehicles",
                    label: "Vehicles",
                    icon: '<path d="M3 14L3 10L6 5H14L17 10V14Z" stroke="currentColor" stroke-width="1.3" fill="none"/><circle cx="6.5" cy="14" r="2" stroke="currentColor" stroke-width="1.2" fill="none"/><circle cx="13.5" cy="14" r="2" stroke="currentColor" stroke-width="1.2" fill="none"/>',
                    href: "vehicles.html",
                },
                {
                    id: "customers",
                    label: "Customers",
                    icon: '<circle cx="10" cy="6" r="3.5" stroke="currentColor" stroke-width="1.3" fill="none"/><path d="M3 17.5C3 13.5 6 11.5 10 11.5C14 11.5 17 13.5 17 17.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" fill="none"/>',
                    href: "customers.html",
                },
                {
                    id: "drivers",
                    label: "Drivers",
                    icon: '<circle cx="10" cy="10" r="7" stroke="currentColor" stroke-width="1.3" fill="none"/><circle cx="10" cy="10" r="3" stroke="currentColor" stroke-width="1" fill="none"/><path d="M10 3V5M10 15V17M3 10H5M15 10H17" stroke="currentColor" stroke-width="1" stroke-linecap="round"/>',
                    href: "drivers.html",
                },
            ],
        },
        {
            group: "Management",
            items: [
                {
                    id: "fleet",
                    label: "Fleet",
                    icon: '<path d="M4 16L4 8L10 4L16 8V16" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round" fill="none"/><rect x="7" y="12" width="6" height="4" rx="0.5" stroke="currentColor" stroke-width="1.2" fill="none"/>',
                    href: "fleet.html",
                },
                {
                    id: "payments",
                    label: "Payments",
                    icon: '<rect x="3" y="3" width="14" height="14" rx="2" stroke="currentColor" stroke-width="1.3" fill="none"/><path d="M3 7.5H17" stroke="currentColor" stroke-width="1"/><path d="M7 3V7.5" stroke="currentColor" stroke-width="1"/><path d="M13 3V7.5" stroke="currentColor" stroke-width="1"/>',
                    href: "payments.html",
                },
                {
                    id: "reviews",
                    label: "Reviews",
                    icon: '<path d="M10 3L12.5 8L18 8.8L14 12.6L15 18L10 15.3L5 18L6 12.6L2 8.8L7.5 8Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round" fill="none"/>',
                    href: "reviews.html",
                },
            ],
        },
        {
            group: "System",
            items: [
                {
                    id: "settings",
                    label: "Settings",
                    icon: '<circle cx="10" cy="10" r="3" stroke="currentColor" stroke-width="1.3" fill="none"/><path d="M10 2V4M10 16V18M2 10H4M16 10H18M4.93 4.93L6.34 6.34M13.66 13.66L15.07 15.07M15.07 4.93L13.66 6.34M6.34 13.66L4.93 15.07" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>',
                    href: "settings.html",
                },
            ],
        },
    ];

    const logoSvg = `<svg class="logo-mark" viewBox="0 0 42 42" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M21 3L37 10V22C37 31.5 30 37.5 21 40C12 37.5 5 31.5 5 22V10L21 3Z" fill="url(#sg)" stroke="#C8A45C" stroke-width="1.2"/>
    <path d="M21 12L14 30H17.5L19 26H23L24.5 30H28L21 12Z" fill="#0B0E11"/>
    <path d="M20 22L21 16L22 22H20Z" fill="#C8A45C"/>
    <line x1="21" y1="30" x2="21" y2="34" stroke="#C8A45C" stroke-width="1" stroke-dasharray="1.5 1.5" opacity="0.5"/>
    <defs><linearGradient id="sg" x1="21" y1="3" x2="21" y2="40"><stop offset="0%" stop-color="#E4C97A"/><stop offset="100%" stop-color="#8B6914"/></linearGradient></defs>
  </svg>`;

    let navHtml = "";
    pages.forEach((group) => {
        navHtml += `<div class="nav-label">${group.group}</div>`;
        group.items.forEach((item) => {
            const isActive = item.id === activePage;
            const badgeHtml = item.badge
                ? `<span class="nav-badge">${item.badge}</span>`
                : "";
            navHtml += `
        <a href="${item.href}" class="nav-item ${isActive ? "active" : ""}">
          <svg class="nav-icon" viewBox="0 0 20 20" fill="none">${item.icon}</svg>
          <span class="nav-text">${item.label}</span>
          ${badgeHtml}
        </a>`;
        });
    });

    return `
  <aside class="sidebar" id="sidebar">
    <div class="logo-section">
      <div class="logo-row">
        ${logoSvg}
        <div style="display:flex;flex-direction:column">
          <span class="logo-name">AresDrive</span>
          <span class="logo-tag">Admin Panel</span>
        </div>
      </div>
      <button class="sidebar-toggle" id="sidebar-toggle" title="Toggle Sidebar">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M3 5H17M3 10H17M3 15H17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </button>
    </div>
    <nav class="nav-section">${navHtml}</nav>
    <div class="sidebar-footer">
      <a href="profile.html" class="user-card" style="text-decoration:none;color:inherit">
        <div class="user-avatar">A</div>
        <div style="display:flex;flex-direction:column">
          <span class="user-name">Ares (Điệp)</span>
          <span class="user-role">Super Admin</span>
        </div>
      </a>
    </div>
  </aside>
  <div class="sidebar-overlay" id="sidebar-overlay" onclick="document.getElementById('sidebar').classList.remove('open');this.classList.remove('active')"></div>`;
}

function renderHeader(title, subtitle) {
    return `
  <header class="header">
    <div class="header-left">
      <button class="mobile-menu-btn" onclick="document.getElementById('sidebar').classList.add('open');document.getElementById('sidebar-overlay').classList.add('active')">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none"><path d="M3 5h14M3 10h14M3 15h14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
      </button>
      <div>
        <span class="header-greeting">${subtitle || ""}</span>
        <h1 class="header-title">${title}</h1>
      </div>
    </div>
    <div class="header-right">
      <div class="search-box">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="7" cy="7" r="5" stroke="#555860" stroke-width="1.3"/><path d="M11 11L14 14" stroke="#555860" stroke-width="1.3" stroke-linecap="round"/></svg>
        <input type="text" placeholder="Tìm kiếm...">
      </div>
      <button class="header-btn">
        <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 2C6 2 4 4.5 4 7V11L2.5 13H15.5L14 11V7C14 4.5 12 2 9 2Z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/><path d="M7 13C7 14.1 7.9 15 9 15C10.1 15 11 14.1 11 13" stroke="currentColor" stroke-width="1.3"/></svg>
        <span class="dot"></span>
      </button>
      <button class="header-btn">
        <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7" stroke="currentColor" stroke-width="1.3"/><path d="M9 6V9.5L11.5 11" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg>
      </button>
    </div>
  </header>`;
}

// Toast notification
function showToast(message, type = "success") {
    let container = document.querySelector(".toast-container");
    if (!container) {
        container = document.createElement("div");
        container.className = "toast-container";
        document.body.appendChild(container);
    }

    const icons = {
        success: `<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7" stroke="#3ECF8E" stroke-width="1.3"/><path d="M6 9L8 11L12 7" stroke="#3ECF8E" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
        error: `<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7" stroke="#EF5A5A" stroke-width="1.3"/><path d="M6.5 6.5L11.5 11.5M11.5 6.5L6.5 11.5" stroke="#EF5A5A" stroke-width="1.3" stroke-linecap="round"/></svg>`,
        warning: `<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 2L16 15H2L9 2Z" stroke="#F5A623" stroke-width="1.3" stroke-linejoin="round"/><path d="M9 7V10M9 12V12.5" stroke="#F5A623" stroke-width="1.3" stroke-linecap="round"/></svg>`,
    };

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `${icons[type] || icons.success}<span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateX(40px)";
        toast.style.transition = "all 0.3s ease";
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// Modal helpers
function openModal(id) {
    document.getElementById(id)?.classList.add("active");
}

function closeModal(id) {
    document.getElementById(id)?.classList.remove("active");
}

// Format number as VND
function formatVND(n) {
    return new Intl.NumberFormat("vi-VN").format(n) + "đ";
}

// Format number with suffix
function formatShort(n) {
    if (n >= 1e9) return (n / 1e9).toFixed(1) + "B";
    if (n >= 1e6) return (n / 1e6).toFixed(1) + "M";
    if (n >= 1e3) return (n / 1e3).toFixed(1) + "K";
    return n.toString();
}

// Sidebar toggle
function toggleSidebar() {
    const sidebar = document.getElementById("sidebar");
    const main = document.querySelector(".main");
    if (!sidebar || !main) return;
    sidebar.classList.toggle("collapsed");
    main.classList.toggle("collapsed");
    localStorage.setItem(
        "sidebar-collapsed",
        sidebar.classList.contains("collapsed"),
    );
}

document.addEventListener("DOMContentLoaded", function () {
    const sidebar = document.getElementById("sidebar");
    const main = document.querySelector(".main");
    const toggleBtn = document.getElementById("sidebar-toggle");

    if (localStorage.getItem("sidebar-collapsed") === "true" && sidebar && main) {
        sidebar.classList.add("collapsed");
        main.classList.add("collapsed");
    }

    if (toggleBtn) {
        toggleBtn.addEventListener("click", (e) => {
            e.preventDefault();
            e.stopPropagation();
            toggleSidebar();
        });
    }

    // Click on logo to expand when collapsed
    const logoMark = sidebar?.querySelector(".logo-mark");
    if (logoMark) {
        logoMark.style.cursor = "pointer";
        logoMark.addEventListener("click", () => {
            if (sidebar.classList.contains("collapsed")) {
                toggleSidebar();
            }
        });
    }
});
