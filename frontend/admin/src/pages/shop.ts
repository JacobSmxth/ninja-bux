import { get, post, put, del } from '../api/client';
import { getCurrentFacilityId } from '../state';
import { renderNavbar, setupNavbarListeners } from '../components/navbar';
import { showModal, showAlert, closeModal } from '../components/modal';
import type { ShopListResponse, ShopItem } from '../types';

let shopItems: ShopItem[] = [];

export async function renderShop() {
  const app = document.getElementById('app')!;
  const facilityId = getCurrentFacilityId();

  app.innerHTML = `
    ${renderNavbar()}
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>Shop Items</h1>
          <button id="add-item-btn" class="btn btn-primary">Add Item</button>
        </div>
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Description</th>
                <th>Price</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody id="shop-tbody">
              <tr><td colspan="5" class="empty">Loading...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  `;

  setupNavbarListeners();

  document.getElementById('add-item-btn')?.addEventListener('click', () => showItemModal());

  // Fetch items
  const response = await get<ShopListResponse>(`/api/facilities/${facilityId}/shop`);
  shopItems = response.data?.items || [];
  renderShopTable();
}

function renderShopTable() {
  const tbody = document.getElementById('shop-tbody')!;

  if (shopItems.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" class="empty">No shop items. Add one to get started!</td></tr>';
    return;
  }

  tbody.innerHTML = shopItems.map(item => `
    <tr>
      <td>${item.name}</td>
      <td>${item.description || '-'}</td>
      <td class="balance">${item.price} Bux</td>
      <td>
        <span class="status-badge ${item.isAvailable ? 'status-active' : 'status-inactive'}">
          ${item.isAvailable ? 'Available' : 'Unavailable'}
        </span>
      </td>
      <td class="actions">
        <button class="btn btn-sm btn-secondary" data-action="edit" data-id="${item.id}">Edit</button>
        <button class="btn btn-sm ${item.isAvailable ? 'btn-warning' : 'btn-success'}" data-action="toggle" data-id="${item.id}">
          ${item.isAvailable ? 'Disable' : 'Enable'}
        </button>
        <button class="btn btn-sm btn-danger" data-action="delete" data-id="${item.id}">Delete</button>
      </td>
    </tr>
  `).join('');

  // Add event listeners
  tbody.querySelectorAll('[data-action="edit"]').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = Number(btn.getAttribute('data-id'));
      const item = shopItems.find(i => i.id === id);
      if (item) showItemModal(item);
    });
  });

  tbody.querySelectorAll('[data-action="toggle"]').forEach(btn => {
    btn.addEventListener('click', () => toggleItem(Number(btn.getAttribute('data-id'))));
  });

  tbody.querySelectorAll('[data-action="delete"]').forEach(btn => {
    btn.addEventListener('click', () => deleteItem(Number(btn.getAttribute('data-id'))));
  });
}

function showItemModal(item?: ShopItem) {
  const isEdit = !!item;

  showModal(isEdit ? 'Edit Shop Item' : 'Add Shop Item', `
    <form id="item-form">
      <div class="form-group">
        <label for="item-name">Name</label>
        <input type="text" id="item-name" value="${item?.name || ''}" required>
      </div>
      <div class="form-group">
        <label for="item-description">Description</label>
        <input type="text" id="item-description" value="${item?.description || ''}">
      </div>
      <div class="form-group">
        <label for="item-price">Price (Bux)</label>
        <input type="number" id="item-price" value="${item?.price || ''}" min="1" required>
      </div>
      <div class="form-group checkbox-group">
        <label>
          <input type="checkbox" id="item-available" ${item?.isAvailable !== false ? 'checked' : ''}>
          Available for purchase
        </label>
      </div>
    </form>
  `);

  const modal = document.querySelector('.modal-overlay')!;
  const confirmBtn = modal.querySelector('.modal-confirm');
  if (confirmBtn) {
    confirmBtn.addEventListener('click', async (e) => {
      e.preventDefault();
      await saveItem(item?.id);
    });
  }
}

async function saveItem(itemId?: number) {
  const name = (document.getElementById('item-name') as HTMLInputElement).value;
  const description = (document.getElementById('item-description') as HTMLInputElement).value;
  const price = Number((document.getElementById('item-price') as HTMLInputElement).value);
  const isAvailable = (document.getElementById('item-available') as HTMLInputElement).checked;

  if (!name || !price) {
    showAlert('Please fill in required fields', 'error');
    return;
  }

  const facilityId = getCurrentFacilityId();
  const payload = { name, description, price, isAvailable };

  let response;
  if (itemId) {
    response = await put<ShopItem>(`/api/facilities/${facilityId}/shop/${itemId}`, payload);
  } else {
    response = await post<ShopItem>(`/api/facilities/${facilityId}/shop`, payload);
  }

  if (response.error) {
    showAlert(response.error, 'error');
    return;
  }

  showAlert(itemId ? 'Item updated!' : 'Item created!', 'success');
  closeModal();

  // Refresh list
  const listResponse = await get<ShopListResponse>(`/api/facilities/${facilityId}/shop`);
  shopItems = listResponse.data?.items || [];
  renderShopTable();
}

async function toggleItem(itemId: number) {
  const item = shopItems.find(i => i.id === itemId);
  if (!item) return;

  const facilityId = getCurrentFacilityId();
  const response = await put<ShopItem>(`/api/facilities/${facilityId}/shop/${itemId}`, {
    name: item.name,
    description: item.description,
    price: item.price,
    isAvailable: !item.isAvailable,
  });

  if (response.error) {
    showAlert(response.error, 'error');
    return;
  }

  item.isAvailable = !item.isAvailable;
  showAlert(`Item ${item.isAvailable ? 'enabled' : 'disabled'}!`, 'success');
  renderShopTable();
}

async function deleteItem(itemId: number) {
  if (!confirm('Are you sure you want to delete this item?')) return;

  const facilityId = getCurrentFacilityId();
  const response = await del(`/api/facilities/${facilityId}/shop/${itemId}`);

  if (response.error) {
    showAlert(response.error, 'error');
    return;
  }

  shopItems = shopItems.filter(i => i.id !== itemId);
  showAlert('Item deleted!', 'success');
  renderShopTable();
}
