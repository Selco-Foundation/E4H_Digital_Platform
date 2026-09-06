import { getViteEnv } from "../env";

export function getConfig(key: string): string | boolean | string[] | undefined {
  return window.globalConfigs?.getConfig(key);
}

export function getConfigString(key: string, fallback = ""): string {
  const value = getConfig(key);
  return typeof value === "string" ? value : fallback;
}

export function contextPath(): string {
  // Deliberately NOT sourced from the shared global config's CONTEXT_PATH key:
  // that config file is shared with the existing DIGIT-UI deployment, where
  // CONTEXT_PATH resolves to "e4hhub" — using it here would make this app's
  // own routes silently collide with the legacy app's base path. This app's
  // path is its own, set locally via VITE_CONTEXT_PATH.
  return getViteEnv("VITE_CONTEXT_PATH", "sem-ui");
}

export function tenantId(envFallback?: string): string {
  return getConfigString(
    "STATE_LEVEL_TENANT_ID",
    envFallback ?? getViteEnv("VITE_STATE_LEVEL_TENANT_ID", "sem"),
  );
}

export function isGlobalConfigLoaded(): boolean {
  return typeof window.globalConfigs?.getConfig === "function";
}
