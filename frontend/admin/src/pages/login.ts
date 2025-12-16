import { post } from '../api/client';
import { setState } from '../state';
import { navigate } from '../router';
import type { LoginResponse } from '../types';

export async function renderLogin() {
  const app = document.getElementById('app')!;

  app.innerHTML = `
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
  `;

  const form = document.getElementById('login-form') as HTMLFormElement;
  const errorDiv = document.getElementById('login-error')!;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    errorDiv.style.display = 'none';

    const username = (document.getElementById('username') as HTMLInputElement).value;
    const password = (document.getElementById('password') as HTMLInputElement).value;

    const submitBtn = form.querySelector('button[type="submit"]') as HTMLButtonElement;
    submitBtn.disabled = true;
    submitBtn.textContent = 'Logging in...';

    const response = await post<LoginResponse>('/api/auth/login', { username, password });

    if (response.error) {
      errorDiv.textContent = response.error === 'HTTP 401' ? 'Invalid credentials' : response.error;
      errorDiv.style.display = 'block';
      submitBtn.disabled = false;
      submitBtn.textContent = 'Login';
      return;
    }

    const { token, adminId, username: adminUsername, superAdmin, facilities } = response.data!;

    setState({
      token,
      adminId,
      username: adminUsername,
      superAdmin,
      facilities,
      currentFacilityId: facilities[0]?.id || null,
    });

    navigate('/dashboard');
  });
}
