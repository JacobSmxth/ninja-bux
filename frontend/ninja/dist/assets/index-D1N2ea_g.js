(function(){const t=document.createElement("link").relList;if(t&&t.supports&&t.supports("modulepreload"))return;for(const s of document.querySelectorAll('link[rel="modulepreload"]'))a(s);new MutationObserver(s=>{for(const o of s)if(o.type==="childList")for(const i of o.addedNodes)i.tagName==="LINK"&&i.rel==="modulepreload"&&a(i)}).observe(document,{childList:!0,subtree:!0});function n(s){const o={};return s.integrity&&(o.integrity=s.integrity),s.referrerPolicy&&(o.referrerPolicy=s.referrerPolicy),s.crossOrigin==="use-credentials"?o.credentials="include":s.crossOrigin==="anonymous"?o.credentials="omit":o.credentials="same-origin",o}function a(s){if(s.ep)return;s.ep=!0;const o=n(s);fetch(s.href,o)}})();const F=[];function H(e,t){const n=[],a=e.replace(/:([^/]+)/g,(s,o)=>(n.push(o),"([^/]+)"));F.push({pattern:new RegExp(`^${a}$`),handler:t,paramNames:n})}function x(e){window.location.hash=e}function j(){const e=window.location.hash.slice(1)||"/";for(const{pattern:n,handler:a,paramNames:s}of F){const o=e.match(n);if(o){const i={};s.forEach((l,m)=>{i[l]=o[m+1]}),a(i);return}}const t=F.find(n=>n.pattern.test("/"));t&&t.handler({})}function me(){window.addEventListener("hashchange",j),window.addEventListener("load",j),j()}const oe="/api";async function S(e){try{const t=await fetch(`${oe}${e}`);return t.ok?{data:await t.json()}:{error:await t.text()||`HTTP ${t.status}`}}catch(t){return{error:String(t)}}}async function J(e,t){try{const n=await fetch(`${oe}${e}`,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(t)});return n.ok?{data:await n.json()}:{error:await n.text()||`HTTP ${n.status}`}}catch(n){return{error:String(n)}}}function fe(){const e=sessionStorage.getItem("currentNinja");if(!e)return null;try{return JSON.parse(e)}catch{return null}}const M={facilityId:sessionStorage.getItem("facilityId")||"fcd4728c-afff-4a3c-8a39-05d2cd9d87ac",studentId:sessionStorage.getItem("studentId"),currentNinja:fe()};function $(){return M}function ie(e){Object.assign(M,e),e.facilityId!==void 0&&sessionStorage.setItem("facilityId",e.facilityId),e.studentId!==void 0&&(e.studentId?sessionStorage.setItem("studentId",e.studentId):sessionStorage.removeItem("studentId"))}function Y(e){M.currentNinja=e,e?sessionStorage.setItem("currentNinja",JSON.stringify(e)):sessionStorage.removeItem("currentNinja")}function he(){ie({studentId:null,currentNinja:null})}function z(){return M.studentId!==null}function ge(e){try{const t=e.split(".");if(t.length!==3)return null;const n=atob(t[1].replace(/-/g,"+").replace(/_/g,"/"));return JSON.parse(n)}catch{return null}}async function ve(){const e=document.getElementById("app");e.innerHTML=`
    <div class="login-page">
      <div class="login-card glass-card">
        <header class="login-header text-center">
          <img src="/CodeNinjasLogo.svg" alt="Code Ninjas" class="login-logo impact-logo" />
          <h1 class="login-brand">NinjaBux</h1>
        </header>

        <form id="login-form" class="login-form">
          <input
            type="text"
            id="cn-username"
            name="cn-username"
            class="login-input"
            placeholder="Username"
            aria-label="Username"
            autocomplete="username"
            required
          />

          <button type="submit" class="btn btn-green" id="login-submit">Log In</button>
        </form>

        <div id="login-status" class="login-status"></div>
      </div>
    </div>
  `;const t=document.getElementById("login-form"),n=document.getElementById("cn-username"),a=document.getElementById("login-status"),s=0,o=0;let i=null,l=null,m=null;const E=p=>{let f=0,L=0;const b=u=>{if(!(!u||typeof u!="object")){if(Array.isArray(u)){u.forEach(b);return}typeof u.status=="boolean"&&(f+=1,u.status&&(L+=1)),Object.values(u).forEach(b)}};return b(p),{done:L,total:f}};function d(p,f=!1){a.textContent=p,a.style.color=f?"#b00020":"#0b3d91"}function K(){return new Promise(p=>{if(!(window.location.protocol==="https:"||window.location.hostname==="localhost"||window.location.hostname==="127.0.0.1")){i=s,l=o,d("Using default location (HTTP mode)"),p();return}if(!navigator.geolocation){i=s,l=o,d("Geolocation not supported, using default location"),p();return}d("Requesting location..."),navigator.geolocation.getCurrentPosition(L=>{const{latitude:b,longitude:u}=L.coords;i=b,l=u,d("Location acquired."),p()},()=>{i=s,l=o,d("Using default location"),p()},{timeout:5e3})})}K(),t.addEventListener("submit",async p=>{var Q,X,Z,D,ee,te;p.preventDefault();const f=n.value.trim();if(!f){d("Please enter your username.",!0);return}(i===null||l===null)&&await K();const L=i??s,b=l??o;d("Logging in...");const u=await J("/cn/login",{user:f,latitude:Number(L),longitude:Number(b)});if(u.error||!u.data){d(`Login failed: ${u.error||"unknown error"}`,!0);return}let h=u.data;if(typeof h=="string")try{h=JSON.parse(h)}catch{}const w=h.token;if(!w){d("Login did not return a token.",!0);return}m=w;const N=ge(w),O=N==null?void 0:N.facilityid,k=N==null?void 0:N.oid;if(!O||!k){d("Could not read facility or student ID from token.",!0);return}d("Fetching your current activity...");const q=await fetch("/api/cn/activity/current",{headers:{Authorization:`Bearer ${w}`}});if(!q.ok){const T=await q.text();d(`Activity fetch failed: ${q.status} ${T}`,!0);return}const y=await q.json(),c=((Q=y==null?void 0:y.relationShips)==null?void 0:Q.data)||{};c.programId&&sessionStorage.setItem("cn_programId",c.programId),c.courseId&&sessionStorage.setItem("cn_courseId",c.courseId),c.levelId&&sessionStorage.setItem("cn_levelId",c.levelId);let U=null,V=null,P=null,C=null;try{const T=new URLSearchParams({programId:c.programId||"",courseId:c.courseId||""});c.levelId&&T.append("levelId",c.levelId);const A=await fetch(`/api/cn/level/statusinfo?${T.toString()}`,{headers:{Authorization:`Bearer ${w}`}});if(A.ok){const g=await A.json();U=g.levelSequence??null,P=g.completedSteps??null,C=g.totalSteps??null;const B=c.activityId||y.id;g.activitySequences&&B&&(V=g.activitySequences[B]??null)}}catch{}if(c.programId&&c.courseId)try{const T=new URLSearchParams({programId:c.programId,courseId:c.courseId}),A=await fetch(`/api/cn/groups/current?${T.toString()}`,{headers:{Authorization:`Bearer ${w}`}});if(A.ok){const g=await A.text();let B;try{B=JSON.parse(g)}catch{B=g}const{done:pe,total:ne}=E(B);ne>0&&(P=pe,C=ne)}}catch{}d("Saving your profile...");const R=await J(`/facilities/${O}/ninjas/${k}/sync-local`,{firstName:((X=h.user)==null?void 0:X.firstName)||c.firstName||"",lastName:((Z=h.user)==null?void 0:Z.lastName)||c.lastName||"",courseName:c.courseName||"",levelId:c.levelId||"",levelSequence:U,activityId:c.activityId||y.id,activitySequence:V,groupId:c.groupId||"",subGroupId:c.subgroupId||"",completedSteps:P,totalSteps:C,lastModifiedDate:y.lastModifiedDate||null});if(R.error||!R.data){d(`Sync failed: ${R.error||"unknown error"}`,!0);return}const _=R.data,r=(D=_==null?void 0:_.changes)==null?void 0:D.ninja,ue={id:(r==null?void 0:r.id)||0,studentId:(r==null?void 0:r.studentId)||k,firstName:(r==null?void 0:r.firstName)||((ee=h.user)==null?void 0:ee.firstName)||c.firstName||"",lastName:(r==null?void 0:r.lastName)||((te=h.user)==null?void 0:te.lastName)||c.lastName||"",courseName:(r==null?void 0:r.courseName)||c.courseName||"",levelName:(r==null?void 0:r.levelName)||null,levelSequence:(r==null?void 0:r.levelSequence)??U??null,activityId:(r==null?void 0:r.activityId)||c.activityId||y.id||null,groupId:(r==null?void 0:r.groupId)||c.groupId||null,subGroupId:(r==null?void 0:r.subGroupId)||c.subgroupId||null,completedSteps:(r==null?void 0:r.completedSteps)??P??null,totalSteps:(r==null?void 0:r.totalSteps)??C??null,currentBalance:(r==null?void 0:r.currentBalance)??0,lastSyncedAt:(r==null?void 0:r.lastSyncedAt)||new Date().toISOString()};ie({facilityId:O,studentId:k}),sessionStorage.setItem("cn_token",m||""),Y(ue),x("/dashboard")})}function G(){const{currentNinja:e}=$();return`
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
          <span class="nav-user">${e?`${e.firstName}`:""}</span>
          <button class="btn btn-logout" id="logout-btn">Logout</button>
        </div>
      </div>
    </nav>
  `}function W(){const e=document.getElementById("logout-btn");e&&e.addEventListener("click",()=>{he(),x("/")})}function be(e){switch(e){case"INITIAL_BALANCE":return"+";case"ACTIVITY_REWARD":return"+";case"PURCHASE":return"-";case"ADJUSTMENT":return"~";default:return""}}function ye(e){return e>0?"positive":e<0?"negative":"neutral"}function Ie(e){return new Date(e).toLocaleDateString("en-US",{month:"short",day:"numeric",hour:"numeric",minute:"2-digit"})}function Se(e){return e.length===0?'<li class="empty">No recent transactions</li>':e.map(t=>`
    <li class="transaction-item ${ye(t.amount)}">
      <span class="transaction-icon">${be(t.type)}</span>
      <div class="transaction-details">
        <span class="transaction-desc">${t.description}</span>
        <span class="transaction-date">${Ie(t.createdAt)}</span>
      </div>
      <span class="transaction-amount">${t.amount>0?"+":""}${t.amount}</span>
    </li>
  `).join("")}async function $e(){var d;const e=document.getElementById("app");if(!z()){x("/");return}const{facilityId:t,studentId:n,currentNinja:a}=$();e.innerHTML=`
    ${G()}
    <main class="dashboard-page">
      <div class="loading">Loading dashboard...</div>
    </main>
  `;const s=e.querySelector("main"),o=await S(`/facilities/${t}/ninjas/${n}`);if(o.error||!o.data){if(a){ae(s,a,null,[]);return}s.innerHTML='<div class="error">Failed to load profile. Please log in again.</div>';return}const i=o.data;Y(i);const l=await Le(),E=((d=(await S(`/facilities/${t}/ninjas/${n}/ledger?limit=20`)).data)==null?void 0:d.transactions)??[];ae(s,i,l,E)}async function Le(){const e=sessionStorage.getItem("cn_token"),t=sessionStorage.getItem("cn_programId"),n=sessionStorage.getItem("cn_courseId"),a=sessionStorage.getItem("cn_levelId");if(!e||!t||!n)return null;try{const s=new URLSearchParams({programId:t,courseId:n});a&&s.append("levelId",a);const o=await fetch(`/api/cn/level/statusinfo?${s.toString()}`,{headers:{Authorization:`Bearer ${e}`}});if(!o.ok)return null;const i=await o.json();return{totalSteps:i.totalSteps,completedSteps:i.completedSteps,completionPercent:i.completionPercent,nextActivityType:i.nextActivityType,nextSequence:i.nextSequence,levelSequence:i.levelSequence}}catch{return null}}function ae(e,t,n,a){const s=(n==null?void 0:n.levelSequence)!=null?`Level ${n.levelSequence}`:t.levelName||"Unknown";e.innerHTML=`
    <div class="dashboard-container">
      <div class="dashboard">
        <header class="dashboard-header">
          <h1>Welcome, ${[t.firstName,t.lastName].filter(Boolean).join(" ")}!</h1>
          <div class="ninja-info">
            <span class="belt-badge belt-${t.courseName.toLowerCase().replace(" ","-")}">${t.courseName}</span>
            <span class="level-badge">${s}</span>
          </div>
        </header>

        <div class="balance-card">
          <div class="balance-label">Your Balance</div>
          <div class="balance-amount">${t.currentBalance??0} <span class="balance-unit">Bux</span></div>
        </div>

        <section class="recent-activity">
          <h2>Progress</h2>
          ${n?`<div class="card" style="padding: 1rem; border-radius: 8px; background: #0f172a; color: white;">
                  <div>Completed: ${n.completedSteps}/${n.totalSteps} (${n.completionPercent}%)</div>
                  <div>Next: ${n.nextActivityType||"Unknown"} ${n.nextSequence||""}</div>
                </div>`:'<div class="card" style="padding: 1rem; border-radius: 8px; background: #0f172a; color: white;">Progress data unavailable.</div>'}
        </section>

        <section class="recent-activity">
          <h2>Your Ledger</h2>
          <ul class="transaction-list">
            ${Se(a)}
          </ul>
        </section>

        <div class="quick-links">
          <a href="#/shop" class="btn btn-primary">Shop</a>
          <a href="#/leaderboard" class="btn btn-secondary">Leaderboard</a>
        </div>
      </div>
    </div>
  `}let v=0;function we(e){return v>=e}function ce(e){const t=we(e.price),n=e.isAvailable,a=t&&n;return`
    <div class="shop-item ${a?"":"disabled"}" data-item-id="${e.id}">
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
          class="btn btn-buy ${a?"":"btn-disabled"}"
          ${a?"":"disabled"}
          data-item-id="${e.id}"
          data-item-name="${e.name}"
          data-item-price="${e.price}"
        >
          ${n?t?"Buy":"Not enough Bux":"Unavailable"}
        </button>
      </div>
    </div>
  `}function Ne(e,t,n){const a=document.createElement("div");a.className="modal-overlay",a.innerHTML=`
    <div class="modal">
      <h2>Confirm Purchase</h2>
      <p>Are you sure you want to buy <strong>${t}</strong> for <strong>${n} Bux</strong>?</p>
      <p class="modal-balance">Your balance: ${v} Bux → ${v-n} Bux</p>
      <div class="modal-actions">
        <button class="btn btn-secondary" id="cancel-purchase">Cancel</button>
        <button class="btn btn-primary" id="confirm-purchase">Confirm</button>
      </div>
    </div>
  `,document.body.appendChild(a),a.querySelector("#cancel-purchase").addEventListener("click",()=>{a.remove()}),a.querySelector("#confirm-purchase").addEventListener("click",async()=>{const{facilityId:s,studentId:o}=$(),i=a.querySelector("#confirm-purchase");i.disabled=!0,i.textContent="Processing...";const l=await J(`/facilities/${s}/ninjas/${o}/purchases`,{shopItemId:e});if(l.error){se(`Purchase failed: ${l.error}`,"error"),a.remove();return}v=l.data.newBalance,Te(),await Be(),a.remove(),se(`Successfully purchased ${t}!`,"success")}),a.addEventListener("click",s=>{s.target===a&&a.remove()})}function se(e,t){const n=document.createElement("div");n.className=`toast toast-${t}`,n.textContent=e,document.body.appendChild(n),setTimeout(()=>{n.classList.add("fade-out"),setTimeout(()=>n.remove(),300)},3e3)}function Te(){const e=document.querySelector(".shop-balance-amount");e&&(e.textContent=`${v} Bux`);const t=document.querySelector(".nav-balance");t&&(t.textContent=`${v} Bux`)}async function Be(){const{facilityId:e}=$(),t=await S(`/facilities/${e}/shop`);if(t.data){const n=document.querySelector(".shop-grid");n&&(n.innerHTML=t.data.items.map(a=>ce(a)).join(""),le())}}function le(){document.querySelectorAll(".btn-buy:not(.btn-disabled)").forEach(e=>{e.addEventListener("click",t=>{t.stopPropagation();const n=t.target,a=parseInt(n.getAttribute("data-item-id")),s=n.getAttribute("data-item-name"),o=parseInt(n.getAttribute("data-item-price"));Ne(a,s,o)})})}async function Ee(){const e=document.getElementById("app");if(!z()){x("/");return}const{facilityId:t,studentId:n,currentNinja:a}=$();e.innerHTML=`
    ${G()}
    <main class="shop-page">
      <div class="loading">Loading shop...</div>
    </main>
  `;const s=a?{data:a,error:void 0}:await S(`/facilities/${t}/ninjas/${n}`),o=await S(`/facilities/${t}/shop`),i=e.querySelector("main");if(s.error||o.error){i.innerHTML='<div class="error">Failed to load shop</div>';return}const l=s.data;Y(l),v=l.currentBalance;const m=o.data.items;i.innerHTML=`
    <div class="shop-container">
      <div class="shop">
        <header class="shop-header">
          <h1>Shop</h1>
          <div class="shop-balance">
            <span class="shop-balance-label">Your Balance:</span>
            <span class="shop-balance-amount">${v} Bux</span>
          </div>
        </header>

        <div class="shop-grid">
          ${m.length>0?m.map(E=>ce(E)).join(""):'<div class="empty">No items available</div>'}
        </div>
      </div>
    </div>
  `,le()}let I="weekly";function re(e,t){if(e.length===0)return'<li class="empty">No data available</li>';const{studentId:n}=$();return e.map(a=>{const s=a.studentId===n,o=t==="earned"?a.pointsEarned:a.pointsSpent;return`
      <li class="leaderboard-entry ${s?"current-user":""}">
        <span class="entry-rank">${a.rank}</span>
        <div class="entry-info">
          <span class="entry-name">${a.ninjaName}</span>
        </div>
        <span class="entry-points">${Math.abs(o||0)} Bux</span>
      </li>
    `}).join("")}async function de(){const{facilityId:e}=$(),t=document.getElementById("earned-list"),n=document.getElementById("spent-list");t.innerHTML='<li class="loading">Loading...</li>',n.innerHTML='<li class="loading">Loading...</li>';const[a,s]=await Promise.all([S(`/facilities/${e}/leaderboard/earned?period=${I}`),S(`/facilities/${e}/leaderboard/spent?period=${I}`)]);a.error?t.innerHTML='<li class="error">Failed to load</li>':t.innerHTML=re(a.data.leaderboard,"earned"),s.error?n.innerHTML='<li class="error">Failed to load</li>':n.innerHTML=re(s.data.leaderboard,"spent")}function Ae(){document.querySelectorAll(".period-tab").forEach(e=>{e.addEventListener("click",async()=>{const t=e.getAttribute("data-period");t!==I&&(I=t,document.querySelectorAll(".period-tab").forEach(n=>n.classList.remove("active")),e.classList.add("active"),await de())})})}async function xe(){const e=document.getElementById("app");if(!z()){x("/");return}e.innerHTML=`
    ${G()}
    <main class="leaderboard-page">
      <div class="dashboard-container">
        <div class="leaderboard">
          <header class="leaderboard-header">
            <h1>Leaderboard</h1>
            <div class="period-tabs">
              <button class="period-tab ${I==="weekly"?"active":""}" data-period="weekly">Weekly</button>
              <button class="period-tab ${I==="monthly"?"active":""}" data-period="monthly">Monthly</button>
              <button class="period-tab ${I==="yearly"?"active":""}" data-period="yearly">Yearly</button>
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
  `,Ae(),await de()}H("/",ve);H("/dashboard",async()=>{await $e(),W()});H("/shop",async()=>{await Ee(),W()});H("/leaderboard",async()=>{await xe(),W()});me();
//# sourceMappingURL=index-D1N2ea_g.js.map
