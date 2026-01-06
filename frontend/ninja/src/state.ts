import type { AppState, Ninja } from "./types";

function loadNinja(): Ninja | null {
  const raw = sessionStorage.getItem("currentNinja");
  if (!raw) return null;
  try {
    return JSON.parse(raw) as Ninja;
  } catch {
    return null;
  }
}

const state: AppState = {
  facilityId: sessionStorage.getItem("facilityId") || "",
  studentId: sessionStorage.getItem("studentId"),
  currentNinja: loadNinja(),
};

export function getState(): AppState {
  return state;
}

export function setState(updates: Partial<AppState>) {
  Object.assign(state, updates);
  if (updates.facilityId !== undefined) {
    sessionStorage.setItem("facilityId", updates.facilityId);
  }
  if (updates.studentId !== undefined) {
    if (updates.studentId) {
      sessionStorage.setItem("studentId", updates.studentId);
    } else {
      sessionStorage.removeItem("studentId");
    }
  }
}

export function setCurrentNinja(ninja: Ninja | null) {
  state.currentNinja = ninja;
  if (ninja) {
    sessionStorage.setItem("currentNinja", JSON.stringify(ninja));
  } else {
    sessionStorage.removeItem("currentNinja");
  }
}

export function logout() {
  setState({ studentId: null, currentNinja: null });
}

export function isLoggedIn(): boolean {
  return state.studentId !== null;
}
