import { get, post, put, del } from "../api/client";
import { getState } from "../state";
import { renderNavbar, setupNavbarListeners } from "../components/navbar";
import { showModal, showAlert } from "../components/modal";
import { navigate } from "../router";
import type { AdminListResponse, AdminUser, Facility } from "../types";

let allAdmins: AdminUser[] = [];
let allFacilities: Facility[] = [];

export async function renderAdmins() {
  const state = getState();

  // Only super admins can access this page
  if (!state.superAdmin) {
    navigate("/dashboard");
    return;
  }

  const app = document.getElementById("app")!;

  app.innerHTML = `
    ${renderNavbar()}
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>Admin Management</h1>
          <button class="btn btn-primary" id="add-admin-btn">Add Admin</button>
        </div>
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Facilities</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody id="admins-tbody">
              <tr><td colspan="6" class="empty">Loading...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  `;

  setupNavbarListeners();

  // Fetch admins and facilities
  const [adminsRes, facilitiesRes] = await Promise.all([
    get<AdminListResponse>("/api/admin/admins"),
    get<Facility[]>("/api/admin/facilities"),
  ]);

  if (adminsRes.error) {
    showAlert("Failed to load admins: " + adminsRes.error, "error");
    return;
  }

  allAdmins = adminsRes.data?.admins || [];
  allFacilities = facilitiesRes.data || [];

  renderAdminsTable();

  document.getElementById("add-admin-btn")?.addEventListener("click", () => {
    showAdminModal();
  });
}

function renderAdminsTable() {
  const tbody = document.getElementById("admins-tbody")!;

  if (allAdmins.length === 0) {
    tbody.innerHTML =
      '<tr><td colspan="6" class="empty">No admins found</td></tr>';
    return;
  }

  tbody.innerHTML = allAdmins
    .map(
      (admin) => `
    <tr>
      <td>${admin.username}</td>
      <td>${admin.email}</td>
      <td><span class="badge ${admin.superAdmin ? "badge-primary" : "badge-secondary"}">${admin.superAdmin ? "Super Admin" : "Admin"}</span></td>
      <td>${admin.superAdmin ? "All" : admin.facilities.map((f) => f.name).join(", ") || "None"}</td>
      <td>${new Date(admin.createdAt).toLocaleDateString()}</td>
      <td class="actions">
        <button class="btn btn-sm btn-secondary" data-action="edit" data-id="${admin.id}">Edit</button>
        <button class="btn btn-sm btn-danger" data-action="delete" data-id="${admin.id}" data-username="${admin.username}">Delete</button>
      </td>
    </tr>
  `,
    )
    .join("");

  // Add event listeners
  tbody.querySelectorAll('[data-action="edit"]').forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = Number(btn.getAttribute("data-id"));
      const admin = allAdmins.find((a) => a.id === id);
      if (admin) showAdminModal(admin);
    });
  });

  tbody.querySelectorAll('[data-action="delete"]').forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = Number(btn.getAttribute("data-id"));
      const username = btn.getAttribute("data-username")!;
      showDeleteModal(id, username);
    });
  });
}

function showAdminModal(admin?: AdminUser) {
  const isEdit = !!admin;
  const title = isEdit ? "Edit Admin" : "Create Admin";

  showModal({
    title,
    content: `
      <form id="admin-form">
        <div class="form-group">
          <label for="admin-username">Username</label>
          <input type="text" id="admin-username" required minlength="3" value="${admin?.username || ""}">
        </div>
        <div class="form-group">
          <label for="admin-email">Email</label>
          <input type="email" id="admin-email" required value="${admin?.email || ""}">
        </div>
        <div class="form-group">
          <label for="admin-password">Password ${isEdit ? "(leave blank to keep current)" : ""}</label>
          <input type="password" id="admin-password" ${isEdit ? "" : "required"} minlength="6">
        </div>
        <div class="form-group">
          <label>
            <input type="checkbox" id="admin-super" ${admin?.superAdmin ? "checked" : ""}>
            Super Admin (access to all facilities)
          </label>
        </div>
        <div class="form-group" id="facilities-group">
          <label>Facilities</label>
          <div class="checkbox-group">
            ${allFacilities
              .map(
                (f) => `
              <label class="checkbox-label">
                <input type="checkbox" name="facilities" value="${f.id}"
                  ${admin?.facilities.some((af) => af.id === f.id) ? "checked" : ""}>
                ${f.name}
              </label>
            `,
              )
              .join("")}
          </div>
        </div>
      </form>
    `,
    showConfirm: true,
    confirmText: isEdit ? "Save" : "Create",
    onConfirm: async () => {
      const username = (
        document.getElementById("admin-username") as HTMLInputElement
      ).value;
      const email = (document.getElementById("admin-email") as HTMLInputElement)
        .value;
      const password = (
        document.getElementById("admin-password") as HTMLInputElement
      ).value;
      const superAdmin = (
        document.getElementById("admin-super") as HTMLInputElement
      ).checked;
      const facilityCheckboxes = document.querySelectorAll<HTMLInputElement>(
        'input[name="facilities"]:checked',
      );
      const facilityIds = Array.from(facilityCheckboxes).map((cb) => cb.value);

      if (!username || !email) {
        showAlert("Please fill in all required fields", "error");
        throw new Error("Validation failed");
      }

      if (!isEdit && !password) {
        showAlert("Password is required for new admins", "error");
        throw new Error("Validation failed");
      }

      const payload = {
        username,
        email,
        password: password || undefined,
        superAdmin,
        facilityIds: superAdmin ? [] : facilityIds,
      };

      const response = isEdit
        ? await put<AdminUser>(`/api/admin/admins/${admin.id}`, payload)
        : await post<AdminUser>("/api/admin/admins", payload);

      if (response.error) {
        showAlert(response.error, "error");
        throw new Error(response.error);
      }

      showAlert(
        `Admin ${isEdit ? "updated" : "created"} successfully`,
        "success",
      );

      // Refresh the list
      const adminsRes = await get<AdminListResponse>("/api/admin/admins");
      if (adminsRes.data) {
        allAdmins = adminsRes.data.admins;
        renderAdminsTable();
      }
    },
  });

  // Toggle facilities visibility based on super admin checkbox
  const superCheckbox = document.getElementById(
    "admin-super",
  ) as HTMLInputElement;
  const facilitiesGroup = document.getElementById(
    "facilities-group",
  ) as HTMLDivElement;

  function updateFacilitiesVisibility() {
    facilitiesGroup.style.display = superCheckbox.checked ? "none" : "block";
  }

  superCheckbox.addEventListener("change", updateFacilitiesVisibility);
  updateFacilitiesVisibility();
}

function showDeleteModal(id: number, username: string) {
  showModal({
    title: "Delete Admin",
    content: `<p>Are you sure you want to delete admin <strong>${username}</strong>?</p><p>This action cannot be undone.</p>`,
    showConfirm: true,
    confirmText: "Delete",
    onConfirm: async () => {
      const response = await del<void>(`/api/admin/admins/${id}`);

      if (response.error) {
        showAlert(response.error, "error");
        throw new Error(response.error);
      }

      showAlert("Admin deleted successfully", "success");
      allAdmins = allAdmins.filter((a) => a.id !== id);
      renderAdminsTable();
    },
  });
}
