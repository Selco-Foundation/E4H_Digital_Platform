import { useEffect, useState } from "react";
import { loadModules } from "./index";

/**
 * Lazily loads translations for a single module when a component first mounts.
 *
 * On first visit: fetches from the localization API and caches in localStorage.
 * On subsequent visits / page refreshes: restores from localStorage cache with no API call.
 *
 * Usage inside a module's root layout or route wrapper:
 *   const { isLoading } = useModuleI18n("im");  // loads "rainmaker-im"
 */
export function useModuleI18n(moduleCode: string): { isLoading: boolean } {
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    void loadModules([`rainmaker-${moduleCode.toLowerCase()}`]).then(() => {
      setIsLoading(false);
    });
  }, [moduleCode]);

  return { isLoading };
}
