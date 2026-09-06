import { contextPath, translateOr, useModuleI18n, useTranslate } from "@/shared";
import type { AnyRoute } from "@tanstack/react-router";
import { createRoute, Outlet, redirect } from "@tanstack/react-router";
import { Inbox } from "lucide-react";
import { ImDetails, ImKpis, ImOverviewActions } from "./components/ImOverview";
import { IM_ROUTES } from "./constants/routes";
import { ComplaintDetailsPage } from "./pages/employee/ComplaintDetailsPage";
import { CreateIncidentPage } from "./pages/employee/CreateIncidentPage";
import { InboxPage } from "./pages/employee/InboxPage";
import type { ImInboxFilters } from "./types/inbox";
import { IM_ROLES } from "./utils/access";

export interface InboxRouteSearch {
  filter?: ImInboxFilters;
  pageOffset: number;
  pageSize: number;
  nearing?: string;
}

function toFiniteNumber(value: unknown, fallback: number): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

/**
 * Parent route component for all IM pages.
 * Blocks rendering until the rainmaker-im translation module is loaded
 * (either from localStorage cache or fetched from the API on first visit).
 */
function ImModuleWrapper() {
  const { isLoading } = useModuleI18n("im");
  const { t } = useTranslate();

  if (isLoading) {
    return (
      <div className="flex min-h-[200px] items-center justify-center text-sm text-muted-foreground">
        {translateOr(t, "CORE_COMMON_LOADING", "Loading...")}
      </div>
    );
  }

  return <Outlet />;
}

export function createImRoutes(rootRoute: AnyRoute, employeeLayoutRoute: AnyRoute) {
  const basePath = contextPath();
  const imRoot = `/${basePath}${IM_ROUTES.imRoot}`;
  const inboxPath = `/${basePath}${IM_ROUTES.inbox}`;
  const createPath = `/${basePath}${IM_ROUTES.createIncident}`;
  const complaintDetailsPath = `/${basePath}${IM_ROUTES.complaintDetails}/$incidentId/$tenantId`;

  // Parent route — loads rainmaker-im translations before any IM page renders
  const imParentRoute = createRoute({
    getParentRoute: () => employeeLayoutRoute,
    id: "im-module",
    component: ImModuleWrapper,
  });

  const imIndexRoute = createRoute({
    getParentRoute: () => imParentRoute,
    path: imRoot,
    beforeLoad: () => {
      throw redirect({ to: inboxPath });
    },
  });

  const inboxRoute = createRoute({
    getParentRoute: () => imParentRoute,
    path: inboxPath,
    validateSearch: (search: Record<string, unknown>): InboxRouteSearch => ({
      // Stored as a plain object — the router's default codec JSON-encodes/decodes
      // object search values natively, so there's no manual stringify/parse to keep
      // in sync (that mismatch is what previously dropped the filter on reload).
      filter:
        search.filter && typeof search.filter === "object"
          ? (search.filter as ImInboxFilters)
          : undefined,
      pageOffset: toFiniteNumber(search.pageOffset, 0),
      pageSize: toFiniteNumber(search.pageSize, 10),
      nearing:
        search.nearing === undefined || search.nearing === null
          ? undefined
          : String(search.nearing),
    }),
    component: InboxPage,
  });

  const createIncidentRoute = createRoute({
    getParentRoute: () => imParentRoute,
    path: createPath,
    component: CreateIncidentPage,
  });

  const complaintDetailsRoute = createRoute({
    getParentRoute: () => imParentRoute,
    path: complaintDetailsPath,
    component: ComplaintDetailsPage,
  });

  return {
    routes: [
      imParentRoute,
      imIndexRoute,
      inboxRoute,
      createIncidentRoute,
      complaintDetailsRoute,
    ],
    navItems: [
      {
        id: "im-inbox",
        label: "Inbox",
        labelKey: "ES_IM_INBOX",
        to: inboxPath,
        icon: Inbox,
        matchPrefixes: [`/${basePath}${IM_ROUTES.complaintDetails}`],
        roles: [...IM_ROLES],
      },
    ],
  };
}

export function createImModule(rootRoute: AnyRoute, employeeLayoutRoute: AnyRoute) {
  const { routes, navItems } = createImRoutes(rootRoute, employeeLayoutRoute);

  return {
    id: "im",
    order: 1,
    routes,
    navItems,
    overview: { kpis: ImKpis, details: ImDetails, actions: ImOverviewActions },
  };
}
