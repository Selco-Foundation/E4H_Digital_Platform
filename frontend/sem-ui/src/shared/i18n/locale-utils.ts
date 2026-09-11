import { getConfigString } from "../config/global-config";

export function getLocaleRegion(): string {
  return getConfigString("LOCALE_REGION", "IN");
}

export function getLocaleDefault(): string {
  return getConfigString("LOCALE_DEFAULT", "en");
}

export function getDefaultLanguage(): string {
  return `${getLocaleDefault()}_${getLocaleRegion()}`;
}

export function normalizeLocale(locale: string): string {
  const region = getLocaleRegion();
  if (!locale.includes(region)) {
    return `${locale}_${region}`;
  }
  return locale;
}

export function toTenantLocale(tenantId: string): string {
  return tenantId.replace(/\./g, "_").toUpperCase();
}

export function checkForNotNull(value = ""): boolean {
  return Boolean(value && value != null && value !== undefined && value !== "");
}

export function stringReplaceAll(str = "", searcher = "", replaceWith = ""): string {
  if (searcher === "") return str;
  let result = str;
  while (result.includes(searcher)) {
    result = result.replace(searcher, replaceWith);
  }
  return result;
}

export function convertDotValues(value = ""): string {
  if (!checkForNotNull(value)) {
    return "NA";
  }
  return value.replaceAll
    ? value.replaceAll(".", "_")
    : stringReplaceAll(value, ".", "_");
}

export function convertToLocale(value = "", key = ""): string {
  const convertedValue = convertDotValues(value).toUpperCase();
  if (convertedValue === "NA") {
    return "COMMON_NA";
  }
  return `${key}_${convertedValue}`;
}

export function getMohallaLocale(value = "", tenantId = ""): string {
  const convertedValue = convertDotValues(tenantId);
  if (convertedValue === "NA" || !checkForNotNull(value)) {
    return "COMMON_NA";
  }
  return convertToLocale(value, `${convertedValue.toUpperCase()}_REVENUE`);
}

export function getCityLocale(value = ""): string {
  const convertedValue = convertDotValues(value);
  if (convertedValue === "NA" || !checkForNotNull(value)) {
    return "COMMON_NA";
  }
  return convertToLocale(convertedValue.toUpperCase(), "TENANT_TENANTS");
}

export function getLocalityCode(
  locality: string | { code?: string },
  tenantId: string,
): string {
  if (typeof locality === "string") {
    return locality.includes("_")
      ? locality
      : `${toTenantLocale(tenantId)}_ADMIN_${locality}`;
  }
  if (locality.code) {
    return locality.code.includes("_")
      ? locality.code
      : `${toTenantLocale(tenantId)}_ADMIN_${locality.code}`;
  }
  return "COMMON_NA";
}

export function getRevenueLocalityCode(
  locality: string | { code?: string },
  tenantId: string,
): string {
  if (typeof locality === "string") {
    return locality.includes("_")
      ? locality
      : `${toTenantLocale(tenantId)}_REVENUE_${locality}`;
  }
  if (locality.code) {
    return locality.code.includes("_")
      ? locality.code
      : `${toTenantLocale(tenantId)}_REVENUE_${locality.code}`;
  }
  return "COMMON_NA";
}

export function getTransformedLocale(label?: string | number): string | number {
  if (typeof label === "number") return label;
  const trimmed = label?.trim();
  if (!trimmed) return "";
  return trimmed.toUpperCase().replace(/[.:-\s/]/g, "_");
}

export function convertToLocaleData<T extends { code: string }>(
  dropdownValues: T[] = [],
  key = "",
  t?: (code: string) => string,
): Array<T & { i18text: string }> {
  return dropdownValues.map((item) => {
    const i18text = convertToLocale(item.code, key);
    return {
      ...item,
      i18text: t ? t(i18text) : i18text,
    };
  });
}

export function sortDropdownNames<T extends Record<string, unknown>>(
  options: T[] = [],
  optionKey = "i18nKey",
  t: (code: string) => string,
): T[] {
  return [...options].sort((a, b) =>
    t(String(a[optionKey] ?? "")).localeCompare(t(String(b[optionKey] ?? ""))),
  );
}

export const NAMESPACE_TO_DIGIT_MODULE: Record<string, string> = {
  common: "rainmaker-common",
  translations: "rainmaker-common",
};

export function namespaceToDigitModule(namespace: string, stateTenantId: string): string {
  if (namespace === "common" || namespace === "translations") {
    return "rainmaker-common";
  }
  if (namespace.startsWith("rainmaker-")) {
    return namespace;
  }
  if (namespace === "state") {
    return `rainmaker-${stateTenantId.toLowerCase()}`;
  }
  return `rainmaker-${namespace.toLowerCase()}`;
}

export function getDefaultLocalizationModules(stateTenantId: string): string[] {
  const stateModule = `rainmaker-${stateTenantId.toLowerCase()}`;
  return ["rainmaker-common", stateModule];
}
