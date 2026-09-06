import { createCoreModule } from "@/modules/core";
import { setRegisteredModules } from "./module-registry";
import type { ModuleDefinition } from "@/shared";
import type { AnyRoute } from "@tanstack/react-router";
import { createRootRoute, Outlet } from "@tanstack/react-router";

const rootRoute = createRootRoute({
  component: Outlet,
});

const core = createCoreModule(rootRoute);

// im/ir-equivalent modules aren't ported yet — register them here, the same
// way core is registered, once they exist (see livelihood-ui's src/modules.ts
// for the shape: createXModule(rootRoute, core.employeeLayoutRoute)).
const enabledModules: ModuleDefinition<AnyRoute>[] = [core];

setRegisteredModules(enabledModules);

export { rootRoute, enabledModules };
export { getModuleOverviews, getModuleNavItems } from "./module-registry";
