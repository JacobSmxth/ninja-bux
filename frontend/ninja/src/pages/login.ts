import { post } from "../api/client";
import { setState, setCurrentNinja } from "../state";
import { navigate } from "../router";
import type { Ninja } from "../types";

function decodeJwt(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split(".");
    if (parts.length !== 3) return null;
    const payload = atob(parts[1].replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(payload);
  } catch (e) {
    return null;
  }
}

export async function renderLogin() {
  const container = document.getElementById("app")!;

  container.innerHTML = `
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
  `;

  const form = document.getElementById("login-form") as HTMLFormElement;
  const usernameInput = document.getElementById(
    "cn-username",
  ) as HTMLInputElement;
  const statusEl = document.getElementById("login-status") as HTMLDivElement;
  // Default fallback coordinates (used when geolocation unavailable over HTTP)
  const DEFAULT_LAT = 0;
  const DEFAULT_LON = 0;

  let cachedLat: number | null = null;
  let cachedLon: number | null = null;
  let cachedToken: string | null = null;

  const countStatuses = (node: any): { done: number; total: number } => {
    let total = 0;
    let done = 0;
    const visit = (value: any) => {
      if (!value || typeof value !== "object") return;
      if (Array.isArray(value)) {
        value.forEach(visit);
        return;
      }
      if (typeof value.status === "boolean") {
        total += 1;
        if (value.status) done += 1;
      }
      Object.values(value).forEach(visit);
    };
    visit(node);
    return { done, total };
  };

  function setStatus(message: string, isError = false) {
    if (!isError) {
      statusEl.textContent = "";
      statusEl.style.display = "none";
      return;
    }
    statusEl.textContent = message;
    statusEl.style.color = "#b00020";
    statusEl.style.display = "block";
  }

  function requestLocation(): Promise<void> {
    return new Promise((resolve) => {
      // Check if we're on a secure origin (HTTPS or localhost)
      const isSecure =
        window.location.protocol === "https:" ||
        window.location.hostname === "localhost" ||
        window.location.hostname === "127.0.0.1";

      if (!isSecure) {
        // Geolocation won't work over HTTP on network - use fallback
        cachedLat = DEFAULT_LAT;
        cachedLon = DEFAULT_LON;
        setStatus("Using default location (HTTP mode)");
        resolve();
        return;
      }

      if (!navigator.geolocation) {
        cachedLat = DEFAULT_LAT;
        cachedLon = DEFAULT_LON;
        setStatus("Geolocation not supported, using default location");
        resolve();
        return;
      }

      setStatus("Requesting location...");
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const { latitude, longitude } = pos.coords;
          cachedLat = latitude;
          cachedLon = longitude;
          setStatus("Location acquired.");
          resolve();
        },
        () => {
          // Use fallback coordinates on error
          cachedLat = DEFAULT_LAT;
          cachedLon = DEFAULT_LON;
          setStatus("Using default location");
          resolve();
        },
        { timeout: 5000 },
      );
    });
  }

  // Request location immediately on page load
  requestLocation();

  form.addEventListener("submit", async (evt) => {
    evt.preventDefault();
    const user = usernameInput.value.trim();

    if (!user) {
      setStatus("Please enter your username.", true);
      return;
    }

    if (cachedLat === null || cachedLon === null) {
      await requestLocation();
    }
    const latitude = cachedLat ?? DEFAULT_LAT;
    const longitude = cachedLon ?? DEFAULT_LON;

    setStatus("Logging in...");
    const loginRes = await post<any>("/cn/login", {
      user,
      latitude: Number(latitude),
      longitude: Number(longitude),
    });

    if (loginRes.error || !loginRes.data) {
      const friendlyError =
        loginRes.error && /404|not found/i.test(loginRes.error)
          ? "User not found. Please check your username."
          : loginRes.error || "unknown error";
      setStatus(`Login failed: ${friendlyError}`, true);
      return;
    }

    let loginData = loginRes.data as any;
    if (typeof loginData === "string") {
      try {
        loginData = JSON.parse(loginData);
      } catch {
        // leave as string
      }
    }

    const token = loginData.token;
    if (!token) {
      setStatus("Login did not return a token.", true);
      return;
    }
    cachedToken = token;

    const claims = decodeJwt(token);
    const facilityId = claims?.["facilityid"] as string | undefined;
    const studentId = claims?.["oid"] as string | undefined;
    if (!facilityId || !studentId) {
      setStatus("Could not read facility or student ID from token.", true);
      return;
    }

    setStatus("Fetching your current activity...");
    const activityRes = await fetch("/api/cn/activity/current", {
      headers: { Authorization: `Bearer ${token}` },
    });
    if (!activityRes.ok) {
      const errText = await activityRes.text();
      setStatus(
        `Activity fetch failed: ${activityRes.status} ${errText}`,
        true,
      );
      return;
    }
    const activityData = await activityRes.json();
    const rel = activityData?.relationShips?.data || {};
    if (rel.programId) {
      sessionStorage.setItem("cn_programId", rel.programId);
    }
    if (rel.courseId) {
      sessionStorage.setItem("cn_courseId", rel.courseId);
    }
    if (rel.levelId) {
      sessionStorage.setItem("cn_levelId", rel.levelId);
    }

    // Get level status to derive level sequence and activity sequence
    let levelSequence: number | null = null;
    let activitySequence: number | null = null;
    let completedSteps: number | null = null;
    let totalSteps: number | null = null;
    try {
      const params = new URLSearchParams({
        programId: rel.programId || "",
        courseId: rel.courseId || "",
      });
      if (rel.levelId) {
        params.append("levelId", rel.levelId);
      }
      const statusRes = await fetch(
        `/api/cn/level/statusinfo?${params.toString()}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        },
      );
      if (statusRes.ok) {
        const statusData = await statusRes.json();
        levelSequence = statusData.levelSequence ?? null;
        completedSteps = statusData.completedSteps ?? null;
        totalSteps = statusData.totalSteps ?? null;

        // Extract activity sequence from the activitySequences map
        const currentActivityId = rel.activityId || activityData.id;
        if (statusData.activitySequences && currentActivityId) {
          activitySequence =
            statusData.activitySequences[currentActivityId] ?? null;
        }
      }
    } catch {
      // ignore
    }

    // Try group-level status for finer-grained step counts
    if (rel.programId && rel.courseId) {
      try {
        const groupParams = new URLSearchParams({
          programId: rel.programId,
          courseId: rel.courseId,
        });
        const groupRes = await fetch(
          `/api/cn/groups/current?${groupParams.toString()}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          },
        );
        if (groupRes.ok) {
          const raw = await groupRes.text();
          let groupData: any;
          try {
            groupData = JSON.parse(raw);
          } catch {
            groupData = raw;
          }
          const { done, total } = countStatuses(groupData);
          if (total > 0) {
            completedSteps = done;
            totalSteps = total;
          }
        }
      } catch {
        // ignore
      }
    }

    setStatus("Saving your profile...");
    const syncRes = await post<any>(
      `/facilities/${facilityId}/ninjas/${studentId}/sync-local`,
      {
        firstName: loginData.user?.firstName || rel.firstName || "",
        lastName: loginData.user?.lastName || rel.lastName || "",
        courseName: rel.courseName || "",
        levelId: rel.levelId || "",
        levelSequence: levelSequence,
        activityId: rel.activityId || activityData.id,
        activitySequence: activitySequence,
        groupId: rel.groupId || "",
        subGroupId: rel.subgroupId || "",
        completedSteps: completedSteps,
        totalSteps: totalSteps,
        lastModifiedDate: activityData.lastModifiedDate || null,
        facilityName: loginData.user?.facilityName || null,
      },
    );
    if (syncRes.error || !syncRes.data) {
      setStatus(`Sync failed: ${syncRes.error || "unknown error"}`, true);
      return;
    }

    const syncData = syncRes.data as any;
    const ninjaData = syncData?.changes?.ninja;

    const mappedNinja: Ninja = {
      id: ninjaData?.id || 0,
      studentId: ninjaData?.studentId || studentId,
      firstName:
        ninjaData?.firstName ||
        loginData.user?.firstName ||
        rel.firstName ||
        "",
      lastName:
        ninjaData?.lastName || loginData.user?.lastName || rel.lastName || "",
      courseName: ninjaData?.courseName || rel.courseName || "",
      levelName: ninjaData?.levelName || null,
      levelSequence: ninjaData?.levelSequence ?? levelSequence ?? null,
      activityId:
        ninjaData?.activityId || rel.activityId || activityData.id || null,
      groupId: ninjaData?.groupId || rel.groupId || null,
      subGroupId: ninjaData?.subGroupId || rel.subgroupId || null,
      completedSteps: ninjaData?.completedSteps ?? completedSteps ?? null,
      totalSteps: ninjaData?.totalSteps ?? totalSteps ?? null,
      currentBalance: ninjaData?.currentBalance ?? 0,
      lastSyncedAt: ninjaData?.lastSyncedAt || new Date().toISOString(),
    };

    setState({ facilityId, studentId });
    // Store token for later calls if needed
    sessionStorage.setItem("cn_token", cachedToken || "");
    setCurrentNinja(mappedNinja);
    navigate("/dashboard");
  });
}
