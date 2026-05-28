export type ToastType = "success" | "error" | "warning";

const ICONS: Record<ToastType, string> = {
  success: `<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7" stroke="#3ECF8E" stroke-width="1.3"/><path d="M6 9L8 11L12 7" stroke="#3ECF8E" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  error: `<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7" stroke="#EF5A5A" stroke-width="1.3"/><path d="M6.5 6.5L11.5 11.5M11.5 6.5L6.5 11.5" stroke="#EF5A5A" stroke-width="1.3" stroke-linecap="round"/></svg>`,
  warning: `<svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 2L16 15H2L9 2Z" stroke="#F5A623" stroke-width="1.3" stroke-linejoin="round"/><path d="M9 7V10M9 12V12.5" stroke="#F5A623" stroke-width="1.3" stroke-linecap="round"/></svg>`,
};

// Lightweight imperative toast reusing the shared .toast styles, so mock
// actions can give feedback without a global toast provider.
export function showToast(message: string, type: ToastType = "success"): void {
  let container = document.querySelector(".toast-container") as HTMLElement | null;
  if (!container) {
    container = document.createElement("div");
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  const text = document.createElement("span");
  text.textContent = message;
  toast.innerHTML = ICONS[type];
  toast.appendChild(text);
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateX(40px)";
    toast.style.transition = "all 0.3s ease";
    setTimeout(() => toast.remove(), 300);
  }, 3200);
}
