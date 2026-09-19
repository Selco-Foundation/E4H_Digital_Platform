import type { ModuleDefinition } from "@/shared";
import type { AnyRoute } from "@tanstack/react-router";
import { createCoreRoutes } from "./routes";

export { AppShell } from "./layout/AppShell";
export { LoginPage } from "./pages/employee/LoginPage";
export { HomePage } from "./pages/employee/HomePage";
export { CORE_ROUTES } from "./constants/routes";
export { createCoreRoutes };

export interface CoreModuleDefinition extends ModuleDefinition<AnyRoute> {
  employeeLayoutRoute: AnyRoute;
}

export function createCoreModule(rootRoute: AnyRoute): CoreModuleDefinition {
  const { routes, navItems, employeeLayoutRoute } = createCoreRoutes(rootRoute);

  return {
    id: "core",
    order: 0,
    routes,
    navItems,
    employeeLayoutRoute,
  };
}
