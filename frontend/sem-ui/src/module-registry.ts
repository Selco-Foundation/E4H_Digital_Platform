import type { ComponentType } from "react";
import type { ModuleDefinition } from "@/shared";

let registeredModules: ModuleDefinition[] = [];

export function setRegisteredModules(modules: ModuleDefinition[]) {
  if (import.meta.env.DEV) {
    for (const module of modules) {
      // order 0 is reserved for the shell/infrastructure module (login, home page,
      // employee layout route) — it has nothing of its own to surface via nav or
      // home, by design, so it's exempt from this check.
      const isShellModule = module.order === 0;
      const hasVisibleSurface =
        isShellModule ||
        Boolean(module.overview?.kpis) ||
        Boolean(module.overview?.details) ||
        Boolean(module.overview?.actions) ||
        module.navItems.length > 0;
      if (!hasVisibleSurface) {
        console.warn(
          `[module-registry] Module "${module.id}" has no kpis, details, actions, or navItems — ` +
            "it's registered but won't appear anywhere in the UI. This usually means a config mistake.",
        );
      }
    }
  }
  registeredModules = modules;
}

export function getRegisteredModules() {
  return registeredModules;
}

/**
 * `userRoles` gates each nav item by its own `roles` list (undefined = visible
 * to everyone). Omit `userRoles` only for contexts with no signed-in user.
 */
export function getModuleNavItems(userRoles?: Array<{ code?: string }>) {
  const userRoleCodes = new Set(userRoles?.map((role) => role.code).filter(Boolean));

  return [...registeredModules]
    .sort((a, b) => (a.order ?? 99) - (b.order ?? 99))
    .flatMap((module) => module.navItems ?? [])
    .filter((item) => !item.roles || item.roles.some((role) => userRoleCodes.has(role)));
}

export interface ModuleOverviewEntry {
  Component: ComponentType;
  moduleId: string;
}

export interface ModuleOverviewSections {
  kpis: ModuleOverviewEntry[];
  details: ModuleOverviewEntry[];
  actions: ModuleOverviewEntry[];
}

/** Modules ordered once, then split into the home page's shared sections. */
export function getModuleOverviews(): ModuleOverviewSections {
  const ordered = [...registeredModules].sort((a, b) => (a.order ?? 99) - (b.order ?? 99));

  return {
    kpis: ordered
      .filter((module) => module.overview?.kpis)
      .map((module) => ({ Component: module.overview!.kpis!, moduleId: module.id })),
    details: ordered
      .filter((module) => module.overview?.details)
      .map((module) => ({ Component: module.overview!.details!, moduleId: module.id })),
    actions: ordered
      .filter((module) => module.overview?.actions)
      .map((module) => ({ Component: module.overview!.actions!, moduleId: module.id })),
  };
}
