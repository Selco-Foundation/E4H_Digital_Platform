type ViteEnv = {
  VITE_STATE_LEVEL_TENANT_ID?: string;
  VITE_CONTEXT_PATH?: string;
};

function readViteEnv(): ViteEnv {
  if (typeof import.meta === "undefined") {
    return {};
  }

  return (import.meta as { env?: ViteEnv }).env ?? {};
}

export function getViteEnv(key: keyof ViteEnv, fallback = ""): string {
  const value = readViteEnv()[key];
  return value ?? fallback;
}
