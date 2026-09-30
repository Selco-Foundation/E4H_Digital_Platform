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

/**
 * Matches DIGIT-UI's AppModules.js: only trust the result when it resolves to
 * exactly one state (a user whose jurisdiction spans more than one, or none,
 * gets no CRM number — same as there). Some local/dev configs expose only the
 * older flat CRM_HELPLINE_NUMBER key, so use that when boundary-specific
 * metadata is not available.
 */
export function getCrmHelplineNumber(stateCodes: string[]): string {
  const getStateBoundaryInfos = window.globalConfigs?.getStateBoundaryInfos;

  if (typeof getStateBoundaryInfos !== "function") {
    return getConfigString("CRM_HELPLINE_NUMBER");
  }

  const infos = getStateBoundaryInfos(stateCodes) ?? [];
  return infos.length === 1 ? (infos[0].crmHelplineNumber ?? "") : "";
}
