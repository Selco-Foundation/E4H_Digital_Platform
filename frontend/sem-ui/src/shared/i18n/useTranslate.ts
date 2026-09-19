import { useTranslation } from "react-i18next";
import type { TFunction } from "i18next";
import {
  convertToLocale,
  convertToLocaleData,
  getCityLocale,
  getLocalityCode,
  getMohallaLocale,
  getRevenueLocalityCode,
  getTransformedLocale,
  sortDropdownNames,
  toTenantLocale,
} from "./locale-utils";

export interface UseTranslateResult {
  t: TFunction;
  ready: boolean;
  i18n: ReturnType<typeof useTranslation>["i18n"];
  getTransformedLocale: typeof getTransformedLocale;
  getCityLocale: typeof getCityLocale;
  getMohallaLocale: typeof getMohallaLocale;
  getLocalityCode: typeof getLocalityCode;
  getRevenueLocalityCode: typeof getRevenueLocalityCode;
  convertToLocale: typeof convertToLocale;
  convertToLocaleData: typeof convertToLocaleData;
  sortDropdownNames: typeof sortDropdownNames;
  toTenantLocale: typeof toTenantLocale;
}

export function useTranslate(ns = "translations"): UseTranslateResult {
  const { t, i18n, ready } = useTranslation(ns);

  return {
    t,
    ready,
    i18n,
    getTransformedLocale,
    getCityLocale,
    getMohallaLocale,
    getLocalityCode,
    getRevenueLocalityCode,
    convertToLocale,
    convertToLocaleData,
    sortDropdownNames: (options, optionKey) => sortDropdownNames(options, optionKey, t),
    toTenantLocale,
  };
}
