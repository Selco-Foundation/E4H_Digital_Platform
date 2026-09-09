import { aggregateBoundaryCodes, translateOr, useJurisdictionStore, useTranslate, useBoundary } from "@/shared";
import { Link } from "@tanstack/react-router";
import { Briefcase, X } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { FormSelectField } from "../create/FormSelectField";

interface BoundaryOption {
  code: string;
  name: string;
  parentCode?: string;
}

export interface RmsPausedFilterState {
  state: BoundaryOption | null;
  district: BoundaryOption | null;
  block: BoundaryOption | null;
}

interface RmsPausedFilterProps {
  value: RmsPausedFilterState;
  onChange: (value: RmsPausedFilterState) => void;
  pauseRmsPath: string;
}

/**
 * Matches DIGIT-UI's rmsPaused/Filter.js exactly: single-select State →
 * District → Block, all sourced from `useBoundary` (boundary-service), no
 * separate facility filter on this screen (unlike the inbox filter).
 */
export function RmsPausedFilter({ value, onChange, pauseRmsPath }: RmsPausedFilterProps) {
  const { t } = useTranslate();
  const boundaries = useJurisdictionStore((state) => state.boundaries);
  const jurisdictionCodes = aggregateBoundaryCodes(boundaries);
  const { data: boundaryData } = useBoundary(jurisdictionCodes);

  const [stateMenu, setStateMenu] = useState<BoundaryOption[]>([]);
  const [districtMenu, setDistrictMenu] = useState<BoundaryOption[]>([]);
  const [blockMenu, setBlockMenu] = useState<BoundaryOption[]>([]);

  useEffect(() => {
    if (!boundaryData?.states) {
      return;
    }
    const unique = new Map<string, BoundaryOption>();
    for (const state of boundaryData.states) {
      if (!unique.has(state.code)) {
        unique.set(state.code, { code: state.code, name: translateOr(t, `Boundary_${state.code}`, state.code) });
      }
    }
    setStateMenu([...unique.values()].sort((a, b) => a.name.localeCompare(b.name)));
  }, [boundaryData, t]);

  useEffect(() => {
    if (!boundaryData?.districts || !value.state) {
      setDistrictMenu([]);
      return;
    }
    setDistrictMenu(
      boundaryData.districts
        .filter((district) => district.parentCode === value.state?.code)
        .map((district) => ({
          code: district.code,
          name: translateOr(t, `Boundary_${district.code}`, district.code),
          parentCode: district.parentCode,
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    );
  }, [boundaryData, value.state, t]);

  useEffect(() => {
    if (!boundaryData?.blocks || !value.district) {
      setBlockMenu([]);
      return;
    }
    setBlockMenu(
      boundaryData.blocks
        .filter((block) => block.parentCode === value.district?.code)
        .map((block) => ({
          code: block.code,
          name: translateOr(t, `Boundary_${block.code}`, block.code),
          parentCode: block.parentCode,
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    );
  }, [boundaryData, value.district, t]);

  const hasActiveFilters = Boolean(value.state || value.district || value.block);

  const stateOptions = useMemo(
    () => stateMenu.map((option) => ({ code: option.code, name: option.name })),
    [stateMenu],
  );

  return (
    <div className="space-y-4">
      <div className="livelihood-card space-y-3 p-5">
        <div className="flex items-center gap-2">
          <span className="flex size-9 items-center justify-center rounded-md bg-accent text-primary">
            <Briefcase className="size-4" />
          </span>
          <span className="font-semibold text-ink-950">
            {translateOr(t, "RMS_FACILITIES", "Facilities")}
          </span>
        </div>
        <Link to={pauseRmsPath} className="block text-sm font-semibold text-success-foreground hover:underline">
          {translateOr(t, "ES_IM_PAUSE_RMS", "Pause RMS")}
        </Link>
      </div>

      <div className="livelihood-card space-y-4 p-5">
        <div className="flex items-center justify-between">
          <span className="text-sm font-semibold text-ink-950">
            {translateOr(t, "ES_COMMON_FILTER_BY", "Filter By")}:
          </span>
          {hasActiveFilters ? (
            <button
              type="button"
              className="cursor-pointer text-sm text-destructive hover:underline"
              onClick={() => onChange({ state: null, district: null, block: null })}
            >
              {translateOr(t, "ES_COMMON_CLEAR", "Clear")}
            </button>
          ) : null}
        </div>

        <FormSelectField
          label={translateOr(t, "CS_STATE", "State")}
          value={value.state?.code ?? ""}
          options={stateOptions}
          onChange={(option) =>
            onChange({
              state: stateMenu.find((entry) => entry.code === option?.code) ?? null,
              district: null,
              block: null,
            })
          }
        />
        {value.state ? (
          <span className="inline-flex items-center gap-2 rounded-full bg-muted px-3 py-1 text-xs text-foreground">
            {value.state.name}
            <button type="button" onClick={() => onChange({ state: null, district: null, block: null })}>
              <X className="size-3" />
            </button>
          </span>
        ) : null}

        <FormSelectField
          label={translateOr(t, "CS_DISTRICT", "District")}
          value={value.district?.code ?? ""}
          options={districtMenu}
          disabled={!value.state}
          onChange={(option) =>
            onChange({
              ...value,
              district: districtMenu.find((entry) => entry.code === option?.code) ?? null,
              block: null,
            })
          }
        />

        <FormSelectField
          label={translateOr(t, "CS_BLOCK", "Block")}
          value={value.block?.code ?? ""}
          options={blockMenu}
          disabled={!value.district}
          onChange={(option) =>
            onChange({
              ...value,
              block: blockMenu.find((entry) => entry.code === option?.code) ?? null,
            })
          }
        />
      </div>
    </div>
  );
}
