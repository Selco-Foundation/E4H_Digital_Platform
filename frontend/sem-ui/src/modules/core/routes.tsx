import { contextPath, employeeHomePath, useAuthStore } from "@/shared";
import { createRoute, redirect } from "@tanstack/react-router";
import type { AnyRoute } from "@tanstack/react-router";
import { AppShell } from "./layout/AppShell";
import { LoginPage } from "./pages/employee/LoginPage";
import { HomePage } from "./pages/employee/HomePage";
import { ForgotPasswordPage } from "./pages/employee/ForgotPasswordPage";
import { ProfilePage } from "./pages/employee/ProfilePage";
import { ProfileChangePasswordPage } from "./pages/employee/ProfileChangePasswordPage";
import { CORE_ROUTES } from "./constants/routes";

export interface LoginRouteSearch {
  from?: string;
  username?: string;
  tenantId?: string;
  facilityId?: string;
}

export function createCoreRoutes(rootRoute: AnyRoute) {
  const basePath = contextPath();
  const employeeHome = `/${basePath}${CORE_ROUTES.employeeHome}`;
  const employeeLogin = `/${basePath}${CORE_ROUTES.employeeLogin}`;
  const employeeForgotPassword = `/${basePath}${CORE_ROUTES.employeeForgotPassword}`;

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

  const employeeForgotPasswordRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: employeeForgotPassword,
    beforeLoad: () => {
      if (useAuthStore.getState().isAuthenticated) {
        throw redirect({ to: employeeHome });
      }
    },
    component: ForgotPasswordPage,
  });

  // The OTP + new-password step (employeeChangePasswordPath/ChangePasswordPage)
  // isn't implemented yet — ForgotPasswordPage currently just sends the OTP and
  // stops there. Add that route back here once that page exists again.
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
      employeeForgotPasswordRoute,
      employeeLayoutRoute,
      employeeHomeRoute,
      employeeProfileRoute,
      employeeProfileChangePasswordRoute,
    ],
    navItems: [],
    employeeLayoutRoute,
  };
}
