function modulesListKey(locale: string): string {
  return `sem-ui-i18n.${locale}.__modules`;
}

function allModulesKey(): string {
  return "sem-ui-i18n.__all-modules";
}

function modulePayloadKey(locale: string, module: string): string {
  return `sem-ui-i18n.${locale}.${module}`;
}

function readStringArray(key: string): string[] {
  if (typeof window === "undefined") {
    return [];
  }

  try {
    const raw = window.localStorage.getItem(key);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as unknown;
    return Array.isArray(parsed) ? parsed.filter((item): item is string => typeof item === "string") : [];
  } catch {
    return [];
  }
}

function writeStringArray(key: string, values: string[]): void {
  if (typeof window === "undefined") {
    return;
  }

  try {
    window.localStorage.setItem(key, JSON.stringify(values));
  } catch {
    // localStorage write failures (e.g. private mode quota) are non-fatal
  }
}

export function getLoadedModulesForLocale(locale: string): string[] {
  return readStringArray(modulesListKey(locale));
}

export function getAllKnownModules(): string[] {
  return readStringArray(allModulesKey());
}

export function markModuleLoaded(locale: string, module: string): void {
  const perLocale = new Set(getLoadedModulesForLocale(locale));
  perLocale.add(module);
  writeStringArray(modulesListKey(locale), [...perLocale]);

  const allModules = new Set(getAllKnownModules());
  allModules.add(module);
  writeStringArray(allModulesKey(), [...allModules]);
}

export function removeModuleFromLocale(locale: string, module: string): void {
  const perLocale = getLoadedModulesForLocale(locale).filter((m) => m !== module);
  writeStringArray(modulesListKey(locale), perLocale);

  if (typeof window === "undefined") {
    return;
  }

  try {
    window.localStorage.removeItem(modulePayloadKey(locale, module));
  } catch {
    // localStorage removal failures are non-fatal
  }
}

export function readModulePayload(locale: string, module: string): Record<string, string> | null {
  if (typeof window === "undefined") {
    return null;
  }

  try {
    const raw = window.localStorage.getItem(modulePayloadKey(locale, module));
    if (!raw) return null;
    return JSON.parse(raw) as Record<string, string>;
  } catch {
    return null;
  }
}

export function writeModulePayload(
  locale: string,
  module: string,
  resources: Record<string, string>,
): void {
  if (typeof window === "undefined") {
    return;
  }

  try {
    window.localStorage.setItem(modulePayloadKey(locale, module), JSON.stringify(resources));
  } catch {
    // localStorage write failures (e.g. private mode quota) are non-fatal
  }
}
