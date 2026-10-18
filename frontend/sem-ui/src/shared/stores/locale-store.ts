import { create } from "zustand";
import { readActiveLocale } from "../i18n/locale-persistence";

interface LocaleState {
  locale: string;
  setLocale: (locale: string) => void;
}

export const useLocaleStore = create<LocaleState>((set) => ({
  locale: readActiveLocale(),
  setLocale: (locale) => set({ locale }),
}));
