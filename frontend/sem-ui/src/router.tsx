import { createRouter } from "@tanstack/react-router";
import { enabledModules, rootRoute } from "./modules";

export function createAppRouter() {
  const routes = enabledModules.flatMap((module) => module.routes);
  const routeTree = rootRoute.addChildren(routes);

  return createRouter({
    routeTree,
    defaultPreload: "intent",
  });
}

declare module "@tanstack/react-router" {
  interface Register {
    router: ReturnType<typeof createAppRouter>;
  }
}
