import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";
import type { HrmsEmployee } from "../api/hrms";
import type { JurisdictionBoundaries } from "../utils/boundary-util";

interface JurisdictionState {
  boundaries: JurisdictionBoundaries | null;
  hrmsUser: HrmsEmployee | null;
  setJurisdictionData: (data: {
    boundaries: JurisdictionBoundaries;
    hrmsUser: HrmsEmployee;
  }) => void;
  clearJurisdiction: () => void;
}

export const useJurisdictionStore = create<JurisdictionState>()(
  persist(
    (set) => ({
      boundaries: null,
      hrmsUser: null,
      setJurisdictionData: ({ boundaries, hrmsUser }) =>
        set({ boundaries, hrmsUser }),
      clearJurisdiction: () =>
        set({
          boundaries: null,
          hrmsUser: null,
        }),
    }),
    {
      name: "sem-jurisdiction",
      storage: createJSONStorage(() => localStorage),
    },
  ),
);
