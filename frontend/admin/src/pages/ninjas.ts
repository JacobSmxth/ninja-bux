import { get, post } from "../api/client";
import { getCurrentFacilityId } from "../state";
import { renderNavbar, setupNavbarListeners } from "../components/navbar";
import { showModal, showAlert } from "../components/modal";
import type {
  NinjaListResponse,
  Ninja,
  AdjustmentResponse,
  LedgerResponse,
} from "../types";

let allNinjas: Ninja[] = [];

export async function renderNinjas() {
  const app = document.getElementById("app")!;
  const facilityId = getCurrentFacilityId();

  app.innerHTML = `
    ${renderNavbar()}
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>Ninjas</h1>
        </div>
        <div class="filters">
          <div class="filter-form">
            <div class="filter-group">
              <input type="text" id="search-input" placeholder="Search by name...">
            </div>
            <div class="filter-group">
              <select id="belt-filter">
                <option value="">All Belts</option>
                <option value="White Belt">White Belt</option>
                <option value="Yellow Belt">Yellow Belt</option>
                <option value="Orange Belt">Orange Belt</option>
                <option value="Green Belt">Green Belt</option>
                <option value="Blue Belt">Blue Belt</option>
              </select>
            </div>
          </div>
        </div>
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Belt</th>
                <th>Level</th>
                <th>Balance</th>
                <th>Last Synced</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody id="ninjas-tbody">
              <tr><td colspan="6" class="empty">Loading...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  `;

  setupNavbarListeners();

  // Fetch ninjas
  const response = await get<NinjaListResponse>(
    `/api/facilities/${facilityId}/ninjas?size=500`,
  );
  allNinjas = response.data?.ninjas || [];

  renderNinjasTable();

  // Setup filters
  document
    .getElementById("search-input")
    ?.addEventListener("input", renderNinjasTable);
  document
    .getElementById("belt-filter")
    ?.addEventListener("change", renderNinjasTable);
}

function renderNinjasTable() {
  const tbody = document.getElementById("ninjas-tbody")!;
  const searchTerm =
    (
      document.getElementById("search-input") as HTMLInputElement
    )?.value.toLowerCase() || "";
  const beltFilter =
    (document.getElementById("belt-filter") as HTMLSelectElement)?.value || "";

  let filtered = allNinjas;

  if (searchTerm) {
    filtered = filtered.filter((n) =>
      `${n.firstName} ${n.lastName}`.toLowerCase().includes(searchTerm),
    );
  }

  if (beltFilter) {
    filtered = filtered.filter((n) => n.courseName === beltFilter);
  }

  if (filtered.length === 0) {
    tbody.innerHTML =
      '<tr><td colspan="6" class="empty">No ninjas found</td></tr>';
    return;
  }

  tbody.innerHTML = filtered
    .map(
      (ninja) => `
    <tr>
      <td>${ninja.firstName} ${ninja.lastName}</td>
      <td><span class="belt-badge belt-${ninja.courseName.toLowerCase().replace(" ", "-")}">${ninja.courseName}</span></td>
      <td>${ninja.levelName}</td>
      <td class="balance">${ninja.currentBalance} Bux</td>
      <td>${ninja.lastSyncedAt ? new Date(ninja.lastSyncedAt).toLocaleDateString() : "Never"}</td>
      <td class="actions">
        <button class="btn btn-sm btn-secondary" data-action="ledger" data-student-id="${ninja.studentId}">Ledger</button>
        <button class="btn btn-sm btn-primary" data-action="adjust" data-student-id="${ninja.studentId}" data-name="${ninja.firstName} ${ninja.lastName}" data-balance="${ninja.currentBalance}">Adjust</button>
      </td>
    </tr>
  `,
    )
    .join("");

  // Add event listeners
  tbody.querySelectorAll('[data-action="adjust"]').forEach((btn) => {
    btn.addEventListener("click", () => {
      const studentId = btn.getAttribute("data-student-id")!;
      const name = btn.getAttribute("data-name")!;
      const balance = btn.getAttribute("data-balance")!;
      showAdjustModal(studentId, name, Number(balance));
    });
  });

  tbody.querySelectorAll('[data-action="ledger"]').forEach((btn) => {
    btn.addEventListener("click", () => {
      const studentId = btn.getAttribute("data-student-id")!;
      showLedgerModal(studentId);
    });
  });
}

function showAdjustModal(
  studentId: string,
  name: string,
  currentBalance: number,
) {
  showModal({
    title: "Adjust Bux",
    content: `
      <div class="modal-info">
        <strong>${name}</strong><br>
        Current Balance: <strong>${currentBalance} Bux</strong>
      </div>
      <form id="adjust-form">
        <div class="form-group">
          <label for="adjust-amount">Amount (positive to add, negative to deduct)</label>
          <input type="number" id="adjust-amount" required>
        </div>
        <div class="form-group">
          <label for="adjust-reason">Reason</label>
          <input type="text" id="adjust-reason" required placeholder="e.g., Bonus for helping">
        </div>
      </form>
    `,
    showConfirm: true,
    confirmText: "Adjust",
    onConfirm: async () => {
      const amount = Number(
        (document.getElementById("adjust-amount") as HTMLInputElement).value,
      );
      const reason = (
        document.getElementById("adjust-reason") as HTMLInputElement
      ).value;

      if (!amount || !reason) {
        showAlert("Please fill in all fields", "error");
        throw new Error("Validation failed");
      }

      const facilityId = getCurrentFacilityId();
      const response = await post<AdjustmentResponse>(
        `/api/facilities/${facilityId}/ninjas/${studentId}/adjustments`,
        { amount, reason },
      );

      if (response.error) {
        showAlert(response.error, "error");
        throw new Error(response.error);
      }

      showAlert(
        `Bux adjusted! New balance: ${response.data!.newBalance} Bux`,
        "success",
      );

      // Update the ninja in our list
      const ninja = allNinjas.find((n) => n.studentId === studentId);
      if (ninja) {
        ninja.currentBalance = response.data!.newBalance;
        renderNinjasTable();
      }
    },
  });
}

async function showLedgerModal(studentId: string) {
  const facilityId = getCurrentFacilityId();
  const response = await get<LedgerResponse>(
    `/api/facilities/${facilityId}/ninjas/${studentId}/ledger?limit=20`,
  );

  if (response.error) {
    showAlert("Failed to load ledger", "error");
    return;
  }

  const { transactions, currentBalance } = response.data!;

  const content = `
    <div class="modal-info">
      Current Balance: <strong>${currentBalance} Bux</strong>
    </div>
    <table class="data-table" style="margin-top: 16px;">
      <thead>
        <tr>
          <th>Date</th>
          <th>Description</th>
          <th>Amount</th>
        </tr>
      </thead>
      <tbody>
        ${transactions.length === 0 ? '<tr><td colspan="3" class="empty">No transactions</td></tr>' : ""}
        ${transactions
          .map(
            (txn) => `
          <tr>
            <td>${new Date(txn.createdAt).toLocaleDateString()}</td>
            <td>${txn.description}</td>
            <td class="${txn.amount >= 0 ? "text-success" : "text-danger"}">${txn.amount >= 0 ? "+" : ""}${txn.amount}</td>
          </tr>
        `,
          )
          .join("")}
      </tbody>
    </table>
  `;

  showModal("Transaction History", content);
}
