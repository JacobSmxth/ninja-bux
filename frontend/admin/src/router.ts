import { isAuthenticated } from './state';

type RouteHandler = () => void | Promise<void>;

const routes: Record<string, RouteHandler> = {};

export function route(path: string, handler: RouteHandler) {
  routes[path] = handler;
}

export function navigate(path: string) {
  window.location.hash = path;
}

async function handleRoute() {
  const hash = window.location.hash.slice(1) || '/login';
  const path = hash.split('?')[0];

  // Redirect to login if not authenticated (except for login page)
  if (path !== '/login' && !isAuthenticated()) {
    navigate('/login');
    return;
  }

  // Redirect to dashboard if authenticated and on login page
  if (path === '/login' && isAuthenticated()) {
    navigate('/dashboard');
    return;
  }

  const handler = routes[path] || routes['/dashboard'];
  if (handler) {
    await handler();
  }
}

export function initRouter() {
  window.addEventListener('hashchange', handleRoute);
  handleRoute();
}
