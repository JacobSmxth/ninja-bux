import { get } from '../api/client';
import { getCurrentFacilityId } from '../state';
import { renderNavbar, setupNavbarListeners } from '../components/navbar';
import type { NinjaListResponse, PurchaseListResponse, ShopListResponse } from '../types';

export async function renderDashboard() {
  const app = document.getElementById('app')!;
  const facilityId = getCurrentFacilityId();

  app.innerHTML = `
    ${renderNavbar()}
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>Dashboard</h1>
        </div>
        <div class="stats-grid" id="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">...</div>
            <div class="stat-content">
              <div class="stat-value">-</div>
              <div class="stat-label">Loading...</div>
            </div>
          </div>
        </div>
        <div class="quick-actions">
          <h2>Quick Actions</h2>
          <div class="action-buttons">
            <a href="#/ninjas" class="btn btn-primary">Manage Ninjas</a>
            <a href="#/shop" class="btn btn-secondary">Manage Shop</a>
            <a href="#/purchases" class="btn btn-secondary">View Purchases</a>
          </div>
        </div>
      </div>
    </main>
  `;

  setupNavbarListeners();

  // Fetch stats
  const [ninjasRes, purchasesRes, shopRes] = await Promise.all([
    get<NinjaListResponse>(`/api/facilities/${facilityId}/ninjas`),
    get<PurchaseListResponse>(`/api/facilities/${facilityId}/purchases`),
    get<ShopListResponse>(`/api/facilities/${facilityId}/shop`),
  ]);

  const ninjas = ninjasRes.data?.ninjas || [];
  const purchases = purchasesRes.data?.purchases || [];
  const shopItems = shopRes.data?.items || [];

  const totalBux = ninjas.reduce((sum, n) => sum + n.currentBalance, 0);
  const pendingPurchases = purchases.filter(p => p.status === 'PENDING').length;

  const statsGrid = document.getElementById('stats-grid')!;
  statsGrid.innerHTML = `
    <div class="stat-card">
      <div class="stat-icon">N</div>
      <div class="stat-content">
        <div class="stat-value">${ninjas.length}</div>
        <div class="stat-label">Total Ninjas</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">B</div>
      <div class="stat-content">
        <div class="stat-value">${totalBux.toLocaleString()}</div>
        <div class="stat-label">Total Bux in Circulation</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">P</div>
      <div class="stat-content">
        <div class="stat-value">${pendingPurchases}</div>
        <div class="stat-label">Pending Purchases</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">S</div>
      <div class="stat-content">
        <div class="stat-value">${shopItems.length}</div>
        <div class="stat-label">Shop Items</div>
      </div>
    </div>
  `;
}
