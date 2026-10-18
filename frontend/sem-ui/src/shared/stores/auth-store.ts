import { create } from "zustand";
import { persist } from "zustand/middleware";
import { queryClient } from "../query/query-client";

export interface AuthUser {
  uuid?: string;
  name?: string;
  userName?: string;
  tenantId?: string;
  roles?: Array<{ code?: string; name?: string; tenantId?: string }>;
}

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: AuthUser | null;
  employeeTenantId: string | null;
  isAuthenticated: boolean;
  setSession: (session: {
    accessToken: string;
    refreshToken?: string;
    user?: AuthUser | null;
    employeeTenantId?: string | null;
  }) => void;
  setUser: (user: AuthUser) => void;
  clearSession: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      employeeTenantId: null,
      isAuthenticated: false,
      setSession: ({ accessToken, refreshToken, user, employeeTenantId }) =>
        set({
          accessToken,
          refreshToken: refreshToken ?? null,
          user: user ?? null,
          employeeTenantId: employeeTenantId ?? user?.tenantId ?? null,
          isAuthenticated: Boolean(accessToken),
        }),
      setUser: (user) => set({ user }),
      clearSession: () => {
        queryClient.clear();
        set({
          accessToken: null,
          refreshToken: null,
          user: null,
          employeeTenantId: null,
          isAuthenticated: false,
        });
      },
    }),
    {
      name: "sem-auth",
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        employeeTenantId: state.employeeTenantId,
        isAuthenticated: state.isAuthenticated,
      }),
    },
  ),
);
