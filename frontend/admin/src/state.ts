import type { Facility } from './types';

interface AppState {
  token: string | null;
  adminId: number | null;
  username: string | null;
  superAdmin: boolean;
  facilities: Facility[];
  currentFacilityId: string | null;
}

const state: AppState = {
  token: localStorage.getItem('token'),
  adminId: localStorage.getItem('adminId') ? Number(localStorage.getItem('adminId')) : null,
  username: localStorage.getItem('username'),
  superAdmin: localStorage.getItem('superAdmin') === 'true',
  facilities: JSON.parse(localStorage.getItem('facilities') || '[]'),
  currentFacilityId: localStorage.getItem('currentFacilityId'),
};

export function getState(): AppState {
  return state;
}

export function setState(updates: Partial<AppState>) {
  Object.assign(state, updates);

  if (updates.token !== undefined) {
    if (updates.token) localStorage.setItem('token', updates.token);
    else localStorage.removeItem('token');
  }
  if (updates.adminId !== undefined) {
    if (updates.adminId) localStorage.setItem('adminId', String(updates.adminId));
    else localStorage.removeItem('adminId');
  }
  if (updates.username !== undefined) {
    if (updates.username) localStorage.setItem('username', updates.username);
    else localStorage.removeItem('username');
  }
  if (updates.superAdmin !== undefined) {
    localStorage.setItem('superAdmin', String(updates.superAdmin));
  }
  if (updates.facilities !== undefined) {
    localStorage.setItem('facilities', JSON.stringify(updates.facilities));
  }
  if (updates.currentFacilityId !== undefined) {
    if (updates.currentFacilityId) localStorage.setItem('currentFacilityId', updates.currentFacilityId);
    else localStorage.removeItem('currentFacilityId');
  }
}

export function isSuperAdmin(): boolean {
  return state.superAdmin;
}

export function isAuthenticated(): boolean {
  return !!state.token;
}

export function getCurrentFacilityId(): string {
  return state.currentFacilityId || state.facilities[0]?.id || '';
}

export function logout() {
  setState({
    token: null,
    adminId: null,
    username: null,
    superAdmin: false,
    facilities: [],
    currentFacilityId: null,
  });
  localStorage.removeItem('superAdmin');
  window.location.hash = '#/login';
}
