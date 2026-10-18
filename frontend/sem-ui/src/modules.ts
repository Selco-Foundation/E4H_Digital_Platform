import { createCoreModule } from "@/modules/core";
import { createImModule } from "@/modules/im";
import { setRegisteredModules } from "./module-registry";
import type { ModuleDefinition } from "@/shared";
import type { AnyRoute } from "@tanstack/react-router";
import { createRootRoute, Outlet } from "@tanstack/react-router";

const rootRoute = createRootRoute({
  component: Outlet,
});

const core = createCoreModule(rootRoute);
const im = createImModule(rootRoute, core.employeeLayoutRoute);

// An ir-equivalent module isn't ported yet — register it here the same way,
// once it exists (see livelihood-ui's src/modules.ts for the shape:
// createIrModule(rootRoute, core.employeeLayoutRoute)).
const enabledModules: ModuleDefinition<AnyRoute>[] = [core, im];

setRegisteredModules(enabledModules);

export { rootRoute, enabledModules };
export { getModuleOverviews, getModuleNavItems } from "./module-registry";
