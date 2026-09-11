import type { ComponentType } from "react";

export interface NavItem {
  id: string;
  label: string;
  /**
   * Localization code for `label`. Module nav items are built once outside
   * React (at module registration), so `label` is a static English fallback;
   * when set, the app shell re-resolves the translated text via `translateOr`
   * at render time so it reacts to language changes.
   */
  labelKey?: string;
  to: string;
  icon?: ComponentType<{ className?: string }>;
  /**
   * Additional path prefixes (besides `to`) that should also render this nav
   * item as active — e.g. a details page reached only via this section, whose
   * own route lives outside `to`'s own path tree.
   */
  matchPrefixes?: string[];
  /**
   * Role codes allowed to see this nav item. Omit to show it to every
   * logged-in user (e.g. core's "Overview"). When set, the item is hidden
   * unless the current user holds at least one of these roles — each module
   * owns its own role list (see its `utils/access.ts`) rather than this type
   * knowing about any module's specific roles.
   */
  roles?: string[];
}

export interface ModuleOverview {
  /** Stat tiles rendered in the home page's shared KPIs row. */
  kpis?: ComponentType;
  /** Richer per-module content rendered in the home page's shared Details section. */
  details?: ComponentType;
  /**
   * Action(s) rendered in the home page's top bar, after the language
   * switcher — e.g. im's "Raise Ticket" button. Unlike the language switcher
   * (baked into every page via `TopBar`), this is contextual to the home
   * page specifically, not broadcast to every page in the module.
   */
  actions?: ComponentType;
}

export interface ModuleDefinition<TRoute = unknown> {
  id: string;
  order?: number;
  routes: TRoute[];
  navItems: NavItem[];
  overview?: ModuleOverview;
}
