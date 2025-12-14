import { getState, logout, setState, getCurrentFacilityId } from '../state';

export function renderNavbar(): string {
  const state = getState();
  const currentFacilityId = getCurrentFacilityId();
  const currentFacility = state.facilities.find(f => f.id === currentFacilityId);
  const currentPath = window.location.hash.slice(1) || '/dashboard';

  return `
    <nav class="navbar">
      <div class="nav-container">
        <div class="nav-left">
          <span class="navbar-brand">Ninja<span>Bux</span></span>
          <nav class="navbar-nav">
            <a href="#/dashboard" class="${currentPath === '/dashboard' ? 'active' : ''}">Dashboard</a>
            <a href="#/ninjas" class="${currentPath === '/ninjas' ? 'active' : ''}">Ninjas</a>
            <a href="#/shop" class="${currentPath === '/shop' ? 'active' : ''}">Shop</a>
            <a href="#/purchases" class="${currentPath === '/purchases' ? 'active' : ''}">Purchases</a>
          </nav>
        </div>
        <div class="navbar-right">
          ${state.facilities.length > 1 ? `
            <select id="facility-select" class="facility-switcher">
              ${state.facilities.map(f => `
                <option value="${f.id}" ${f.id === currentFacilityId ? 'selected' : ''}>${f.name}</option>
              `).join('')}
            </select>
          ` : `
            <span class="navbar-user">${currentFacility?.name || ''}</span>
          `}
          <span class="navbar-user">${state.username}</span>
          <button id="logout-btn" class="btn-logout">Logout</button>
        </div>
      </div>
    </nav>
  `;
}

export function setupNavbarListeners() {
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => logout());
  }

  const facilitySelect = document.getElementById('facility-select') as HTMLSelectElement;
  if (facilitySelect) {
    facilitySelect.addEventListener('change', () => {
      setState({ currentFacilityId: facilitySelect.value });
      // Refresh current page
      const hash = window.location.hash;
      window.location.hash = '';
      window.location.hash = hash;
    });
  }
}
