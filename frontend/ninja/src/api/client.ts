import type { ApiResponse } from "../types";

const BASE_URL = "/api";

export async function get<T>(url: string): Promise<ApiResponse<T>> {
  try {
    const response = await fetch(`${BASE_URL}${url}`);
    if (!response.ok) {
      const errorText = await response.text();
      return { error: errorText || `HTTP ${response.status}` };
    }
    const data = await response.json();
    return { data };
  } catch (error) {
    return { error: String(error) };
  }
}

export async function post<T>(
  url: string,
  body: unknown,
): Promise<ApiResponse<T>> {
  try {
    const response = await fetch(`${BASE_URL}${url}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!response.ok) {
      const errorText = await response.text();
      return { error: errorText || `HTTP ${response.status}` };
    }
    const data = await response.json();
    return { data };
  } catch (error) {
    return { error: String(error) };
  }
}

export async function put<T>(
  url: string,
  body?: unknown,
): Promise<ApiResponse<T>> {
  try {
    const response = await fetch(`${BASE_URL}${url}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: body ? JSON.stringify(body) : undefined,
    });
    if (!response.ok) {
      const errorText = await response.text();
      return { error: errorText || `HTTP ${response.status}` };
    }
    const data = await response.json();
    return { data };
  } catch (error) {
    return { error: String(error) };
  }
}

export async function del<T>(url: string): Promise<ApiResponse<T>> {
  try {
    const response = await fetch(`${BASE_URL}${url}`, {
      method: "DELETE",
    });
    if (!response.ok) {
      const errorText = await response.text();
      return { error: errorText || `HTTP ${response.status}` };
    }
    if (response.status === 204) {
      return { data: undefined as T };
    }
    const data = await response.json();
    return { data };
  } catch (error) {
    return { error: String(error) };
  }
}
