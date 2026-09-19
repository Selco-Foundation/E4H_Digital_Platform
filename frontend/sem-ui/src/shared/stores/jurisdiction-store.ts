import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";
import type { HrmsEmployee } from "../api/hrms";
import type { JurisdictionBoundaries } from "../utils/boundary-util";

interface JurisdictionState {
  /**
   * The scope every existing consumer already reads (`state.boundaries`) —
   * defaults to the user's full assigned jurisdiction but can be narrowed to
   * a single facility via `setCurrentBoundary`, matching DIGIT-UI's
   * ChangeCity.js ("All" vs. one facility) header dropdown. Every page that
   * already calls `aggregateBoundaryCodes(boundaries)` picks up the narrowed
   * scope automatically — no consumer changes needed.
   */
  boundaries: JurisdictionBoundaries | null;
  /** The user's true, full jurisdiction — set once at login, never narrowed. */
  fullBoundaries: JurisdictionBoundaries | null;
  hrmsUser: HrmsEmployee | null;
  setJurisdictionData: (data: {
    boundaries: JurisdictionBoundaries;
    hrmsUser: HrmsEmployee;
  }) => void;
  /** Pass `null` for "All" (resets to the full jurisdiction). */
  setCurrentBoundary: (boundary: JurisdictionBoundaries | null) => void;
  clearJurisdiction: () => void;
}

export const useJurisdictionStore = create<JurisdictionState>()(
  persist(
    (set, get) => ({
      boundaries: null,
      fullBoundaries: null,
      hrmsUser: null,
      setJurisdictionData: ({ boundaries, hrmsUser }) =>
        set({ boundaries, fullBoundaries: boundaries, hrmsUser }),
      setCurrentBoundary: (boundary) =>
        set({ boundaries: boundary ?? get().fullBoundaries }),
      clearJurisdiction: () =>
        set({
          boundaries: null,
          fullBoundaries: null,
          hrmsUser: null,
        }),
    }),
    {
      name: "sem-jurisdiction",
      storage: createJSONStorage(() => localStorage),
    },
  ),
);
