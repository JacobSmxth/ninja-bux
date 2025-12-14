(function(){const e=document.createElement("link").relList;if(e&&e.supports&&e.supports("modulepreload"))return;for(const a of document.querySelectorAll('link[rel="modulepreload"]'))n(a);new MutationObserver(a=>{for(const o of a)if(o.type==="childList")for(const s of o.addedNodes)s.tagName==="LINK"&&s.rel==="modulepreload"&&n(s)}).observe(document,{childList:!0,subtree:!0});function i(a){const o={};return a.integrity&&(o.integrity=a.integrity),a.referrerPolicy&&(o.referrerPolicy=a.referrerPolicy),a.crossOrigin==="use-credentials"?o.credentials="include":a.crossOrigin==="anonymous"?o.credentials="omit":o.credentials="same-origin",o}function n(a){if(a.ep)return;a.ep=!0;const o=i(a);fetch(a.href,o)}})();const g={token:localStorage.getItem("token"),adminId:localStorage.getItem("adminId")?Number(localStorage.getItem("adminId")):null,username:localStorage.getItem("username"),facilities:JSON.parse(localStorage.getItem("facilities")||"[]"),currentFacilityId:localStorage.getItem("currentFacilityId")};function M(){return g}function k(t){Object.assign(g,t),t.token!==void 0&&(t.token?localStorage.setItem("token",t.token):localStorage.removeItem("token")),t.adminId!==void 0&&(t.adminId?localStorage.setItem("adminId",String(t.adminId)):localStorage.removeItem("adminId")),t.username!==void 0&&(t.username?localStorage.setItem("username",t.username):localStorage.removeItem("username")),t.facilities!==void 0&&localStorage.setItem("facilities",JSON.stringify(t.facilities)),t.currentFacilityId!==void 0&&(t.currentFacilityId?localStorage.setItem("currentFacilityId",t.currentFacilityId):localStorage.removeItem("currentFacilityId"))}function x(){return!!g.token}function d(){var t;return g.currentFacilityId||((t=g.facilities[0])==null?void 0:t.id)||""}function R(){k({token:null,adminId:null,username:null,facilities:[],currentFacilityId:null}),window.location.hash="#/login"}const A={};function y(t,e){A[t]=e}function P(t){window.location.hash=t}async function F(){const e=(window.location.hash.slice(1)||"/login").split("?")[0];if(e!=="/login"&&!x()){P("/login");return}if(e==="/login"&&x()){P("/dashboard");return}const i=A[e]||A["/dashboard"];i&&await i()}function U(){window.addEventListener("hashchange",F),F()}async function E(t,e={}){const i=M(),n={"Content-Type":"application/json",...e.headers};i.token&&(n.Authorization=`Bearer ${i.token}`);try{const a=await fetch(t,{...e,headers:n});return a.status===401?(localStorage.removeItem("token"),window.location.hash="#/login",{error:"Unauthorized"}):a.ok?a.status===204?{data:void 0}:{data:await a.json()}:{error:(await a.json().catch(()=>({}))).error||`HTTP ${a.status}`}}catch(a){return{error:String(a)}}}async function v(t){return E(t,{method:"GET"})}async function D(t,e){return E(t,{method:"POST",body:e?JSON.stringify(e):void 0})}async function L(t,e){return E(t,{method:"PUT",body:e?JSON.stringify(e):void 0})}async function J(t){return E(t,{method:"DELETE"})}async function z(){const t=document.getElementById("app");t.innerHTML=`
    <div class="login-body">
      <div class="login-container">
        <div class="login-card">
          <h1 class="login-title">NinjaBux</h1>
          <p class="login-subtitle">Admin Portal</p>
          <form id="login-form" class="login-form">
            <div class="form-group">
              <label for="username">Username</label>
              <input type="text" id="username" name="username" required autocomplete="username">
            </div>
            <div class="form-group">
              <label for="password">Password</label>
              <input type="password" id="password" name="password" required autocomplete="current-password">
            </div>
            <div id="login-error" class="alert alert-error" style="display: none;"></div>
            <button type="submit" class="btn btn-primary btn-block">Login</button>
          </form>
        </div>
      </div>
    </div>
  `;const e=document.getElementById("login-form"),i=document.getElementById("login-error");e.addEventListener("submit",async n=>{var b;n.preventDefault(),i.style.display="none";const a=document.getElementById("username").value,o=document.getElementById("password").value,s=e.querySelector('button[type="submit"]');s.disabled=!0,s.textContent="Logging in...";const r=await D("/api/auth/login",{username:a,password:o});if(r.error){i.textContent=r.error==="HTTP 401"?"Invalid credentials":r.error,i.style.display="block",s.disabled=!1,s.textContent="Login";return}const{token:m,adminId:c,username:h,facilities:p}=r.data;k({token:m,adminId:c,username:h,facilities:p,currentFacilityId:((b=p[0])==null?void 0:b.id)||null}),P("/dashboard")})}function w(){const t=M(),e=d(),i=t.facilities.find(a=>a.id===e),n=window.location.hash.slice(1)||"/dashboard";return`
    <nav class="navbar">
      <div class="nav-container">
        <div class="nav-left">
          <span class="navbar-brand">Ninja<span>Bux</span></span>
          <nav class="navbar-nav">
            <a href="#/dashboard" class="${n==="/dashboard"?"active":""}">Dashboard</a>
            <a href="#/ninjas" class="${n==="/ninjas"?"active":""}">Ninjas</a>
            <a href="#/shop" class="${n==="/shop"?"active":""}">Shop</a>
            <a href="#/purchases" class="${n==="/purchases"?"active":""}">Purchases</a>
          </nav>
        </div>
        <div class="navbar-right">
          ${t.facilities.length>1?`
            <select id="facility-select" class="facility-switcher">
              ${t.facilities.map(a=>`
                <option value="${a.id}" ${a.id===e?"selected":""}>${a.name}</option>
              `).join("")}
            </select>
          `:`
            <span class="navbar-user">${(i==null?void 0:i.name)||""}</span>
          `}
          <span class="navbar-user">${t.username}</span>
          <button id="logout-btn" class="btn-logout">Logout</button>
        </div>
      </div>
    </nav>
  `}function B(){const t=document.getElementById("logout-btn");t&&t.addEventListener("click",()=>R());const e=document.getElementById("facility-select");e&&e.addEventListener("change",()=>{k({currentFacilityId:e.value});const i=window.location.hash;window.location.hash="",window.location.hash=i})}async function W(){var p,b,C;const t=document.getElementById("app"),e=d();t.innerHTML=`
    ${w()}
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
  `,B();const[i,n,a]=await Promise.all([v(`/api/facilities/${e}/ninjas`),v(`/api/facilities/${e}/purchases`),v(`/api/facilities/${e}/shop`)]),o=((p=i.data)==null?void 0:p.ninjas)||[],s=((b=n.data)==null?void 0:b.purchases)||[],r=((C=a.data)==null?void 0:C.items)||[],m=o.reduce((N,G)=>N+G.currentBalance,0),c=s.filter(N=>N.status==="PENDING").length,h=document.getElementById("stats-grid");h.innerHTML=`
    <div class="stat-card">
      <div class="stat-icon">N</div>
      <div class="stat-content">
        <div class="stat-value">${o.length}</div>
        <div class="stat-label">Total Ninjas</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">B</div>
      <div class="stat-content">
        <div class="stat-value">${m.toLocaleString()}</div>
        <div class="stat-label">Total Bux in Circulation</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">P</div>
      <div class="stat-content">
        <div class="stat-value">${c}</div>
        <div class="stat-label">Pending Purchases</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">S</div>
      <div class="stat-content">
        <div class="stat-value">${r.length}</div>
        <div class="stat-label">Shop Items</div>
      </div>
    </div>
  `}function T(t,e,i){var o,s;const n=document.querySelector(".modal-overlay");n&&n.remove();const a=document.createElement("div");a.className="modal-overlay",a.innerHTML=`
    <div class="modal">
      <h2>${t}</h2>
      <div class="modal-content">${e}</div>
      <div class="modal-actions">
        <button class="btn btn-secondary modal-cancel">Cancel</button>
        
      </div>
    </div>
  `,document.body.appendChild(a),(o=a.querySelector(".modal-cancel"))==null||o.addEventListener("click",()=>a.remove()),(s=a.querySelector(".modal-overlay"))==null||s.addEventListener("click",r=>{r.target===a&&a.remove()})}function H(){var t;(t=document.querySelector(".modal-overlay"))==null||t.remove()}function l(t,e="success"){const i=document.querySelector(".alert-toast");i&&i.remove();const n=document.createElement("div");n.className=`alert-toast alert-${e}`,n.textContent=t,document.body.appendChild(n),setTimeout(()=>n.remove(),3e3)}let q=[];async function Y(){var n,a,o;const t=document.getElementById("app"),e=d();t.innerHTML=`
    ${w()}
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
  `,B(),q=((n=(await v(`/api/facilities/${e}/ninjas?size=500`)).data)==null?void 0:n.ninjas)||[],$(),(a=document.getElementById("search-input"))==null||a.addEventListener("input",$),(o=document.getElementById("belt-filter"))==null||o.addEventListener("change",$)}function $(){var a,o;const t=document.getElementById("ninjas-tbody"),e=((a=document.getElementById("search-input"))==null?void 0:a.value.toLowerCase())||"",i=((o=document.getElementById("belt-filter"))==null?void 0:o.value)||"";let n=q;if(e&&(n=n.filter(s=>`${s.firstName} ${s.lastName}`.toLowerCase().includes(e))),i&&(n=n.filter(s=>s.courseName===i)),n.length===0){t.innerHTML='<tr><td colspan="6" class="empty">No ninjas found</td></tr>';return}t.innerHTML=n.map(s=>`
    <tr>
      <td>${s.firstName} ${s.lastName}</td>
      <td><span class="belt-badge belt-${s.courseName.toLowerCase().replace(" ","-")}">${s.courseName}</span></td>
      <td>${s.levelName}</td>
      <td class="balance">${s.currentBalance} Bux</td>
      <td>${s.lastSyncedAt?new Date(s.lastSyncedAt).toLocaleDateString():"Never"}</td>
      <td class="actions">
        <button class="btn btn-sm btn-secondary" data-action="ledger" data-student-id="${s.studentId}">Ledger</button>
        <button class="btn btn-sm btn-primary" data-action="adjust" data-student-id="${s.studentId}" data-name="${s.firstName} ${s.lastName}" data-balance="${s.currentBalance}">Adjust</button>
      </td>
    </tr>
  `).join(""),t.querySelectorAll('[data-action="adjust"]').forEach(s=>{s.addEventListener("click",()=>{const r=s.getAttribute("data-student-id"),m=s.getAttribute("data-name"),c=s.getAttribute("data-balance");K(r,m,Number(c))})}),t.querySelectorAll('[data-action="ledger"]').forEach(s=>{s.addEventListener("click",()=>{const r=s.getAttribute("data-student-id");Q(r)})})}function K(t,e,i){T("Adjust Points",`
    <div class="modal-info">
      <strong>${e}</strong><br>
      Current Balance: <strong>${i} Bux</strong>
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
  `);const a=document.querySelector(".modal-overlay").querySelector(".modal-confirm");a&&a.addEventListener("click",async o=>{o.preventDefault();const s=Number(document.getElementById("adjust-amount").value),r=document.getElementById("adjust-reason").value;if(!s||!r){l("Please fill in all fields","error");return}const m=d(),c=await D(`/api/facilities/${m}/ninjas/${t}/adjustments`,{amount:s,reason:r});if(c.error){l(c.error,"error");return}l(`Points adjusted! New balance: ${c.data.newBalance} Bux`,"success"),H();const h=q.find(p=>p.studentId===t);h&&(h.currentBalance=c.data.newBalance,$())})}async function Q(t){const e=d(),i=await v(`/api/facilities/${e}/ninjas/${t}/ledger?limit=20`);if(i.error){l("Failed to load ledger","error");return}const{transactions:n,currentBalance:a}=i.data,o=`
    <div class="modal-info">
      Current Balance: <strong>${a} Bux</strong>
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
        ${n.length===0?'<tr><td colspan="3" class="empty">No transactions</td></tr>':""}
        ${n.map(s=>`
          <tr>
            <td>${new Date(s.createdAt).toLocaleDateString()}</td>
            <td>${s.description}</td>
            <td class="${s.amount>=0?"text-success":"text-danger"}">${s.amount>=0?"+":""}${s.amount}</td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  `;T("Transaction History",o)}let f=[];async function V(){var n,a;const t=document.getElementById("app"),e=d();t.innerHTML=`
    ${w()}
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
  `,B(),(n=document.getElementById("add-item-btn"))==null||n.addEventListener("click",()=>O()),f=((a=(await v(`/api/facilities/${e}/shop`)).data)==null?void 0:a.items)||[],S()}function S(){const t=document.getElementById("shop-tbody");if(f.length===0){t.innerHTML='<tr><td colspan="5" class="empty">No shop items. Add one to get started!</td></tr>';return}t.innerHTML=f.map(e=>`
    <tr>
      <td>${e.name}</td>
      <td>${e.description||"-"}</td>
      <td class="balance">${e.price} Bux</td>
      <td>
        <span class="status-badge ${e.isAvailable?"status-active":"status-inactive"}">
          ${e.isAvailable?"Available":"Unavailable"}
        </span>
      </td>
      <td class="actions">
        <button class="btn btn-sm btn-secondary" data-action="edit" data-id="${e.id}">Edit</button>
        <button class="btn btn-sm ${e.isAvailable?"btn-warning":"btn-success"}" data-action="toggle" data-id="${e.id}">
          ${e.isAvailable?"Disable":"Enable"}
        </button>
        <button class="btn btn-sm btn-danger" data-action="delete" data-id="${e.id}">Delete</button>
      </td>
    </tr>
  `).join(""),t.querySelectorAll('[data-action="edit"]').forEach(e=>{e.addEventListener("click",()=>{const i=Number(e.getAttribute("data-id")),n=f.find(a=>a.id===i);n&&O(n)})}),t.querySelectorAll('[data-action="toggle"]').forEach(e=>{e.addEventListener("click",()=>Z(Number(e.getAttribute("data-id"))))}),t.querySelectorAll('[data-action="delete"]').forEach(e=>{e.addEventListener("click",()=>_(Number(e.getAttribute("data-id"))))})}function O(t){T(!!t?"Edit Shop Item":"Add Shop Item",`
    <form id="item-form">
      <div class="form-group">
        <label for="item-name">Name</label>
        <input type="text" id="item-name" value="${(t==null?void 0:t.name)||""}" required>
      </div>
      <div class="form-group">
        <label for="item-description">Description</label>
        <input type="text" id="item-description" value="${(t==null?void 0:t.description)||""}">
      </div>
      <div class="form-group">
        <label for="item-price">Price (Bux)</label>
        <input type="number" id="item-price" value="${(t==null?void 0:t.price)||""}" min="1" required>
      </div>
      <div class="form-group checkbox-group">
        <label>
          <input type="checkbox" id="item-available" ${(t==null?void 0:t.isAvailable)!==!1?"checked":""}>
          Available for purchase
        </label>
      </div>
    </form>
  `);const n=document.querySelector(".modal-overlay").querySelector(".modal-confirm");n&&n.addEventListener("click",async a=>{a.preventDefault(),await X(t==null?void 0:t.id)})}async function X(t){var c;const e=document.getElementById("item-name").value,i=document.getElementById("item-description").value,n=Number(document.getElementById("item-price").value),a=document.getElementById("item-available").checked;if(!e||!n){l("Please fill in required fields","error");return}const o=d(),s={name:e,description:i,price:n,isAvailable:a};let r;if(t?r=await L(`/api/facilities/${o}/shop/${t}`,s):r=await D(`/api/facilities/${o}/shop`,s),r.error){l(r.error,"error");return}l(t?"Item updated!":"Item created!","success"),H(),f=((c=(await v(`/api/facilities/${o}/shop`)).data)==null?void 0:c.items)||[],S()}async function Z(t){const e=f.find(a=>a.id===t);if(!e)return;const i=d(),n=await L(`/api/facilities/${i}/shop/${t}`,{name:e.name,description:e.description,price:e.price,isAvailable:!e.isAvailable});if(n.error){l(n.error,"error");return}e.isAvailable=!e.isAvailable,l(`Item ${e.isAvailable?"enabled":"disabled"}!`,"success"),S()}async function _(t){if(!confirm("Are you sure you want to delete this item?"))return;const e=d(),i=await J(`/api/facilities/${e}/shop/${t}`);if(i.error){l(i.error,"error");return}f=f.filter(n=>n.id!==t),l("Item deleted!","success"),S()}let j=[],u="PENDING";async function tt(){const t=document.getElementById("app");t.innerHTML=`
    ${w()}
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>Purchases</h1>
        </div>
        <div class="tabs">
          <button class="tab ${u==="PENDING"?"active":""}" data-status="PENDING">Pending</button>
          <button class="tab ${u==="FULFILLED"?"active":""}" data-status="FULFILLED">Fulfilled</button>
          <button class="tab ${u==="CANCELLED"?"active":""}" data-status="CANCELLED">Cancelled</button>
          <button class="tab ${u===""?"active":""}" data-status="">All</button>
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
  `,B(),document.querySelectorAll(".tab").forEach(e=>{e.addEventListener("click",()=>{u=e.getAttribute("data-status")||"",document.querySelectorAll(".tab").forEach(i=>i.classList.remove("active")),e.classList.add("active"),I()})}),await I()}async function I(){var n;const t=d(),e=u?`/api/facilities/${t}/purchases?status=${u}`:`/api/facilities/${t}/purchases`;j=((n=(await v(e)).data)==null?void 0:n.purchases)||[],et()}function et(){const t=document.getElementById("purchases-tbody");if(j.length===0){t.innerHTML=`<tr><td colspan="6" class="empty">No ${(u==null?void 0:u.toLowerCase())||""} purchases</td></tr>`;return}t.innerHTML=j.map(e=>`
    <tr>
      <td>${e.ninjaName||e.studentId}</td>
      <td>${e.itemName}</td>
      <td class="balance">${e.price} Bux</td>
      <td>
        <span class="status-badge status-${e.status.toLowerCase()}">
          ${e.status}
        </span>
      </td>
      <td>${new Date(e.purchasedAt).toLocaleString()}</td>
      <td class="actions">
        ${e.status==="PENDING"?`
          <button class="btn btn-sm btn-success" data-action="fulfill" data-id="${e.id}">Fulfill</button>
          <button class="btn btn-sm btn-danger" data-action="cancel" data-id="${e.id}">Cancel</button>
        `:e.fulfilledAt?`
          <span class="text-muted">Fulfilled ${new Date(e.fulfilledAt).toLocaleDateString()}</span>
        `:"-"}
      </td>
    </tr>
  `).join(""),t.querySelectorAll('[data-action="fulfill"]').forEach(e=>{e.addEventListener("click",()=>at(Number(e.getAttribute("data-id"))))}),t.querySelectorAll('[data-action="cancel"]').forEach(e=>{e.addEventListener("click",()=>nt(Number(e.getAttribute("data-id"))))})}async function at(t){const e=d(),i=await L(`/api/facilities/${e}/purchases/${t}/fulfill`);if(i.error){l(i.error,"error");return}l("Purchase fulfilled!","success"),await I()}async function nt(t){if(!confirm("Cancel this purchase and refund points?"))return;const e=d(),i=await L(`/api/facilities/${e}/purchases/${t}/cancel`);if(i.error){l(i.error,"error");return}l("Purchase cancelled and points refunded!","success"),await I()}y("/login",z);y("/dashboard",W);y("/ninjas",Y);y("/shop",V);y("/purchases",tt);U();
