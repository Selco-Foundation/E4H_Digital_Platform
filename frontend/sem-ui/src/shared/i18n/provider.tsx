import { I18nextProvider } from "react-i18next";
import { useEffect, useState, type ReactNode } from "react";
import { tenantId } from "../config/global-config";
import { getViteEnv } from "../env";
import { useAuthStore } from "../stores/auth-store";
import { initI18n, i18n } from "./index";

interface I18nProviderProps {
  children: ReactNode;
}

export function I18nProvider({ children }: I18nProviderProps) {
  const [ready, setReady] = useState(i18n.isInitialized);
  const employeeTenantId = useAuthStore((state) => state.employeeTenantId);

  useEffect(() => {
    let cancelled = false;

    void initI18n({
      tenantId: employeeTenantId ?? tenantId(getViteEnv("VITE_STATE_LEVEL_TENANT_ID")),
    }).then(() => {
      if (!cancelled) {
        setReady(true);
      }
    });

    return () => {
      cancelled = true;
    };
  }, [employeeTenantId]);

  if (!ready) {
    return (
      <div className="flex min-h-screen items-center justify-center text-sm text-muted-foreground">
        Loading translations...
      </div>
    );
  }

  return <I18nextProvider i18n={i18n}>{children}</I18nextProvider>;
}
