/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_PROXY_API?: string;
  readonly VITE_STATE_LEVEL_TENANT_ID?: string;
  readonly VITE_CONTEXT_PATH?: string;
  readonly VITE_GLOBAL_CONFIG_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

interface StateBoundaryInfo {
  crmHelplineNumber?: string;
  logos?: unknown[];
  languages?: unknown[];
}

interface GlobalConfigs {
  getConfig: (key: string) => string | boolean | string[] | undefined;
  /** Same runtime global DIGIT-UI's AppModules.js reads for the sidebar's CRM
   * toll-number row — not part of `getConfig`, provided separately by the
   * hosting globalConfigs script. */
  getStateBoundaryInfos?: (stateCodes: string[]) => StateBoundaryInfo[];
}

interface Window {
  globalConfigs?: GlobalConfigs;
}
