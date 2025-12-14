import { getState, logout } from '../state';
import { navigate } from '../router';

export function renderNavbar(): string {
  const { currentNinja } = getState();
  const balance = currentNinja?.currentBalance ?? 0;
  const name = currentNinja ? `${currentNinja.firstName}` : '';

  return `
    <nav class="navbar">
      <div class="nav-container">
        <div class="nav-left">
          <img src="/CodeNinjasLogo.svg" alt="Code Ninjas" class="app-logo" />
          <span class="app-title">NinjaBux</span>
        </div>

        <div class="navbar-links">
          <a href="#/dashboard" class="nav-link">Dashboard</a>
          <a href="#/shop" class="nav-link">Shop</a>
          <a href="#/leaderboard" class="nav-link">Leaderboard</a>
        </div>

        <div class="navbar-right">
          <span class="nav-balance">${balance} Bux</span>
          <span class="nav-user">${name}</span>
          <button class="btn btn-logout" id="logout-btn">Logout</button>
        </div>
      </div>
    </nav>
  `;
}

export function attachNavbarHandlers() {
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      logout();
      navigate('/');
    });
  }
}
