import { getState, logout, setState, getCurrentFacilityId } from "../state";

export function renderNavbar(): string {
  const state = getState();
  const currentFacilityId = getCurrentFacilityId();
  const currentFacility = state.facilities.find(
    (f) => f.id === currentFacilityId,
  );
  const currentPath = window.location.hash.slice(1) || "/dashboard";
  const managePaths = ["/ninjas", "/shop", "/purchases", "/admins"];
  const manageActive = managePaths.includes(currentPath);

  return `
    <nav class="navbar">
      <div class="nav-container">
        <div class="nav-left">
          <img src="/CodeNinjasLogo.svg" alt="Code Ninjas" class="app-logo" />
          <span class="app-title">NinjaBux</span>
          <nav class="navbar-nav">
            <a href="#/dashboard" class="${currentPath === "/dashboard" ? "active" : ""}">Dashboard</a>
            <div class="navbar-dropdown">
              <button id="manage-toggle" class="dropdown-toggle ${manageActive ? "active" : ""}">
                Manage
                <span class="caret">▾</span>
              </button>
              <div id="manage-menu" class="dropdown-menu">
                <a href="#/ninjas" class="${currentPath === "/ninjas" ? "active" : ""}">Ninjas</a>
                <a href="#/shop" class="${currentPath === "/shop" ? "active" : ""}">Shop</a>
                <a href="#/purchases" class="${currentPath === "/purchases" ? "active" : ""}">Purchases</a>
                ${state.superAdmin ? `<a href="#/admins" class="${currentPath === "/admins" ? "active" : ""}">Admins</a>` : ""}
              </div>
            </div>
          </nav>
        </div>
        <div class="navbar-right">
          ${
            state.facilities.length > 1
              ? `
            <select id="facility-select" class="facility-switcher">
              ${state.facilities
                .map(
                  (f) => `
                <option value="${f.id}" ${f.id === currentFacilityId ? "selected" : ""}>${f.name}</option>
              `,
                )
                .join("")}
            </select>
          `
              : `
            <span class="navbar-user">${currentFacility?.name || ""}</span>
          `
          }
          <span class="navbar-user">${state.username}</span>
          <button id="logout-btn" class="btn-logout">Logout</button>
        </div>
      </div>
    </nav>
  `;
}

export function setupNavbarListeners() {
  const logoutBtn = document.getElementById("logout-btn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => logout());
  }

  const dropdown = document.querySelector(".navbar-dropdown");
  const toggle = document.getElementById("manage-toggle");
  if (dropdown && toggle) {
    const menu = document.getElementById("manage-menu");
    const closeDropdown = () => dropdown.classList.remove("open");

    toggle.addEventListener("click", (evt) => {
      evt.stopPropagation();
      dropdown.classList.toggle("open");
    });

    if (menu) {
      menu.addEventListener("click", () => closeDropdown());
    }

    document.addEventListener("click", (evt) => {
      if (!dropdown.contains(evt.target as Node)) {
        closeDropdown();
      }
    });
  }

  const facilitySelect = document.getElementById(
    "facility-select",
  ) as HTMLSelectElement;
  if (facilitySelect) {
    facilitySelect.addEventListener("change", () => {
      setState({ currentFacilityId: facilitySelect.value });
      // Refresh current page
      const hash = window.location.hash;
      window.location.hash = "";
      window.location.hash = hash;
    });
  }
}
