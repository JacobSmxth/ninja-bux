(function(){const a=document.createElement("link").relList;if(a&&a.supports&&a.supports("modulepreload"))return;for(const s of document.querySelectorAll('link[rel="modulepreload"]'))n(s);new MutationObserver(s=>{for(const r of s)if(r.type==="childList")for(const c of r.addedNodes)c.tagName==="LINK"&&c.rel==="modulepreload"&&n(c)}).observe(document,{childList:!0,subtree:!0});function t(s){const r={};return s.integrity&&(r.integrity=s.integrity),s.referrerPolicy&&(r.referrerPolicy=s.referrerPolicy),s.crossOrigin==="use-credentials"?r.credentials="include":s.crossOrigin==="anonymous"?r.credentials="omit":r.credentials="same-origin",r}function n(s){if(s.ep)return;s.ep=!0;const r=t(s);fetch(s.href,r)}})();const g=[];function h(e,a){const t=[],n=e.replace(/:([^/]+)/g,(s,r)=>(t.push(r),"([^/]+)"));g.push({pattern:new RegExp(`^${n}$`),handler:a,paramNames:t})}function m(e){window.location.hash=e}function b(){const e=window.location.hash.slice(1)||"/";for(const{pattern:t,handler:n,paramNames:s}of g){const r=e.match(t);if(r){const c={};s.forEach((i,o)=>{c[i]=r[o+1]}),n(c);return}}const a=g.find(t=>t.pattern.test("/"));a&&a.handler({})}function H(){window.addEventListener("hashchange",b),window.addEventListener("load",b),b()}const T="/api";async function l(e){try{const a=await fetch(`${T}${e}`);return a.ok?{data:await a.json()}:{error:await a.text()||`HTTP ${a.status}`}}catch(a){return{error:String(a)}}}async function M(e,a){try{const t=await fetch(`${T}${e}`,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(a)});return t.ok?{data:await t.json()}:{error:await t.text()||`HTTP ${t.status}`}}catch(t){return{error:String(t)}}}const v={facilityId:sessionStorage.getItem("facilityId")||"fcd4728c-afff-4a3c-8a39-05d2cd9d87ac",studentId:sessionStorage.getItem("studentId"),currentNinja:null};function u(){return v}function B(e){Object.assign(v,e),e.facilityId!==void 0&&sessionStorage.setItem("facilityId",e.facilityId),e.studentId!==void 0&&(e.studentId?sessionStorage.setItem("studentId",e.studentId):sessionStorage.removeItem("studentId"))}function y(e){v.currentNinja=e}function x(){B({studentId:null,currentNinja:null})}function $(){return v.studentId!==null}function k(e){const a=e.toLowerCase();return a.includes("white")?"belt-white":a.includes("yellow")?"belt-yellow":a.includes("orange")?"belt-orange":a.includes("green")?"belt-green":a.includes("blue")?"belt-blue":a.includes("purple")?"belt-purple":a.includes("red")?"belt-red":a.includes("brown")?"belt-brown":(a.includes("black"),"belt-black")}async function P(){const e=document.getElementById("app"),{facilityId:a}=u();e.innerHTML=`
    <div class="login-container">
      <div class="login-card">
        <header class="login-header">
          <img src="/CodeNinjasLogo.svg" alt="Code Ninjas" class="login-logo" />
          <h1 class="login-title">NinjaBux</h1>
          <p class="login-subtitle">Select your profile to continue</p>
        </header>

        <div class="search-container">
          <input
            type="text"
            id="ninja-search"
            class="search-input"
            placeholder="Search by name..."
          />
        </div>

        <div id="ninja-grid" class="ninja-grid">
          <div class="loading">Loading ninjas...</div>
        </div>
      </div>
    </div>
  `;const t=await l(`/facilities/${a}/ninjas`),n=document.getElementById("ninja-grid");if(t.error){n.innerHTML=`<div class="error">Failed to load ninjas: ${t.error}</div>`;return}const s=t.data.ninjas;function r(i){if(i.length===0){n.innerHTML='<div class="empty">No ninjas found</div>';return}n.innerHTML=i.map(o=>`
      <div class="ninja-card" data-student-id="${o.studentId}">
        <div class="ninja-avatar">${o.firstName.charAt(0)}${o.lastName.charAt(0)}</div>
        <div class="ninja-name">${o.firstName} ${o.lastName}</div>
        <div class="ninja-belt belt-badge ${k(o.courseName)}">${o.courseName}</div>
        <div class="ninja-balance">${o.currentBalance} Bux</div>
      </div>
    `).join(""),n.querySelectorAll(".ninja-card").forEach(o=>{o.addEventListener("click",()=>{const p=o.getAttribute("data-student-id"),C=i.find(A=>A.studentId===p);B({studentId:p}),y(C),m("/dashboard")})})}r(s);const c=document.getElementById("ninja-search");c.addEventListener("input",()=>{const i=c.value.toLowerCase(),o=s.filter(p=>p.firstName.toLowerCase().includes(i)||p.lastName.toLowerCase().includes(i));r(o)})}function L(){const{currentNinja:e}=u(),a=(e==null?void 0:e.currentBalance)??0,t=e?`${e.firstName}`:"";return`
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
          <span class="nav-balance">${a} Bux</span>
          <span class="nav-user">${t}</span>
          <button class="btn btn-logout" id="logout-btn">Logout</button>
        </div>
      </div>
    </nav>
  `}function I(){const e=document.getElementById("logout-btn");e&&e.addEventListener("click",()=>{x(),m("/")})}function q(e){switch(e){case"INITIAL_BALANCE":return"+";case"ACTIVITY_REWARD":return"+";case"PURCHASE":return"-";case"ADJUSTMENT":return"~";default:return""}}function R(e){return e>0?"positive":e<0?"negative":"neutral"}function O(e){return new Date(e).toLocaleDateString("en-US",{month:"short",day:"numeric",hour:"numeric",minute:"2-digit"})}function D(e){return e.length===0?'<li class="empty">No recent transactions</li>':e.map(a=>`
    <li class="transaction-item ${R(a.amount)}">
      <span class="transaction-icon">${q(a.type)}</span>
      <div class="transaction-details">
        <span class="transaction-desc">${a.description}</span>
        <span class="transaction-date">${O(a.createdAt)}</span>
      </div>
      <span class="transaction-amount">${a.amount>0?"+":""}${a.amount}</span>
    </li>
  `).join("")}async function F(){const e=document.getElementById("app");if(!$()){m("/");return}const{facilityId:a,studentId:t,currentNinja:n}=u();e.innerHTML=`
    ${L()}
    <main class="dashboard-page">
      <div class="loading">Loading dashboard...</div>
    </main>
  `;const s=n?{data:n,error:void 0}:await l(`/facilities/${a}/ninjas/${t}`),r=await l(`/facilities/${a}/ninjas/${t}/ledger?limit=10`),c=e.querySelector("main");if(s.error||r.error){c.innerHTML='<div class="error">Failed to load dashboard</div>';return}const i=s.data;y(i);const o=r.data.transactions;c.innerHTML=`
    <div class="dashboard-container">
      <div class="dashboard">
        <header class="dashboard-header">
          <h1>Welcome, ${i.firstName}!</h1>
          <div class="ninja-info">
            <span class="belt-badge belt-${i.courseName.toLowerCase().replace(" ","-")}">${i.courseName}</span>
            <span class="level-badge">${i.levelName}</span>
          </div>
        </header>

        <div class="balance-card">
          <div class="balance-label">Your Balance</div>
          <div class="balance-amount">${i.currentBalance} <span class="balance-unit">Bux</span></div>
        </div>

        <section class="recent-activity">
          <h2>Recent Activity</h2>
          <ul class="transaction-list">
            ${D(o)}
          </ul>
        </section>

        <div class="quick-links">
          <a href="#/shop" class="btn btn-primary">Shop</a>
          <a href="#/leaderboard" class="btn btn-secondary">Leaderboard</a>
        </div>
      </div>
    </div>
  `}let d=0;function U(e){return d>=e}function S(e){const a=U(e.price),t=e.isAvailable,n=a&&t;return`
    <div class="shop-item ${n?"":"disabled"}" data-item-id="${e.id}">
      <div class="shop-item-icon">
        <span class="item-placeholder">${e.name.charAt(0)}</span>
      </div>
      <div class="shop-item-details">
        <h3 class="shop-item-name">${e.name}</h3>
        <p class="shop-item-description">${e.description}</p>
      </div>
      <div class="shop-item-footer">
        <span class="shop-item-price">${e.price} Bux</span>
        <button
          class="btn btn-buy ${n?"":"btn-disabled"}"
          ${n?"":"disabled"}
          data-item-id="${e.id}"
          data-item-name="${e.name}"
          data-item-price="${e.price}"
        >
          ${t?a?"Buy":"Not enough Bux":"Unavailable"}
        </button>
      </div>
    </div>
  `}function Y(e,a,t){const n=document.createElement("div");n.className="modal-overlay",n.innerHTML=`
    <div class="modal">
      <h2>Confirm Purchase</h2>
      <p>Are you sure you want to buy <strong>${a}</strong> for <strong>${t} Bux</strong>?</p>
      <p class="modal-balance">Your balance: ${d} Bux → ${d-t} Bux</p>
      <div class="modal-actions">
        <button class="btn btn-secondary" id="cancel-purchase">Cancel</button>
        <button class="btn btn-primary" id="confirm-purchase">Confirm</button>
      </div>
    </div>
  `,document.body.appendChild(n),n.querySelector("#cancel-purchase").addEventListener("click",()=>{n.remove()}),n.querySelector("#confirm-purchase").addEventListener("click",async()=>{const{facilityId:s,studentId:r}=u(),c=n.querySelector("#confirm-purchase");c.disabled=!0,c.textContent="Processing...";const i=await M(`/facilities/${s}/ninjas/${r}/purchases`,{shopItemId:e});if(i.error){w(`Purchase failed: ${i.error}`,"error"),n.remove();return}d=i.data.newBalance,_(),await W(),n.remove(),w(`Successfully purchased ${a}!`,"success")}),n.addEventListener("click",s=>{s.target===n&&n.remove()})}function w(e,a){const t=document.createElement("div");t.className=`toast toast-${a}`,t.textContent=e,document.body.appendChild(t),setTimeout(()=>{t.classList.add("fade-out"),setTimeout(()=>t.remove(),300)},3e3)}function _(){const e=document.querySelector(".shop-balance-amount");e&&(e.textContent=`${d} Bux`);const a=document.querySelector(".nav-balance");a&&(a.textContent=`${d} Bux`)}async function W(){const{facilityId:e}=u(),a=await l(`/facilities/${e}/shop`);if(a.data){const t=document.querySelector(".shop-grid");t&&(t.innerHTML=a.data.items.map(n=>S(n)).join(""),j())}}function j(){document.querySelectorAll(".btn-buy:not(.btn-disabled)").forEach(e=>{e.addEventListener("click",a=>{a.stopPropagation();const t=a.target,n=parseInt(t.getAttribute("data-item-id")),s=t.getAttribute("data-item-name"),r=parseInt(t.getAttribute("data-item-price"));Y(n,s,r)})})}async function J(){const e=document.getElementById("app");if(!$()){m("/");return}const{facilityId:a,studentId:t,currentNinja:n}=u();e.innerHTML=`
    ${L()}
    <main class="shop-page">
      <div class="loading">Loading shop...</div>
    </main>
  `;const s=n?{data:n,error:void 0}:await l(`/facilities/${a}/ninjas/${t}`),r=await l(`/facilities/${a}/shop`),c=e.querySelector("main");if(s.error||r.error){c.innerHTML='<div class="error">Failed to load shop</div>';return}const i=s.data;y(i),d=i.currentBalance;const o=r.data.items;c.innerHTML=`
    <div class="shop-container">
      <div class="shop">
        <header class="shop-header">
          <h1>Shop</h1>
          <div class="shop-balance">
            <span class="shop-balance-label">Your Balance:</span>
            <span class="shop-balance-amount">${d} Bux</span>
          </div>
        </header>

        <div class="shop-grid">
          ${o.length>0?o.map(p=>S(p)).join(""):'<div class="empty">No items available</div>'}
        </div>
      </div>
    </div>
  `,j()}let f="weekly";function N(e,a){if(e.length===0)return'<li class="empty">No data available</li>';const{studentId:t}=u();return e.map(n=>{const s=n.studentId===t,r=a==="earned"?n.pointsEarned:n.pointsSpent;return`
      <li class="leaderboard-entry ${s?"current-user":""}">
        <span class="entry-rank">${n.rank}</span>
        <div class="entry-info">
          <span class="entry-name">${n.ninjaName}</span>
        </div>
        <span class="entry-points">${Math.abs(r||0)} Bux</span>
      </li>
    `}).join("")}async function E(){const{facilityId:e}=u(),a=document.getElementById("earned-list"),t=document.getElementById("spent-list");a.innerHTML='<li class="loading">Loading...</li>',t.innerHTML='<li class="loading">Loading...</li>';const[n,s]=await Promise.all([l(`/facilities/${e}/leaderboard/earned?period=${f}`),l(`/facilities/${e}/leaderboard/spent?period=${f}`)]);n.error?a.innerHTML='<li class="error">Failed to load</li>':a.innerHTML=N(n.data.leaderboard,"earned"),s.error?t.innerHTML='<li class="error">Failed to load</li>':t.innerHTML=N(s.data.leaderboard,"spent")}function K(){document.querySelectorAll(".period-tab").forEach(e=>{e.addEventListener("click",async()=>{const a=e.getAttribute("data-period");a!==f&&(f=a,document.querySelectorAll(".period-tab").forEach(t=>t.classList.remove("active")),e.classList.add("active"),await E())})})}async function V(){const e=document.getElementById("app");if(!$()){m("/");return}e.innerHTML=`
    ${L()}
    <main class="leaderboard-page">
      <div class="dashboard-container">
        <div class="leaderboard">
          <header class="leaderboard-header">
            <h1>Leaderboard</h1>
            <div class="period-tabs">
              <button class="period-tab ${f==="weekly"?"active":""}" data-period="weekly">Weekly</button>
              <button class="period-tab ${f==="monthly"?"active":""}" data-period="monthly">Monthly</button>
              <button class="period-tab ${f==="yearly"?"active":""}" data-period="yearly">Yearly</button>
            </div>
          </header>

          <div class="leaderboard-columns">
            <section class="leaderboard-column">
              <h2>Top Earners</h2>
              <ul id="earned-list" class="leaderboard-list">
                <li class="loading">Loading...</li>
              </ul>
            </section>

            <section class="leaderboard-column">
              <h2>Top Spenders</h2>
              <ul id="spent-list" class="leaderboard-list">
                <li class="loading">Loading...</li>
              </ul>
            </section>
          </div>
        </div>
      </div>
    </main>
  `,K(),await E()}h("/",P);h("/dashboard",async()=>{await F(),I()});h("/shop",async()=>{await J(),I()});h("/leaderboard",async()=>{await V(),I()});H();
//# sourceMappingURL=index-qAK1bc0f.js.map
