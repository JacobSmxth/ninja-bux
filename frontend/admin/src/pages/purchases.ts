import { get, put } from '../api/client';
import { getCurrentFacilityId } from '../state';
import { renderNavbar, setupNavbarListeners } from '../components/navbar';
import { showAlert } from '../components/modal';
import type { PurchaseListResponse, Purchase, PurchaseStatus } from '../types';

let purchases: Purchase[] = [];
let currentStatus: PurchaseStatus | '' = 'PENDING';

export async function renderPurchases() {
  const app = document.getElementById('app')!;

  app.innerHTML = `
    ${renderNavbar()}
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>Purchases</h1>
        </div>
        <div class="tabs">
          <button class="tab ${currentStatus === 'PENDING' ? 'active' : ''}" data-status="PENDING">Pending</button>
          <button class="tab ${currentStatus === 'FULFILLED' ? 'active' : ''}" data-status="FULFILLED">Fulfilled</button>
          <button class="tab ${currentStatus === 'CANCELLED' ? 'active' : ''}" data-status="CANCELLED">Cancelled</button>
          <button class="tab ${currentStatus === '' ? 'active' : ''}" data-status="">All</button>
        </div>
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>Ninja</th>
                <th>Item</th>
                <th>Price</th>
                <th>Status</th>
                <th>Purchased</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody id="purchases-tbody">
              <tr><td colspan="6" class="empty">Loading...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  `;

  setupNavbarListeners();

  // Setup tab listeners
  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
      currentStatus = (tab.getAttribute('data-status') || '') as PurchaseStatus | '';
      document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');
      loadPurchases();
    });
  });

  await loadPurchases();
}

async function loadPurchases() {
  const facilityId = getCurrentFacilityId();
  const url = currentStatus
    ? `/api/facilities/${facilityId}/purchases?status=${currentStatus}`
    : `/api/facilities/${facilityId}/purchases`;

  const response = await get<PurchaseListResponse>(url);
  purchases = response.data?.purchases || [];
  renderPurchasesTable();
}

function renderPurchasesTable() {
  const tbody = document.getElementById('purchases-tbody')!;

  if (purchases.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty">No ${currentStatus?.toLowerCase() || ''} purchases</td></tr>`;
    return;
  }

  tbody.innerHTML = purchases.map(purchase => `
    <tr>
      <td>${purchase.ninjaName || purchase.studentId}</td>
      <td>${purchase.itemName}</td>
      <td class="balance">${purchase.price} Bux</td>
      <td>
        <span class="status-badge status-${purchase.status.toLowerCase()}">
          ${purchase.status}
        </span>
      </td>
      <td>${new Date(purchase.purchasedAt).toLocaleString()}</td>
      <td class="actions">
        ${purchase.status === 'PENDING' ? `
          <button class="btn btn-sm btn-success" data-action="fulfill" data-id="${purchase.id}">Fulfill</button>
          <button class="btn btn-sm btn-danger" data-action="cancel" data-id="${purchase.id}">Cancel</button>
        ` : purchase.fulfilledAt ? `
          <span class="text-muted">Fulfilled ${new Date(purchase.fulfilledAt).toLocaleDateString()}</span>
        ` : '-'}
      </td>
    </tr>
  `).join('');

  // Add event listeners
  tbody.querySelectorAll('[data-action="fulfill"]').forEach(btn => {
    btn.addEventListener('click', () => fulfillPurchase(Number(btn.getAttribute('data-id'))));
  });

  tbody.querySelectorAll('[data-action="cancel"]').forEach(btn => {
    btn.addEventListener('click', () => cancelPurchase(Number(btn.getAttribute('data-id'))));
  });
}

async function fulfillPurchase(purchaseId: number) {
  const facilityId = getCurrentFacilityId();
  const response = await put(`/api/facilities/${facilityId}/purchases/${purchaseId}/fulfill`);

  if (response.error) {
    showAlert(response.error, 'error');
    return;
  }

  showAlert('Purchase fulfilled!', 'success');
  await loadPurchases();
}

async function cancelPurchase(purchaseId: number) {
  if (!confirm('Cancel this purchase and refund points?')) return;

  const facilityId = getCurrentFacilityId();
  const response = await put(`/api/facilities/${facilityId}/purchases/${purchaseId}/cancel`);

  if (response.error) {
    showAlert(response.error, 'error');
    return;
  }

  showAlert('Purchase cancelled and points refunded!', 'success');
  await loadPurchases();
}
