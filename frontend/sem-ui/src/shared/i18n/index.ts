import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import { fetchLocalization } from "../api/localization";
import { tenantId as resolveTenantId } from "../config/global-config";
import { getViteEnv } from "../env";
import { useAuthStore } from "../stores/auth-store";
import { persistActiveLocale, readActiveLocale } from "./locale-persistence";
import {
  getAllKnownModules,
  getLoadedModulesForLocale,
  markModuleLoaded,
  readModulePayload,
  removeModuleFromLocale,
  writeModulePayload,
} from "./module-cache";
import { getDefaultLocalizationModules, normalizeLocale } from "./locale-utils";

const TRANSLATIONS_NS = "translations";

function getActiveTenantId(explicitTenantId?: string): string {
  if (explicitTenantId) {
    return explicitTenantId;
  }

  const employeeTenant = useAuthStore.getState().employeeTenantId;
  return employeeTenant ?? resolveTenantId(getViteEnv("VITE_STATE_LEVEL_TENANT_ID"));
}

async function fetchAndApplyModules(
  modules: string[],
  locale: string,
  tenant: string,
): Promise<void> {
  const normalizedLocale = normalizeLocale(locale);
  const loadedForLocale = new Set(getLoadedModulesForLocale(normalizedLocale));
  const modulesToEnsure = [...new Set([...modules, ...getAllKnownModules()])];

  for (const module of modulesToEnsure) {
    try {
      if (loadedForLocale.has(module)) {
        const cached = readModulePayload(normalizedLocale, module);
        if (cached) {
          i18n.addResources(normalizedLocale, TRANSLATIONS_NS, cached);
          continue;
        }
        // Marked loaded but the payload is missing (e.g. a prior write failed) —
        // fall through and refetch instead of leaving the module untranslated.
      }

      const resources = await fetchLocalization({
        locale: normalizedLocale,
        tenantId: tenant,
        modules: [module],
      });

      writeModulePayload(normalizedLocale, module, resources);
      i18n.addResources(normalizedLocale, TRANSLATIONS_NS, resources);
      markModuleLoaded(normalizedLocale, module);
    } catch (error) {
      console.error(
        `[i18n] failed to load module "${module}" for locale "${normalizedLocale}"`,
        error,
      );
    }
  }
}

let instanceReady = false;

export interface InitI18nOptions {
  locale?: string;
  tenantId?: string;
  modules?: string[];
}

async function ensureI18nInstance(locale: string): Promise<void> {
  if (instanceReady) {
    return;
  }

  await i18n.use(initReactI18next).init({
    lng: locale,
    fallbackLng: "en_IN",
    debug: false,
    ns: [TRANSLATIONS_NS],
    defaultNS: TRANSLATIONS_NS,
    keySeparator: false,
    interpolation: {
      escapeValue: false,
      formatSeparator: ",",
    },
    react: {
      useSuspense: false,
      bindI18n: "languageChanged loaded",
      bindI18nStore: "added",
    },
    resources: {
      [locale]: {
        [TRANSLATIONS_NS]: {},
      },
    },
  });

  instanceReady = true;
}

export async function initI18n(options: InitI18nOptions = {}): Promise<typeof i18n> {
  const locale = normalizeLocale(options.locale ?? readActiveLocale());
  const tenant = getActiveTenantId(options.tenantId);
  const modules = options.modules ?? getDefaultLocalizationModules(tenant);

  persistActiveLocale(locale);
  await ensureI18nInstance(locale);
  await loadModules(modules, locale, tenant);
  await i18n.changeLanguage(locale);
  return i18n;
}

export async function loadModules(
  modules: string[],
  locale?: string,
  tenantId?: string,
): Promise<void> {
  const activeLocale = normalizeLocale(locale ?? readActiveLocale());
  const tenant = getActiveTenantId(tenantId);
  await fetchAndApplyModules(modules, activeLocale, tenant);
}

/**
 * Forces a fresh re-fetch of a module's translations from the API for one locale.
 *
 * Use this when you need to invalidate and refresh a module's translations at runtime,
 * e.g. after an admin updates localization keys.
 *
 * @param moduleCode  Short module name, e.g. "im" → resolves to "rainmaker-im"
 */
export async function reloadModule(
  moduleCode: string,
  locale?: string,
  tenantId?: string,
): Promise<void> {
  const fullModuleName = `rainmaker-${moduleCode.toLowerCase()}`;
  const activeLocale = normalizeLocale(locale ?? readActiveLocale());

  removeModuleFromLocale(activeLocale, fullModuleName);
  await loadModules([fullModuleName], activeLocale, tenantId);
}

export async function setLocale(
  locale: string,
  tenantId?: string,
): Promise<void> {
  const normalizedLocale = normalizeLocale(locale);
  const tenant = getActiveTenantId(tenantId);
  const modules = getDefaultLocalizationModules(tenant);

  await loadModules(modules, normalizedLocale, tenant);
  await i18n.changeLanguage(normalizedLocale);
  persistActiveLocale(normalizedLocale);
}

export { i18n };
export { useModuleI18n } from "./useModuleI18n";
export { translateOr } from "./translate-or";
