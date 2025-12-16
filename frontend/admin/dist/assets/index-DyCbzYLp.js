(function(){const e=document.createElement("link").relList;if(e&&e.supports&&e.supports("modulepreload"))return;for(const i of document.querySelectorAll('link[rel="modulepreload"]'))a(i);new MutationObserver(i=>{for(const o of i)if(o.type==="childList")for(const s of o.addedNodes)s.tagName==="LINK"&&s.rel==="modulepreload"&&a(s)}).observe(document,{childList:!0,subtree:!0});function n(i){const o={};return i.integrity&&(o.integrity=i.integrity),i.referrerPolicy&&(o.referrerPolicy=i.referrerPolicy),i.crossOrigin==="use-credentials"?o.credentials="include":i.crossOrigin==="anonymous"?o.credentials="omit":o.credentials="same-origin",o}function a(i){if(i.ep)return;i.ep=!0;const o=n(i);fetch(i.href,o)}})();const w={token:localStorage.getItem("token"),adminId:localStorage.getItem("adminId")?Number(localStorage.getItem("adminId")):null,username:localStorage.getItem("username"),superAdmin:localStorage.getItem("superAdmin")==="true",facilities:JSON.parse(localStorage.getItem("facilities")||"[]"),currentFacilityId:localStorage.getItem("currentFacilityId")};function D(){return w}function M(t){Object.assign(w,t),t.token!==void 0&&(t.token?localStorage.setItem("token",t.token):localStorage.removeItem("token")),t.adminId!==void 0&&(t.adminId?localStorage.setItem("adminId",String(t.adminId)):localStorage.removeItem("adminId")),t.username!==void 0&&(t.username?localStorage.setItem("username",t.username):localStorage.removeItem("username")),t.superAdmin!==void 0&&localStorage.setItem("superAdmin",String(t.superAdmin)),t.facilities!==void 0&&localStorage.setItem("facilities",JSON.stringify(t.facilities)),t.currentFacilityId!==void 0&&(t.currentFacilityId?localStorage.setItem("currentFacilityId",t.currentFacilityId):localStorage.removeItem("currentFacilityId"))}function R(){return!!w.token}function u(){var t;return w.currentFacilityId||((t=w.facilities[0])==null?void 0:t.id)||""}function z(){M({token:null,adminId:null,username:null,superAdmin:!1,facilities:[],currentFacilityId:null}),localStorage.removeItem("superAdmin"),window.location.hash="#/login"}const x={};function I(t,e){x[t]=e}function N(t){window.location.hash=t}async function G(){const e=(window.location.hash.slice(1)||"/login").split("?")[0];if(e!=="/login"&&!R()){N("/login");return}if(e==="/login"&&R()){N("/dashboard");return}const n=x[e]||x["/dashboard"];n&&await n()}function W(){window.addEventListener("hashchange",G),G()}async function j(t,e={}){const n=D(),a={"Content-Type":"application/json",...e.headers};n.token&&(a.Authorization=`Bearer ${n.token}`);try{const i=await fetch(t,{...e,headers:a});return i.status===401?(localStorage.removeItem("token"),window.location.hash="#/login",{error:"Unauthorized"}):i.ok?i.status===204?{data:void 0}:{data:await i.json()}:{error:(await i.json().catch(()=>({}))).error||`HTTP ${i.status}`}}catch(i){return{error:String(i)}}}async function m(t){return j(t,{method:"GET"})}async function C(t,e){return j(t,{method:"POST",body:e?JSON.stringify(e):void 0})}async function A(t,e){return j(t,{method:"PUT",body:e?JSON.stringify(e):void 0})}async function O(t){return j(t,{method:"DELETE"})}async function Y(){const t=document.getElementById("app");t.innerHTML=`
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
  `;const e=document.getElementById("login-form"),n=document.getElementById("login-error");e.addEventListener("submit",async a=>{var f;a.preventDefault(),n.style.display="none";const i=document.getElementById("username").value,o=document.getElementById("password").value,s=e.querySelector('button[type="submit"]');s.disabled=!0,s.textContent="Logging in...";const r=await C("/api/auth/login",{username:i,password:o});if(r.error){n.textContent=r.error==="HTTP 401"?"Invalid credentials":r.error,n.style.display="block",s.disabled=!1,s.textContent="Login";return}const{token:l,adminId:c,username:h,superAdmin:y,facilities:v}=r.data;M({token:l,adminId:c,username:h,superAdmin:y,facilities:v,currentFacilityId:((f=v[0])==null?void 0:f.id)||null}),N("/dashboard")})}function E(){const t=D(),e=u(),n=t.facilities.find(i=>i.id===e),a=window.location.hash.slice(1)||"/dashboard";return`
    <nav class="navbar">
      <div class="nav-container">
        <div class="nav-left">
          <img src="/CodeNinjasLogo.svg" alt="Code Ninjas" class="app-logo" />
          <span class="app-title">NinjaBux</span>
          <nav class="navbar-nav">
            <a href="#/dashboard" class="${a==="/dashboard"?"active":""}">Dashboard</a>
            <a href="#/ninjas" class="${a==="/ninjas"?"active":""}">Ninjas</a>
            <a href="#/shop" class="${a==="/shop"?"active":""}">Shop</a>
            <a href="#/purchases" class="${a==="/purchases"?"active":""}">Purchases</a>
            ${t.superAdmin?`<a href="#/admins" class="${a==="/admins"?"active":""}">Admins</a>`:""}
          </nav>
        </div>
        <div class="navbar-right">
          ${t.facilities.length>1?`
            <select id="facility-select" class="facility-switcher">
              ${t.facilities.map(i=>`
                <option value="${i.id}" ${i.id===e?"selected":""}>${i.name}</option>
              `).join("")}
            </select>
          `:`
            <span class="navbar-user">${(n==null?void 0:n.name)||""}</span>
          `}
          <span class="navbar-user">${t.username}</span>
          <button id="logout-btn" class="btn-logout">Logout</button>
        </div>
      </div>
    </nav>
  `}function L(){const t=document.getElementById("logout-btn");t&&t.addEventListener("click",()=>z());const e=document.getElementById("facility-select");e&&e.addEventListener("change",()=>{M({currentFacilityId:e.value});const n=window.location.hash;window.location.hash="",window.location.hash=n})}async function K(){var y,v,f;const t=document.getElementById("app"),e=u();t.innerHTML=`
    ${E()}
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
  `,L();const[n,a,i]=await Promise.all([m(`/api/facilities/${e}/ninjas`),m(`/api/facilities/${e}/purchases`),m(`/api/facilities/${e}/shop`)]),o=((y=n.data)==null?void 0:y.ninjas)||[],s=((v=a.data)==null?void 0:v.purchases)||[],r=((f=i.data)==null?void 0:f.items)||[],l=o.reduce(($,T)=>$+T.currentBalance,0),c=s.filter($=>$.status==="PENDING").length,h=document.getElementById("stats-grid");h.innerHTML=`
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
        <div class="stat-value">${l.toLocaleString()}</div>
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
  `}function B(t,e,n){var l,c;const a=typeof t=="string"?{title:t,content:e,onConfirm:n}:t,i=document.querySelector(".modal-overlay");i&&i.remove();const o=a.showConfirm??!!a.onConfirm,s=a.confirmText??"Confirm",r=document.createElement("div");r.className="modal-overlay",r.innerHTML=`
    <div class="modal">
      <h2>${a.title}</h2>
      <div class="modal-content">${a.content}</div>
      <div class="modal-actions">
        <button class="btn btn-secondary modal-cancel">Cancel</button>
        ${o?`<button class="btn btn-primary modal-confirm">${s}</button>`:""}
      </div>
    </div>
  `,document.body.appendChild(r),(l=r.querySelector(".modal-cancel"))==null||l.addEventListener("click",()=>r.remove()),r.addEventListener("click",h=>{h.target===r&&r.remove()}),a.onConfirm&&((c=r.querySelector(".modal-confirm"))==null||c.addEventListener("click",async()=>{try{await a.onConfirm(),r.remove()}catch{}}))}function Q(){var t;(t=document.querySelector(".modal-overlay"))==null||t.remove()}function d(t,e="success"){const n=document.querySelector(".alert-toast");n&&n.remove();const a=document.createElement("div");a.className=`alert-toast alert-${e}`,a.textContent=t,document.body.appendChild(a),setTimeout(()=>a.remove(),3e3)}let F=[];async function X(){var a,i,o;const t=document.getElementById("app"),e=u();t.innerHTML=`
    ${E()}
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
  `,L(),F=((a=(await m(`/api/facilities/${e}/ninjas?size=500`)).data)==null?void 0:a.ninjas)||[],S(),(i=document.getElementById("search-input"))==null||i.addEventListener("input",S),(o=document.getElementById("belt-filter"))==null||o.addEventListener("change",S)}function S(){var i,o;const t=document.getElementById("ninjas-tbody"),e=((i=document.getElementById("search-input"))==null?void 0:i.value.toLowerCase())||"",n=((o=document.getElementById("belt-filter"))==null?void 0:o.value)||"";let a=F;if(e&&(a=a.filter(s=>`${s.firstName} ${s.lastName}`.toLowerCase().includes(e))),n&&(a=a.filter(s=>s.courseName===n)),a.length===0){t.innerHTML='<tr><td colspan="6" class="empty">No ninjas found</td></tr>';return}t.innerHTML=a.map(s=>`
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
  `).join(""),t.querySelectorAll('[data-action="adjust"]').forEach(s=>{s.addEventListener("click",()=>{const r=s.getAttribute("data-student-id"),l=s.getAttribute("data-name"),c=s.getAttribute("data-balance");Z(r,l,Number(c))})}),t.querySelectorAll('[data-action="ledger"]').forEach(s=>{s.addEventListener("click",()=>{const r=s.getAttribute("data-student-id");_(r)})})}function Z(t,e,n){B({title:"Adjust Bux",content:`
      <div class="modal-info">
        <strong>${e}</strong><br>
        Current Balance: <strong>${n} Bux</strong>
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
    `,showConfirm:!0,confirmText:"Adjust",onConfirm:async()=>{const a=Number(document.getElementById("adjust-amount").value),i=document.getElementById("adjust-reason").value;if(!a||!i)throw d("Please fill in all fields","error"),new Error("Validation failed");const o=u(),s=await C(`/api/facilities/${o}/ninjas/${t}/adjustments`,{amount:a,reason:i});if(s.error)throw d(s.error,"error"),new Error(s.error);d(`Bux adjusted! New balance: ${s.data.newBalance} Bux`,"success");const r=F.find(l=>l.studentId===t);r&&(r.currentBalance=s.data.newBalance,S())}})}async function _(t){const e=u(),n=await m(`/api/facilities/${e}/ninjas/${t}/ledger?limit=20`);if(n.error){d("Failed to load ledger","error");return}const{transactions:a,currentBalance:i}=n.data,o=`
    <div class="modal-info">
      Current Balance: <strong>${i} Bux</strong>
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
        ${a.length===0?'<tr><td colspan="3" class="empty">No transactions</td></tr>':""}
        ${a.map(s=>`
          <tr>
            <td>${new Date(s.createdAt).toLocaleDateString()}</td>
            <td>${s.description}</td>
            <td class="${s.amount>=0?"text-success":"text-danger"}">${s.amount>=0?"+":""}${s.amount}</td>
          </tr>
        `).join("")}
      </tbody>
    </table>
  `;B("Transaction History",o)}let b=[];async function tt(){var a,i;const t=document.getElementById("app"),e=u();t.innerHTML=`
    ${E()}
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
  `,L(),(a=document.getElementById("add-item-btn"))==null||a.addEventListener("click",()=>U()),b=((i=(await m(`/api/facilities/${e}/shop`)).data)==null?void 0:i.items)||[],P()}function P(){const t=document.getElementById("shop-tbody");if(b.length===0){t.innerHTML='<tr><td colspan="5" class="empty">No shop items. Add one to get started!</td></tr>';return}t.innerHTML=b.map(e=>`
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
  `).join(""),t.querySelectorAll('[data-action="edit"]').forEach(e=>{e.addEventListener("click",()=>{const n=Number(e.getAttribute("data-id")),a=b.find(i=>i.id===n);a&&U(a)})}),t.querySelectorAll('[data-action="toggle"]').forEach(e=>{e.addEventListener("click",()=>at(Number(e.getAttribute("data-id"))))}),t.querySelectorAll('[data-action="delete"]').forEach(e=>{e.addEventListener("click",()=>nt(Number(e.getAttribute("data-id"))))})}function U(t){B(!!t?"Edit Shop Item":"Add Shop Item",`
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
  `);const a=document.querySelector(".modal-overlay").querySelector(".modal-confirm");a&&a.addEventListener("click",async i=>{i.preventDefault(),await et(t==null?void 0:t.id)})}async function et(t){var c;const e=document.getElementById("item-name").value,n=document.getElementById("item-description").value,a=Number(document.getElementById("item-price").value),i=document.getElementById("item-available").checked;if(!e||!a){d("Please fill in required fields","error");return}const o=u(),s={name:e,description:n,price:a,isAvailable:i};let r;if(t?r=await A(`/api/facilities/${o}/shop/${t}`,s):r=await C(`/api/facilities/${o}/shop`,s),r.error){d(r.error,"error");return}d(t?"Item updated!":"Item created!","success"),Q(),b=((c=(await m(`/api/facilities/${o}/shop`)).data)==null?void 0:c.items)||[],P()}async function at(t){const e=b.find(i=>i.id===t);if(!e)return;const n=u(),a=await A(`/api/facilities/${n}/shop/${t}`,{name:e.name,description:e.description,price:e.price,isAvailable:!e.isAvailable});if(a.error){d(a.error,"error");return}e.isAvailable=!e.isAvailable,d(`Item ${e.isAvailable?"enabled":"disabled"}!`,"success"),P()}async function nt(t){if(!confirm("Are you sure you want to delete this item?"))return;const e=u(),n=await O(`/api/facilities/${e}/shop/${t}`);if(n.error){d(n.error,"error");return}b=b.filter(a=>a.id!==t),d("Item deleted!","success"),P()}let q=[],p="PENDING";async function it(){const t=document.getElementById("app");t.innerHTML=`
    ${E()}
    <main class="main-content">
      <div class="container">
        <div class="page-header">
          <h1>Purchases</h1>
        </div>
        <div class="tabs">
          <button class="tab ${p==="PENDING"?"active":""}" data-status="PENDING">Pending</button>
          <button class="tab ${p==="FULFILLED"?"active":""}" data-status="FULFILLED">Fulfilled</button>
          <button class="tab ${p==="CANCELLED"?"active":""}" data-status="CANCELLED">Cancelled</button>
          <button class="tab ${p===""?"active":""}" data-status="">All</button>
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
  `,L(),document.querySelectorAll(".tab").forEach(e=>{e.addEventListener("click",()=>{p=e.getAttribute("data-status")||"",document.querySelectorAll(".tab").forEach(n=>n.classList.remove("active")),e.classList.add("active"),k()})}),await k()}async function k(){var a;const t=u(),e=p?`/api/facilities/${t}/purchases?status=${p}`:`/api/facilities/${t}/purchases`;q=((a=(await m(e)).data)==null?void 0:a.purchases)||[],st()}function st(){const t=document.getElementById("purchases-tbody");if(q.length===0){t.innerHTML=`<tr><td colspan="6" class="empty">No ${(p==null?void 0:p.toLowerCase())||""} purchases</td></tr>`;return}t.innerHTML=q.map(e=>`
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
  `).join(""),t.querySelectorAll('[data-action="fulfill"]').forEach(e=>{e.addEventListener("click",()=>ot(Number(e.getAttribute("data-id"))))}),t.querySelectorAll('[data-action="cancel"]').forEach(e=>{e.addEventListener("click",()=>rt(Number(e.getAttribute("data-id"))))})}async function ot(t){const e=u(),n=await A(`/api/facilities/${e}/purchases/${t}/fulfill`);if(n.error){d(n.error,"error");return}d("Purchase fulfilled!","success"),await k()}async function rt(t){if(!confirm("Cancel this purchase and refund points?"))return;const e=u(),n=await A(`/api/facilities/${e}/purchases/${t}/cancel`);if(n.error){d(n.error,"error");return}d("Purchase cancelled and points refunded!","success"),await k()}let g=[],V=[];async function dt(){var i,o;if(!D().superAdmin){N("/dashboard");return}const e=document.getElementById("app");e.innerHTML=`
    ${E()}
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
  `,L();const[n,a]=await Promise.all([m("/api/admin/admins"),m("/api/admin/facilities")]);if(n.error){d("Failed to load admins: "+n.error,"error");return}g=((i=n.data)==null?void 0:i.admins)||[],V=a.data||[],H(),(o=document.getElementById("add-admin-btn"))==null||o.addEventListener("click",()=>{J()})}function H(){const t=document.getElementById("admins-tbody");if(g.length===0){t.innerHTML='<tr><td colspan="6" class="empty">No admins found</td></tr>';return}t.innerHTML=g.map(e=>`
    <tr>
      <td>${e.username}</td>
      <td>${e.email}</td>
      <td><span class="badge ${e.superAdmin?"badge-primary":"badge-secondary"}">${e.superAdmin?"Super Admin":"Admin"}</span></td>
      <td>${e.superAdmin?"All":e.facilities.map(n=>n.name).join(", ")||"None"}</td>
      <td>${new Date(e.createdAt).toLocaleDateString()}</td>
      <td class="actions">
        <button class="btn btn-sm btn-secondary" data-action="edit" data-id="${e.id}">Edit</button>
        <button class="btn btn-sm btn-danger" data-action="delete" data-id="${e.id}" data-username="${e.username}">Delete</button>
      </td>
    </tr>
  `).join(""),t.querySelectorAll('[data-action="edit"]').forEach(e=>{e.addEventListener("click",()=>{const n=Number(e.getAttribute("data-id")),a=g.find(i=>i.id===n);a&&J(a)})}),t.querySelectorAll('[data-action="delete"]').forEach(e=>{e.addEventListener("click",()=>{const n=Number(e.getAttribute("data-id")),a=e.getAttribute("data-username");lt(n,a)})})}function J(t){const e=!!t;B({title:e?"Edit Admin":"Create Admin",content:`
      <form id="admin-form">
        <div class="form-group">
          <label for="admin-username">Username</label>
          <input type="text" id="admin-username" required minlength="3" value="${(t==null?void 0:t.username)||""}">
        </div>
        <div class="form-group">
          <label for="admin-email">Email</label>
          <input type="email" id="admin-email" required value="${(t==null?void 0:t.email)||""}">
        </div>
        <div class="form-group">
          <label for="admin-password">Password ${e?"(leave blank to keep current)":""}</label>
          <input type="password" id="admin-password" ${e?"":"required"} minlength="6">
        </div>
        <div class="form-group">
          <label>
            <input type="checkbox" id="admin-super" ${t!=null&&t.superAdmin?"checked":""}>
            Super Admin (access to all facilities)
          </label>
        </div>
        <div class="form-group" id="facilities-group">
          <label>Facilities</label>
          <div class="checkbox-group">
            ${V.map(s=>`
              <label class="checkbox-label">
                <input type="checkbox" name="facilities" value="${s.id}"
                  ${t!=null&&t.facilities.some(r=>r.id===s.id)?"checked":""}>
                ${s.name}
              </label>
            `).join("")}
          </div>
        </div>
      </form>
    `,showConfirm:!0,confirmText:e?"Save":"Create",onConfirm:async()=>{const s=document.getElementById("admin-username").value,r=document.getElementById("admin-email").value,l=document.getElementById("admin-password").value,c=document.getElementById("admin-super").checked,h=document.querySelectorAll('input[name="facilities"]:checked'),y=Array.from(h).map(T=>T.value);if(!s||!r)throw d("Please fill in all required fields","error"),new Error("Validation failed");if(!e&&!l)throw d("Password is required for new admins","error"),new Error("Validation failed");const v={username:s,email:r,password:l||void 0,superAdmin:c,facilityIds:c?[]:y},f=e?await A(`/api/admin/admins/${t.id}`,v):await C("/api/admin/admins",v);if(f.error)throw d(f.error,"error"),new Error(f.error);d(`Admin ${e?"updated":"created"} successfully`,"success");const $=await m("/api/admin/admins");$.data&&(g=$.data.admins,H())}});const a=document.getElementById("admin-super"),i=document.getElementById("facilities-group");function o(){i.style.display=a.checked?"none":"block"}a.addEventListener("change",o),o()}function lt(t,e){B({title:"Delete Admin",content:`<p>Are you sure you want to delete admin <strong>${e}</strong>?</p><p>This action cannot be undone.</p>`,showConfirm:!0,confirmText:"Delete",onConfirm:async()=>{const n=await O(`/api/admin/admins/${t}`);if(n.error)throw d(n.error,"error"),new Error(n.error);d("Admin deleted successfully","success"),g=g.filter(a=>a.id!==t),H()}})}I("/login",Y);I("/dashboard",K);I("/ninjas",X);I("/shop",tt);I("/purchases",it);I("/admins",dt);W();
