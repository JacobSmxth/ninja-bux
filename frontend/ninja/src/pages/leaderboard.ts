import { get } from '../api/client';
import { getState, isLoggedIn } from '../state';
import { navigate } from '../router';
import { renderNavbar } from '../components/navbar';
import type { LeaderboardResponse, LeaderboardEntry, LeaderboardPeriod } from '../types';

let currentPeriod: LeaderboardPeriod = 'weekly';

function renderLeaderboardList(entries: LeaderboardEntry[], type: 'earned' | 'spent'): string {
  if (entries.length === 0) {
    return '<li class="empty">No data available</li>';
  }

  const { studentId } = getState();

  return entries.map(entry => {
    const isCurrentUser = entry.studentId === studentId;
    const points = type === 'earned' ? entry.pointsEarned : entry.pointsSpent;

    return `
      <li class="leaderboard-entry ${isCurrentUser ? 'current-user' : ''}">
        <span class="entry-rank">${entry.rank}</span>
        <div class="entry-info">
          <span class="entry-name">${entry.ninjaName}</span>
        </div>
        <span class="entry-points">${Math.abs(points || 0)} Bux</span>
      </li>
    `;
  }).join('');
}

async function loadLeaderboards() {
  const { facilityId } = getState();

  const earnedList = document.getElementById('earned-list')!;
  const spentList = document.getElementById('spent-list')!;

  earnedList.innerHTML = '<li class="loading">Loading...</li>';
  spentList.innerHTML = '<li class="loading">Loading...</li>';

  const [earnedRes, spentRes] = await Promise.all([
    get<LeaderboardResponse>(`/facilities/${facilityId}/leaderboard/earned?period=${currentPeriod}`),
    get<LeaderboardResponse>(`/facilities/${facilityId}/leaderboard/spent?period=${currentPeriod}`)
  ]);

  if (earnedRes.error) {
    earnedList.innerHTML = `<li class="error">Failed to load</li>`;
  } else {
    earnedList.innerHTML = renderLeaderboardList(earnedRes.data!.leaderboard, 'earned');
  }

  if (spentRes.error) {
    spentList.innerHTML = `<li class="error">Failed to load</li>`;
  } else {
    spentList.innerHTML = renderLeaderboardList(spentRes.data!.leaderboard, 'spent');
  }
}

function attachPeriodHandlers() {
  document.querySelectorAll('.period-tab').forEach(tab => {
    tab.addEventListener('click', async () => {
      const period = tab.getAttribute('data-period') as LeaderboardPeriod;
      if (period === currentPeriod) return;

      currentPeriod = period;

      // Update active tab
      document.querySelectorAll('.period-tab').forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      await loadLeaderboards();
    });
  });
}

export async function renderLeaderboard() {
  const container = document.getElementById('app')!;

  if (!isLoggedIn()) {
    navigate('/');
    return;
  }

  container.innerHTML = `
    ${renderNavbar()}
    <main class="leaderboard-page">
      <div class="dashboard-container">
        <div class="leaderboard">
          <header class="leaderboard-header">
            <h1>Leaderboard</h1>
            <div class="period-tabs">
              <button class="period-tab ${currentPeriod === 'weekly' ? 'active' : ''}" data-period="weekly">Weekly</button>
              <button class="period-tab ${currentPeriod === 'monthly' ? 'active' : ''}" data-period="monthly">Monthly</button>
              <button class="period-tab ${currentPeriod === 'allTime' ? 'active' : ''}" data-period="allTime">All Time</button>
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
  `;

  attachPeriodHandlers();
  await loadLeaderboards();
}
