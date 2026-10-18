import {
  aggregateBoundaryCodes,
  aggregateBoundaryTypes,
  translateOr,
  useBoundary,
  useFacility,
  useJurisdictionStore,
  useTranslate,
  type JurisdictionBoundaries,
} from "@/shared";
import { ChevronDown } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Button } from "./ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";

interface ScopeOption {
  code: string;
  label: string;
  type: "UNIFIED" | "facility";
}

/**
 * Matches DIGIT-UI's header "All"/facility dropdown (ChangeCity.js): lists
 * the user's full jurisdiction as "All" plus one entry per individual
 * facility within it (boundary-service + facility-service, same hooks used
 * everywhere else). Selecting a facility narrows `useJurisdictionStore`'s
 * `boundaries` to just that facility (via `setCurrentBoundary`), which every
 * existing boundary-scoped query already reads — no other call site needs to
 * change. Selecting "All" resets it back to the full jurisdiction.
 */
export function FacilityScopeSwitcher() {
  const { t } = useTranslate();
  const fullBoundaries = useJurisdictionStore((state) => state.fullBoundaries);
  const currentBoundaries = useJurisdictionStore((state) => state.boundaries);
  const setCurrentBoundary = useJurisdictionStore((state) => state.setCurrentBoundary);

  const fullJurisdictionCodes = useMemo(
    () => aggregateBoundaryCodes(fullBoundaries),
    [fullBoundaries],
  );
  const { data: boundaryData } = useBoundary(fullJurisdictionCodes);
  const [facilityBoundaryCodes, setFacilityBoundaryCodes] = useState<string[]>(["-"]);

  useEffect(() => {
    if (boundaryData?.facilities) {
      setFacilityBoundaryCodes(
        boundaryData.facilities.map((facility) => facility.code).filter(Boolean),
      );
    }
  }, [boundaryData]);

  const { data: facilityData } = useFacility(facilityBoundaryCodes);

  const options = useMemo<ScopeOption[]>(() => {
    const jurisdictionTypes = aggregateBoundaryTypes(fullBoundaries);
    const isOnlyFacilityType =
      jurisdictionTypes.length === 1 && jurisdictionTypes[0] === "facility";

    const allLabel =
      fullJurisdictionCodes.length === 1 && isOnlyFacilityType
        ? translateOr(t, `Boundary_${fullJurisdictionCodes[0]}`, fullJurisdictionCodes[0])
        : translateOr(t, "CORE_COMMON_ALL", "All");

    const result: ScopeOption[] = [
      { code: fullJurisdictionCodes.join(","), label: allLabel, type: "UNIFIED" },
    ];

    for (const facility of facilityData?.facilities ?? []) {
      if (result.every((option) => option.code !== facility.boundaryCode)) {
        result.push({
          code: facility.boundaryCode,
          label: translateOr(t, `Boundary_${facility.boundaryCode}`, facility.boundaryCode),
          type: "facility",
        });
      }
    }

    return result.sort((a, b) => a.label.localeCompare(b.label));
  }, [facilityData, fullBoundaries, fullJurisdictionCodes, t]);

  const currentCodes = aggregateBoundaryCodes(currentBoundaries).join(",");
  const selected =
    options.find((option) => option.code === currentCodes) ?? options[0];

  function handleSelect(option: ScopeOption) {
    if (option.code === selected?.code) {
      return;
    }
    const nextBoundary: JurisdictionBoundaries | null =
      option.type === "UNIFIED" ? null : { facility: option.code.split(",") };
    setCurrentBoundary(nextBoundary);
    window.location.reload();
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" size="sm" className="min-w-40 justify-between gap-2">
          <span className="truncate">
            {selected?.label ?? translateOr(t, "CORE_COMMON_ALL", "All")}
          </span>
          <ChevronDown className="size-4 shrink-0 opacity-60" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent
        align="end"
        className="max-h-72 w-[var(--radix-dropdown-menu-trigger-width)] min-w-32 overflow-y-auto"
      >
        {options.map((option) => (
          <DropdownMenuItem
            key={option.code}
            className="cursor-pointer"
            onClick={() => handleSelect(option)}
          >
            {option.label}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
