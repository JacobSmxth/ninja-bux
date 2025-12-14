import { get, post } from '../api/client';
import { getState, setCurrentNinja, isLoggedIn } from '../state';
import { navigate } from '../router';
import { renderNavbar } from '../components/navbar';
import type { ShopListResponse, ShopItem, PurchaseResponse, Ninja } from '../types';

let currentBalance = 0;

function canAfford(price: number): boolean {
  return currentBalance >= price;
}

function renderShopItem(item: ShopItem): string {
  const affordable = canAfford(item.price);
  const available = item.isAvailable;
  const canBuy = affordable && available;

  return `
    <div class="shop-item ${!canBuy ? 'disabled' : ''}" data-item-id="${item.id}">
      <div class="shop-item-icon">
        <span class="item-placeholder">${item.name.charAt(0)}</span>
      </div>
      <div class="shop-item-details">
        <h3 class="shop-item-name">${item.name}</h3>
        <p class="shop-item-description">${item.description}</p>
      </div>
      <div class="shop-item-footer">
        <span class="shop-item-price">${item.price} Bux</span>
        <button
          class="btn btn-buy ${!canBuy ? 'btn-disabled' : ''}"
          ${!canBuy ? 'disabled' : ''}
          data-item-id="${item.id}"
          data-item-name="${item.name}"
          data-item-price="${item.price}"
        >
          ${!available ? 'Unavailable' : !affordable ? 'Not enough Bux' : 'Buy'}
        </button>
      </div>
    </div>
  `;
}

function showPurchaseModal(itemId: number, itemName: string, price: number) {
  const modal = document.createElement('div');
  modal.className = 'modal-overlay';
  modal.innerHTML = `
    <div class="modal">
      <h2>Confirm Purchase</h2>
      <p>Are you sure you want to buy <strong>${itemName}</strong> for <strong>${price} Bux</strong>?</p>
      <p class="modal-balance">Your balance: ${currentBalance} Bux → ${currentBalance - price} Bux</p>
      <div class="modal-actions">
        <button class="btn btn-secondary" id="cancel-purchase">Cancel</button>
        <button class="btn btn-primary" id="confirm-purchase">Confirm</button>
      </div>
    </div>
  `;

  document.body.appendChild(modal);

  modal.querySelector('#cancel-purchase')!.addEventListener('click', () => {
    modal.remove();
  });

  modal.querySelector('#confirm-purchase')!.addEventListener('click', async () => {
    const { facilityId, studentId } = getState();
    const confirmBtn = modal.querySelector('#confirm-purchase') as HTMLButtonElement;
    confirmBtn.disabled = true;
    confirmBtn.textContent = 'Processing...';

    const res = await post<PurchaseResponse>(`/facilities/${facilityId}/ninjas/${studentId}/purchases`, {
      shopItemId: itemId
    });

    if (res.error) {
      showToast(`Purchase failed: ${res.error}`, 'error');
      modal.remove();
      return;
    }

    currentBalance = res.data!.newBalance;
    updateBalanceDisplay();

    // Re-render shop items to update button states
    await refreshShopItems();

    modal.remove();
    showToast(`Successfully purchased ${itemName}!`, 'success');
  });

  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      modal.remove();
    }
  });
}

function showToast(message: string, type: 'success' | 'error') {
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('fade-out');
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

function updateBalanceDisplay() {
  const balanceEl = document.querySelector('.shop-balance-amount');
  if (balanceEl) {
    balanceEl.textContent = `${currentBalance} Bux`;
  }

  // Update navbar balance
  const navBalance = document.querySelector('.nav-balance');
  if (navBalance) {
    navBalance.textContent = `${currentBalance} Bux`;
  }
}

async function refreshShopItems() {
  const { facilityId } = getState();
  const res = await get<ShopListResponse>(`/facilities/${facilityId}/shop`);

  if (res.data) {
    const grid = document.querySelector('.shop-grid');
    if (grid) {
      grid.innerHTML = res.data.items.map(item => renderShopItem(item)).join('');
      attachBuyHandlers();
    }
  }
}

function attachBuyHandlers() {
  document.querySelectorAll('.btn-buy:not(.btn-disabled)').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const target = e.target as HTMLElement;
      const itemId = parseInt(target.getAttribute('data-item-id')!);
      const itemName = target.getAttribute('data-item-name')!;
      const price = parseInt(target.getAttribute('data-item-price')!);
      showPurchaseModal(itemId, itemName, price);
    });
  });
}

export async function renderShop() {
  const container = document.getElementById('app')!;

  if (!isLoggedIn()) {
    navigate('/');
    return;
  }

  const { facilityId, studentId, currentNinja } = getState();

  container.innerHTML = `
    ${renderNavbar()}
    <main class="shop-page">
      <div class="loading">Loading shop...</div>
    </main>
  `;

  // Fetch ninja (for balance) and shop items
  const ninjaRes = currentNinja
    ? { data: currentNinja, error: undefined }
    : await get<Ninja>(`/facilities/${facilityId}/ninjas/${studentId}`);
  const shopRes = await get<ShopListResponse>(`/facilities/${facilityId}/shop`);

  const mainContainer = container.querySelector('main')!;

  if (ninjaRes.error || shopRes.error) {
    mainContainer.innerHTML = `<div class="error">Failed to load shop</div>`;
    return;
  }

  const ninja = ninjaRes.data!;
  setCurrentNinja(ninja);
  currentBalance = ninja.currentBalance;

  const items = shopRes.data!.items;

  mainContainer.innerHTML = `
    <div class="shop-container">
      <div class="shop">
        <header class="shop-header">
          <h1>Shop</h1>
          <div class="shop-balance">
            <span class="shop-balance-label">Your Balance:</span>
            <span class="shop-balance-amount">${currentBalance} Bux</span>
          </div>
        </header>

        <div class="shop-grid">
          ${items.length > 0
            ? items.map(item => renderShopItem(item)).join('')
            : '<div class="empty">No items available</div>'
          }
        </div>
      </div>
    </div>
  `;

  attachBuyHandlers();
}
