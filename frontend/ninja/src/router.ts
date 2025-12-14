type RouteHandler = (params?: Record<string, string>) => void | Promise<void>;

interface Route {
  pattern: RegExp;
  handler: RouteHandler;
  paramNames: string[];
}

const routes: Route[] = [];

export function route(path: string, handler: RouteHandler) {
  // Convert path pattern to regex, extracting param names
  const paramNames: string[] = [];
  const pattern = path.replace(/:([^/]+)/g, (_match, paramName) => {
    paramNames.push(paramName);
    return '([^/]+)';
  });
  routes.push({
    pattern: new RegExp(`^${pattern}$`),
    handler,
    paramNames,
  });
}

export function navigate(path: string) {
  window.location.hash = path;
}

function handleRoute() {
  const hash = window.location.hash.slice(1) || '/';

  for (const { pattern, handler, paramNames } of routes) {
    const match = hash.match(pattern);
    if (match) {
      const params: Record<string, string> = {};
      paramNames.forEach((name, index) => {
        params[name] = match[index + 1];
      });
      handler(params);
      return;
    }
  }

  // Default to root if no match
  const rootRoute = routes.find(r => r.pattern.test('/'));
  if (rootRoute) {
    rootRoute.handler({});
  }
}

export function initRouter() {
  window.addEventListener('hashchange', handleRoute);
  window.addEventListener('load', handleRoute);
  // Handle initial load
  handleRoute();
}
