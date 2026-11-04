import { useLocaleStore } from "../stores/locale-store";
import { getDefaultLanguage } from "./locale-utils";

const ACTIVE_LOCALE_KEY = "sem-ui.locale";

export function persistActiveLocale(locale: string): void {
  useLocaleStore.getState().setLocale(locale);

  if (typeof window === "undefined") {
    return;
  }

  try {
    window.localStorage.setItem(ACTIVE_LOCALE_KEY, locale);
  } catch {
    // localStorage write failures (e.g. private mode quota) are non-fatal
  }
}

export function readActiveLocale(): string {
  if (typeof window === "undefined") {
    return getDefaultLanguage();
  }

  return window.localStorage.getItem(ACTIVE_LOCALE_KEY) ?? getDefaultLanguage();
}
