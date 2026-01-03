import { getState } from '../state';

interface ApiResponse<T> {
  data?: T;
  error?: string;
}

function getFriendlyErrorMessage(status: number): string {
  if (status >= 500) {
    return 'The server is not functioning right now. Please try again in a moment.';
  }

  switch (status) {
    case 400:
      return 'We could not process that request. Please check the details and try again.';
    case 401:
      return 'You are not authorized to perform this action.';
    case 403:
      return 'You do not have permission to perform this action.';
    case 404:
      return 'We could not find what you are looking for.';
    case 408:
      return 'The request took too long. Please try again.';
    case 409:
      return 'That action conflicts with existing data. Please refresh and try again.';
    case 422:
      return 'Some details are invalid. Please review and try again.';
    case 429:
      return 'Too many requests. Please try again shortly.';
    default:
      return 'Something went wrong. Please try again.';
  }
}

async function request<T>(
  url: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  const state = getState();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (state.token) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }

  try {
    const response = await fetch(url, { ...options, headers });
    const isLoginRequest = url.includes('/auth/login');

    if (response.status === 401) {
      localStorage.removeItem('token');
      if (!isLoginRequest) {
        window.location.hash = '#/login';
      }
      return {
        error: isLoginRequest
          ? 'Invalid username or password.'
          : 'Your session has expired. Please log in again.'
      };
    }

    if (!response.ok) {
      const errorBody = await response.json().catch(() => ({})) as {
        error?: string;
        message?: string;
      };
      const serverMessage = errorBody.error || errorBody.message;
      return { error: serverMessage || getFriendlyErrorMessage(response.status) };
    }

    if (response.status === 204) {
      return { data: undefined as T };
    }

    const data = await response.json();
    return { data };
  } catch (error) {
    return { error: 'Unable to reach the server right now. Please check your connection and try again.' };
  }
}

export async function get<T>(url: string): Promise<ApiResponse<T>> {
  return request<T>(url, { method: 'GET' });
}

export async function post<T>(url: string, body?: unknown): Promise<ApiResponse<T>> {
  return request<T>(url, {
    method: 'POST',
    body: body ? JSON.stringify(body) : undefined,
  });
}

export async function put<T>(url: string, body?: unknown): Promise<ApiResponse<T>> {
  return request<T>(url, {
    method: 'PUT',
    body: body ? JSON.stringify(body) : undefined,
  });
}

export async function del<T>(url: string): Promise<ApiResponse<T>> {
  return request<T>(url, { method: 'DELETE' });
}
