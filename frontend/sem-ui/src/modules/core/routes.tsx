import { contextPath, employeeHomePath, useAuthStore } from "@/shared";
import { createRoute, redirect } from "@tanstack/react-router";
import type { AnyRoute } from "@tanstack/react-router";
import { AppShell } from "./layout/AppShell";
import { LoginPage } from "./pages/employee/LoginPage";
import { HomePage } from "./pages/employee/HomePage";
import { ProfilePage } from "./pages/employee/ProfilePage";
import { ProfileChangePasswordPage } from "./pages/employee/ProfileChangePasswordPage";
import { CORE_ROUTES } from "./constants/routes";

export interface LoginRouteSearch {
  from?: string;
  username?: string;
  tenantId?: string;
  facilityId?: string;
}

// ForgotPassword / ChangePassword (pre-login reset) pages aren't ported yet —
// only login, the authenticated home shell, and profile/profile-change-password.
// See modules/core in livelihood-ui for the original shape of the remaining
// two: employeeForgotPasswordRoute, employeeChangePasswordRoute.
export function createCoreRoutes(rootRoute: AnyRoute) {
  const basePath = contextPath();
  const employeeHome = `/${basePath}${CORE_ROUTES.employeeHome}`;
  const employeeLogin = `/${basePath}${CORE_ROUTES.employeeLogin}`;

  const indexRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: "/",
    beforeLoad: () => {
      throw redirect({ to: employeeHome });
    },
  });

  const contextRootRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: `/${basePath}`,
    beforeLoad: () => {
      throw redirect({ to: employeeHome });
    },
  });

  const employeeLoginRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: employeeLogin,
    validateSearch: (search: Record<string, unknown>): LoginRouteSearch => ({
      from: typeof search.from === "string" ? search.from : undefined,
      username: typeof search.username === "string" ? search.username : undefined,
      tenantId: typeof search.tenantId === "string" ? search.tenantId : undefined,
      facilityId: typeof search.facilityId === "string" ? search.facilityId : undefined,
    }),
    beforeLoad: () => {
      if (useAuthStore.getState().isAuthenticated) {
        throw redirect({ to: employeeHome });
      }
    },
    component: LoginPage,
  });

  const employeeLayoutRoute = createRoute({
    getParentRoute: () => rootRoute,
    id: "employee-layout",
    beforeLoad: ({ location }) => {
      if (!useAuthStore.getState().isAuthenticated) {
        throw redirect({
          to: employeeLogin,
          search: {
            from: location.href,
          },
        });
      }
    },
    component: () => <AppShell />,
  });

  const employeeHomeRoute = createRoute({
    getParentRoute: () => employeeLayoutRoute,
    path: employeeHome,
    component: HomePage,
  });

  const employeeProfile = `/${basePath}${CORE_ROUTES.employeeProfile}`;
  const employeeProfileRoute = createRoute({
    getParentRoute: () => employeeLayoutRoute,
    path: employeeProfile,
    component: ProfilePage,
  });

  const employeeProfileChangePassword = `/${basePath}${CORE_ROUTES.employeeProfileChangePassword}`;
  const employeeProfileChangePasswordRoute = createRoute({
    getParentRoute: () => employeeLayoutRoute,
    path: employeeProfileChangePassword,
    component: ProfileChangePasswordPage,
  });

  return {
    routes: [
      indexRoute,
      contextRootRoute,
      employeeLoginRoute,
      employeeLayoutRoute,
      employeeHomeRoute,
      employeeProfileRoute,
      employeeProfileChangePasswordRoute,
    ],
    navItems: [],
    employeeLayoutRoute,
  };
}
