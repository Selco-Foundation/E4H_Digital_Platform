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

interface GlobalConfigs {
  getConfig: (key: string) => string | boolean | string[] | undefined;
}

interface Window {
  globalConfigs?: GlobalConfigs;
}
