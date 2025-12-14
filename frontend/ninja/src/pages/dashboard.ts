import { getState, isLoggedIn, setCurrentNinja } from '../state';
import { navigate } from '../router';
import { renderNavbar } from '../components/navbar';
import type { Ninja, LedgerTxn, TxnType, LedgerResponse } from '../types';
import { get } from '../api/client';

function getTxnIcon(type: TxnType): string {
  switch (type) {
    case 'INITIAL_BALANCE': return '+';
    case 'ACTIVITY_REWARD': return '+';
    case 'PURCHASE': return '-';
    case 'ADJUSTMENT': return '~';
    default: return '';
  }
}

function getTxnClass(amount: number): string {
  if (amount > 0) return 'positive';
  if (amount < 0) return 'negative';
  return 'neutral';
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
  });
}

function renderTransactions(transactions: LedgerTxn[]): string {
  if (transactions.length === 0) {
    return '<li class="empty">No recent transactions</li>';
  }

  return transactions.map(txn => `
    <li class="transaction-item ${getTxnClass(txn.amount)}">
      <span class="transaction-icon">${getTxnIcon(txn.type)}</span>
      <div class="transaction-details">
        <span class="transaction-desc">${txn.description}</span>
        <span class="transaction-date">${formatDate(txn.createdAt)}</span>
      </div>
      <span class="transaction-amount">${txn.amount > 0 ? '+' : ''}${txn.amount}</span>
    </li>
  `).join('');
}

export async function renderDashboard() {
  const container = document.getElementById('app')!;

  if (!isLoggedIn()) {
    navigate('/');
    return;
  }

  const { facilityId, studentId, currentNinja } = getState();

  container.innerHTML = `
    ${renderNavbar()}
    <main class="dashboard-page">
      <div class="loading">Loading dashboard...</div>
    </main>
  `;

  const mainContainer = container.querySelector('main')!;

  // Always refresh from the backend to ensure latest balance
  const ninjaRes = await get<Ninja>(`/facilities/${facilityId}/ninjas/${studentId}`);
  if (ninjaRes.error || !ninjaRes.data) {
    if (currentNinja) {
      // Fallback to cached profile
      renderDashboardContent(mainContainer, currentNinja, null, []);
      return;
    }
    mainContainer.innerHTML = `<div class="error">Failed to load profile. Please log in again.</div>`;
    return;
  }

  const ninja = ninjaRes.data;
  setCurrentNinja(ninja);
  const progress = await fetchProgress();
  const ledgerRes = await get<LedgerResponse>(
    `/facilities/${facilityId}/ninjas/${studentId}/ledger?limit=20`
  );
  const txns = ledgerRes.data?.transactions ?? [];
  renderDashboardContent(mainContainer, ninja, progress, txns);
}

type Progress = {
  totalSteps: number;
  completedSteps: number;
  completionPercent: number;
  nextActivityType?: string | null;
  nextSequence?: number | null;
  levelSequence?: number | null;
} | null;

async function fetchProgress(): Promise<Progress> {
  const token = sessionStorage.getItem('cn_token');
  const programId = sessionStorage.getItem('cn_programId');
  const courseId = sessionStorage.getItem('cn_courseId');
  const levelId = sessionStorage.getItem('cn_levelId');
  if (!token || !programId || !courseId) {
    return null;
  }
  try {
    const params = new URLSearchParams({
      programId,
      courseId,
    });
    if (levelId) {
      params.append('levelId', levelId);
    }
    const res = await fetch(`/api/cn/level/statusinfo?${params.toString()}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!res.ok) return null;
    const data = await res.json();
    return {
      totalSteps: data.totalSteps,
      completedSteps: data.completedSteps,
      completionPercent: data.completionPercent,
      nextActivityType: data.nextActivityType,
      nextSequence: data.nextSequence,
      levelSequence: data.levelSequence,
    };
  } catch {
    return null;
  }
}

function renderDashboardContent(
  mainContainer: Element,
  ninja: Ninja,
  progress: Progress,
  transactions: LedgerTxn[]
) {
  const levelLabel =
    progress?.levelSequence != null
      ? `Level ${progress.levelSequence}`
      : (ninja.levelName || 'Unknown');
  mainContainer.innerHTML = `
    <div class="dashboard-container">
      <div class="dashboard">
        <header class="dashboard-header">
          <h1>Welcome, ${[ninja.firstName, ninja.lastName].filter(Boolean).join(' ')}!</h1>
          <div class="ninja-info">
            <span class="belt-badge belt-${ninja.courseName.toLowerCase().replace(' ', '-')}">${ninja.courseName}</span>
            <span class="level-badge">${levelLabel}</span>
          </div>
        </header>

        <div class="balance-card">
          <div class="balance-label">Your Balance</div>
          <div class="balance-amount">${ninja.currentBalance ?? 0} <span class="balance-unit">Bux</span></div>
        </div>

        <section class="recent-activity">
          <h2>Progress</h2>
          ${
            progress
              ? `<div class="card" style="padding: 1rem; border-radius: 8px; background: #0f172a; color: white;">
                  <div>Completed: ${progress.completedSteps}/${progress.totalSteps} (${progress.completionPercent}%)</div>
                  <div>Next: ${progress.nextActivityType || 'Unknown'} ${progress.nextSequence || ''}</div>
                </div>`
              : '<div class="card" style="padding: 1rem; border-radius: 8px; background: #0f172a; color: white;">Progress data unavailable.</div>'
          }
        </section>

        <section class="recent-activity">
          <h2>Your Ledger</h2>
          <ul class="transaction-list">
            ${renderTransactions(transactions)}
          </ul>
        </section>

        <div class="quick-links">
          <a href="#/shop" class="btn btn-primary">Shop</a>
          <a href="#/leaderboard" class="btn btn-secondary">Leaderboard</a>
        </div>
      </div>
    </div>
  `;
}
