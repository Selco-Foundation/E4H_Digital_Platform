import {
  aggregateBoundaryCodes,
  translateOr,
  useAuthStore,
  useBoundary,
  useFacility,
  useJurisdictionStore,
  useTranslate,
} from "@/shared";
import {
  Button,
  Checkbox,
  Input,
  Popover,
  PopoverContent,
  PopoverTrigger,
  ScrollArea,
  Separator,
  Sheet,
  SheetContent,
  SheetFooter,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
  cn,
} from "@/ui";
import { ChevronDown, Filter } from "lucide-react";
import { useEffect, useMemo, useState, type ReactNode } from "react";
import { ORDERED_INBOX_STATUSES } from "../../constants/inbox-statuses";
import { buildDefaultInboxRoleFilters } from "../../hooks/inbox-defaults";
import { useImAssetTypes } from "../../hooks/use-im-inbox-summary";
import type { ImInboxFilters, InboxDataResult } from "../../types/inbox";
import { isEndUser } from "../../utils/access";
import { buildFilterQueryFromState } from "../../utils/inbox-filters";

interface FilterOption {
  code: string;
  name: string;
  key?: string;
  parentCode?: string;
}

function codesOf(items: Array<{ code: string }>): Set<string> {
  return new Set(items.map((item) => item.code));
}

function pruneToValidCodes<T extends { code: string }>(items: T[], validCodes: Set<string>): T[] {
  const filtered = items.filter((item) => validCodes.has(item.code));
  return filtered.length === items.length ? items : filtered;
}

function areAllStatusesSelected(
  selected: Array<{ code: string }>,
  statuses: readonly string[],
): boolean {
  const selectedCodes = codesOf(selected);
  return statuses.every((code) => selectedCodes.has(code));
}

type PgrFilterKey = "assetType" | "facility" | "state" | "district" | "block";

interface InboxFilterProps {
  complaints?: InboxDataResult;
  searchParams: { filters?: ImInboxFilters };
  onFilterChange: (filters: ImInboxFilters) => void;
}

export function InboxFilter({
  searchParams,
  onFilterChange,
}: InboxFilterProps) {
  const { t } = useTranslate();
  const user = useAuthStore((state) => state.user);
  const userUuid = user?.uuid ?? "";
  const roles = user?.roles;
  const boundaries = useJurisdictionStore((state) => state.boundaries);
  const jurisdictionCodes = aggregateBoundaryCodes(boundaries);

  const assignedToOptions = useMemo(
    () => [
      { code: "ASSIGNED_TO_ME", name: translateOr(t, "ASSIGNED_TO_ME", "My Tickets") },
      { code: "ASSIGNED_TO_ALL", name: translateOr(t, "ASSIGNED_TO_ALL", "All Tickets") },
    ],
    [t],
  );

  const defaultFilters = buildDefaultInboxRoleFilters(user);
  const showGeoFilters = !isEndUser(roles);

  const [selectAssigned, setSelectAssigned] = useState(
    searchParams.filters?.wfFilters?.assignee?.[0]?.code === userUuid
      ? assignedToOptions[0]
      : assignedToOptions[1],
  );

  // Two independent open states, not one shared boolean: the desktop Popover's
  // content portals to document.body regardless of its trigger's CSS visibility,
  // so sharing a single `open` state with the mobile Sheet opens both Radix
  // overlays at once and their dismiss-on-outside-click layers fight, closing
  // one another immediately.
  const [desktopFiltersOpen, setDesktopFiltersOpen] = useState(false);
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);
  const [activeCategory, setActiveCategory] = useState<PgrFilterKey | "applicationStatus">(
    "assetType",
  );
  const [categorySearch, setCategorySearch] = useState("");

  const emptyPgrFilters = {
    assetType: [] as Array<{ code: string; name?: string; key?: string }>,
    facility: [] as Array<{ code: string; name?: string }>,
    state: [] as Array<{ code: string; name?: string }>,
    district: [] as Array<{ code: string; name?: string }>,
    block: [] as Array<{ code: string; name?: string }>,
    applicationStatus: [] as Array<{ code: string }>,
  };

  const [pgrfilters, setPgrFilters] = useState({
    ...emptyPgrFilters,
    ...(searchParams.filters?.pgrfilters ?? defaultFilters.pgrfilters),
  });

  const [wfFilters, setWfFilters] = useState(
    searchParams.filters?.wfFilters ?? defaultFilters.wfFilters!,
  );

  const [stateMenu, setStateMenu] = useState<FilterOption[]>([]);
  const [districtMenu, setDistrictMenu] = useState<FilterOption[]>([]);
  const [blockMenu, setBlockMenu] = useState<FilterOption[]>([]);
  const [facilityMenu, setFacilityMenu] = useState<FilterOption[]>([]);
  const [facilityOptions, setFacilityOptions] = useState<FilterOption[]>([]);
  const [facilityBoundaries, setFacilityBoundaries] = useState<FilterOption[]>([]);
  const [facilityBoundaryCodes, setFacilityBoundaryCodes] = useState<string[]>(["-"]);

  const { data: boundaryData } = useBoundary(jurisdictionCodes);
  const { data: facilityData } = useFacility(facilityBoundaryCodes);
  const { data: assetTypes } = useImAssetTypes();

  const assetTypeMenu = useMemo(
    () =>
      (assetTypes ?? [])
        .map((item) => ({
          code: item.code,
          name: translateOr(t, `ASSETTYPE_${item.code}`, item.code),
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    [assetTypes, t],
  );

  const statusMenu = useMemo(
    () =>
      ORDERED_INBOX_STATUSES.map((status) => ({
        code: status.code,
        name: translateOr(t, `CS_COMMON_${status.code}`, status.code),
      })),
    [t],
  );

  useEffect(() => {
    if (boundaryData?.facilities) {
      setFacilityBoundaries(
        boundaryData.facilities.map((facility) => ({
          code: facility.code,
          name: facility.code,
          parentCode: facility.parentCode,
        })),
      );
      setFacilityBoundaryCodes(
        boundaryData.facilities.map((facility) => facility.code).filter(Boolean),
      );
    }
  }, [boundaryData]);

  useEffect(() => {
    if (facilityBoundaries.length && facilityData?.facilities?.length) {
      const parentMap = new Map(
        facilityBoundaries.map((facility) => [facility.code, facility.parentCode]),
      );
      setFacilityOptions(
        facilityData.facilities.map((facility) => ({
          code: facility.boundaryCode,
          name: facility.boundaryCode,
          parentCode: parentMap.get(facility.boundaryCode),
        })),
      );
    }
  }, [facilityBoundaries, facilityData]);

  useEffect(() => {
    if (boundaryData?.states) {
      const unique = new Map<string, FilterOption>();
      for (const state of boundaryData.states) {
        if (!unique.has(state.code)) {
          unique.set(state.code, {
            code: state.code,
            name: translateOr(t, `BOUNDARY_${state.code}`, state.code),
          });
        }
      }
      setStateMenu([...unique.values()].sort((a, b) => a.name.localeCompare(b.name)));
    }
  }, [boundaryData, t]);

  useEffect(() => {
    if (!boundaryData?.districts) {
      setDistrictMenu([]);
      return;
    }
    const selectedStateCodes = pgrfilters.state.map((item) => item.code);
    const districts = selectedStateCodes.length
      ? boundaryData.districts.filter(
          (district) => district.parentCode && selectedStateCodes.includes(district.parentCode),
        )
      : boundaryData.districts;

    setDistrictMenu(
      districts
        .map((district) => ({
          code: district.code,
          name: translateOr(t, `BOUNDARY_${district.code}`, district.code),
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    );

    const validDistrictCodes = codesOf(districts);
    setPgrFilters((prev) => {
      const pruned = pruneToValidCodes(prev.district, validDistrictCodes);
      return pruned === prev.district ? prev : { ...prev, district: pruned };
    });
  }, [pgrfilters.state, boundaryData, t]);

  useEffect(() => {
    if (!boundaryData?.blocks) {
      setBlockMenu([]);
      return;
    }
    const selectedStateCodes = pgrfilters.state.map((item) => item.code);
    const selectedDistrictCodes = pgrfilters.district.map((item) => item.code);

    let blocks = boundaryData.blocks;
    if (selectedDistrictCodes.length) {
      blocks = blocks.filter(
        (block) => block.parentCode && selectedDistrictCodes.includes(block.parentCode),
      );
    } else if (selectedStateCodes.length && boundaryData.districts) {
      const districtCodes = codesOf(
        boundaryData.districts.filter(
          (district) => district.parentCode && selectedStateCodes.includes(district.parentCode),
        ),
      );
      blocks = blocks.filter((block) => block.parentCode && districtCodes.has(block.parentCode));
    }

    setBlockMenu(
      blocks
        .map((block) => ({
          code: block.code,
          name: translateOr(t, `BOUNDARY_${block.code}`, block.code),
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    );

    const validBlockCodes = codesOf(blocks);
    setPgrFilters((prev) => {
      const pruned = pruneToValidCodes(prev.block, validBlockCodes);
      return pruned === prev.block ? prev : { ...prev, block: pruned };
    });
  }, [pgrfilters.state, pgrfilters.district, boundaryData, t]);

  useEffect(() => {
    const selectedStateCodes = pgrfilters.state.map((item) => item.code);
    const selectedDistrictCodes = pgrfilters.district.map((item) => item.code);
    const selectedBlockCodes = pgrfilters.block.map((item) => item.code);

    let facilities = facilityOptions;
    if (selectedBlockCodes.length) {
      facilities = facilities.filter(
        (facility) => facility.parentCode && selectedBlockCodes.includes(facility.parentCode),
      );
    } else if (selectedDistrictCodes.length && boundaryData?.blocks) {
      const blockCodes = codesOf(
        boundaryData.blocks.filter(
          (block) => block.parentCode && selectedDistrictCodes.includes(block.parentCode),
        ),
      );
      facilities = facilities.filter(
        (facility) => facility.parentCode && blockCodes.has(facility.parentCode),
      );
    } else if (selectedStateCodes.length && boundaryData?.districts && boundaryData?.blocks) {
      const districtCodes = codesOf(
        boundaryData.districts.filter(
          (district) => district.parentCode && selectedStateCodes.includes(district.parentCode),
        ),
      );
      const blockCodes = codesOf(
        boundaryData.blocks.filter(
          (block) => block.parentCode && districtCodes.has(block.parentCode),
        ),
      );
      facilities = facilities.filter(
        (facility) => facility.parentCode && blockCodes.has(facility.parentCode),
      );
    }

    setFacilityMenu(
      facilities
        .map((facility) => ({
          code: facility.code,
          name: translateOr(t, `BOUNDARY_${facility.code}`, facility.code),
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    );

    const validFacilityCodes = codesOf(facilities);
    setPgrFilters((prev) => {
      const pruned = pruneToValidCodes(prev.facility, validFacilityCodes);
      return pruned === prev.facility ? prev : { ...prev, facility: pruned };
    });
  }, [
    pgrfilters.state,
    pgrfilters.district,
    pgrfilters.block,
    facilityOptions,
    boundaryData,
    t,
  ]);

  useEffect(() => {
    const code = selectAssigned.code === "ASSIGNED_TO_ME" ? userUuid : "";
    setWfFilters((prev) => ({
      ...prev,
      assignee: code ? [{ code }] : [],
    }));
  }, [selectAssigned, userUuid]);

  useEffect(() => {
    const { pgrQuery, wfQuery } = buildFilterQueryFromState({ pgrfilters, wfFilters });
    onFilterChange({ pgrQuery, wfQuery, wfFilters, pgrfilters });
  }, [pgrfilters, wfFilters]); // eslint-disable-line react-hooks/exhaustive-deps

  const hasActiveFilters =
    Object.values(pgrfilters).some((value) => value.length > 0) ||
    selectAssigned.code !== assignedToOptions[1].code;

  function handleClearAllFilters() {
    setPgrFilters({ ...emptyPgrFilters, ...defaultFilters.pgrfilters });
    setWfFilters(defaultFilters.wfFilters!);
    setSelectAssigned(
      defaultFilters.wfFilters?.assignee?.[0]?.code === userUuid
        ? assignedToOptions[0]
        : assignedToOptions[1],
    );
  }

  function toggleArrayFilter(
    category: PgrFilterKey,
    option: { code: string; name?: string },
  ) {
    setPgrFilters((prev) => {
      const current = prev[category];
      const exists = current.some((item) => item.code === option.code);
      return {
        ...prev,
        [category]: exists
          ? current.filter((item) => item.code !== option.code)
          : [...current, option],
      };
    });
  }

  function isStatusGroupChecked(statuses: readonly string[]) {
    return areAllStatusesSelected(pgrfilters.applicationStatus, statuses);
  }

  function toggleStatusGroup(statuses: readonly string[]) {
    setPgrFilters((prev) => {
      const isChecked = areAllStatusesSelected(prev.applicationStatus, statuses);
      if (isChecked) {
        return {
          ...prev,
          applicationStatus: prev.applicationStatus.filter(
            (item) => !statuses.includes(item.code),
          ),
        };
      }
      const existingCodes = new Set(prev.applicationStatus.map((item) => item.code));
      return {
        ...prev,
        applicationStatus: [
          ...prev.applicationStatus,
          ...statuses.filter((code) => !existingCodes.has(code)).map((code) => ({ code })),
        ],
      };
    });
  }

  const categories = [
    { key: "assetType" as const, label: translateOr(t, "CS_ASSET_TYPE", "Asset Type"), options: assetTypeMenu },
    ...(showGeoFilters
      ? [
          { key: "state" as const, label: translateOr(t, "CS_STATE", "State"), options: stateMenu },
          { key: "district" as const, label: translateOr(t, "CS_DISTRICT", "District"), options: districtMenu },
          { key: "block" as const, label: translateOr(t, "CS_BLOCK", "Block"), options: blockMenu },
          {
            key: "facility" as const,
            label: translateOr(t, "INCIDENT_END_USER", "End User"),
            options: facilityMenu,
          },
        ]
      : []),
    {
      key: "applicationStatus" as const,
      label: translateOr(t, "ES_IM_FILTER_STATUS", "Ticket Status"),
      options: statusMenu,
    },
  ];

  const activeCategoryData = categories.find((category) => category.key === activeCategory);
  const searchLower = categorySearch.trim().toLowerCase();

  function isOptionSelected(option: { code: string; name?: string }) {
    if (activeCategory === "applicationStatus") {
      const group = ORDERED_INBOX_STATUSES.find((item) => item.code === option.code);
      return isStatusGroupChecked(group?.statuses ?? [option.code]);
    }
    return pgrfilters[activeCategory as PgrFilterKey].some((item) => item.code === option.code);
  }

  const visibleOptions = (activeCategoryData?.options ?? [])
    .filter((option) => (searchLower ? option.name.toLowerCase().includes(searchLower) : true))
    .slice()
    .sort((a, b) => {
      const aSelected = isOptionSelected(a);
      const bSelected = isOptionSelected(b);
      if (aSelected !== bSelected) return aSelected ? -1 : 1;
      return a.name.localeCompare(b.name);
    });

  let optionsContent: ReactNode;
  if (visibleOptions.length === 0) {
    optionsContent = (
      <p className="text-sm text-muted-foreground">
        {translateOr(t, "ES_COMMON_NO_OPTIONS", "No options found")}
      </p>
    );
  } else if (activeCategory === "applicationStatus") {
    optionsContent = visibleOptions.map((option) => {
      const group = ORDERED_INBOX_STATUSES.find((item) => item.code === option.code);
      const statuses = group?.statuses ?? [option.code];
      return (
        <label
          key={option.code}
          className="flex cursor-pointer items-center gap-2 text-sm font-semibold"
        >
          <Checkbox
            className="size-5 rounded-md border-2 border-primary"
            checked={isStatusGroupChecked(statuses)}
            onCheckedChange={() => toggleStatusGroup(statuses)}
          />
          {option.name}
        </label>
      );
    });
  } else {
    optionsContent = visibleOptions.map((option) => (
      <label
        key={option.code}
        className="flex cursor-pointer items-center gap-2 text-sm font-semibold"
      >
        <Checkbox
          className="size-5 rounded-md border-2 border-primary"
          checked={pgrfilters[activeCategory as PgrFilterKey].some(
            (item) => item.code === option.code,
          )}
          onCheckedChange={() => toggleArrayFilter(activeCategory as PgrFilterKey, option)}
        />
        {option.name}
      </label>
    ));
  }

  return (
    <div className="lg:rounded-lg lg:border lg:border-border lg:bg-card lg:p-5 lg:shadow-sm">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-5">
          <div className="hidden lg:block">
            <Popover
              open={desktopFiltersOpen}
              onOpenChange={(open) => {
                setDesktopFiltersOpen(open);
                if (open) {
                  setCategorySearch("");
                }
              }}
            >
              <PopoverTrigger asChild>
                <button
                  type="button"
                  className="inline-flex h-8 cursor-pointer items-center gap-2 rounded-md border border-primary px-3 text-sm font-semibold text-primary"
                >
                  <Filter className="size-4" />
                  {translateOr(t, "ES_IM_FILTERS", "Filters")}
                  <Separator orientation="vertical" className="h-4" />
                  <ChevronDown
                    className={cn(
                      "size-4 transition-transform",
                      desktopFiltersOpen && "rotate-180",
                    )}
                  />
                </button>
              </PopoverTrigger>
              <PopoverContent align="start" className="w-auto p-0">
                <div className="flex">
                  <div className="w-40 shrink-0 border-r border-border py-2">
                    {categories.map((category) => (
                      <button
                        key={category.key}
                        type="button"
                        onClick={() => {
                          setActiveCategory(category.key);
                          setCategorySearch("");
                        }}
                        className={cn(
                          "block w-full border-l-2 px-4 py-2 text-left text-sm transition-colors",
                          activeCategory === category.key
                            ? "border-primary font-semibold text-primary"
                            : "border-transparent text-muted-foreground hover:text-foreground",
                        )}
                      >
                        {category.label}
                      </button>
                    ))}
                  </div>

                  <div className="w-64 shrink-0 space-y-3 p-3">
                    <Input
                      value={categorySearch}
                      onChange={(event) => setCategorySearch(event.target.value)}
                      placeholder={translateOr(t, "ES_COMMON_SEARCH", "Search")}
                    />
                    <ScrollArea className="h-56 pr-3">
                      <div className="space-y-3">{optionsContent}</div>
                    </ScrollArea>
                  </div>
                </div>
              </PopoverContent>
            </Popover>
          </div>

          <div className="lg:hidden">
            <Sheet
              open={mobileFiltersOpen}
              onOpenChange={(open) => {
                setMobileFiltersOpen(open);
                if (open) {
                  setCategorySearch("");
                }
              }}
            >
              <SheetTrigger asChild>
                <button
                  type="button"
                  className="inline-flex h-8 cursor-pointer items-center gap-2 rounded-md border border-primary px-3 text-sm font-semibold text-primary"
                >
                  <Filter className="size-4" />
                  <Separator orientation="vertical" className="h-4" />
                  <ChevronDown
                    className={cn(
                      "size-4 transition-transform",
                      mobileFiltersOpen && "rotate-180",
                    )}
                  />
                </button>
              </SheetTrigger>
              <SheetContent
                side="bottom"
                showCloseButton={false}
                className="max-h-[85dvh] rounded-t-2xl p-0"
              >
                <div className="mx-auto mt-3 h-1.5 w-10 shrink-0 rounded-full bg-border" />
                <SheetHeader className="shrink-0 pb-0">
                  <SheetTitle className="text-lg">
                    {translateOr(t, "ES_IM_FILTERS", "Filters")}
                  </SheetTitle>
                </SheetHeader>
                <div className="flex shrink-0 gap-6 overflow-x-auto border-b border-border px-4">
                  {categories.map((category) => (
                    <button
                      key={category.key}
                      type="button"
                      onClick={() => {
                        setActiveCategory(category.key);
                        setCategorySearch("");
                      }}
                      className={cn(
                        "shrink-0 border-b-2 px-1 py-2 text-sm font-medium whitespace-nowrap transition-colors",
                        activeCategory === category.key
                          ? "border-primary text-primary"
                          : "border-transparent text-muted-foreground",
                      )}
                    >
                      {category.label}
                    </button>
                  ))}
                </div>
                <div className="flex-1 space-y-3 overflow-y-auto p-4">
                  <Input
                    value={categorySearch}
                    onChange={(event) => setCategorySearch(event.target.value)}
                    placeholder={translateOr(t, "ES_COMMON_SEARCH", "Search")}
                  />
                  <div className="space-y-3">{optionsContent}</div>
                </div>
                <SheetFooter className="flex-row gap-3 border-t border-border">
                  <Button
                    type="button"
                    variant="outline"
                    className="flex-1"
                    disabled={!hasActiveFilters}
                    onClick={() => {
                      handleClearAllFilters();
                      setMobileFiltersOpen(false);
                    }}
                  >
                    {translateOr(t, "ES_IM_CLEAR_ALL_FILTERS", "clear all filters")}
                  </Button>
                  <Button
                    type="button"
                    className="flex-1"
                    onClick={() => setMobileFiltersOpen(false)}
                  >
                    {translateOr(t, "ES_IM_APPLY_FILTERS", "Apply filters")}
                  </Button>
                </SheetFooter>
              </SheetContent>
            </Sheet>
          </div>

          {assignedToOptions.map((option) => (
            <label key={option.code} className="flex cursor-pointer items-center gap-2 text-sm">
              <input
                type="radio"
                name="assignedTo"
                className="livelihood-radio"
                checked={selectAssigned.code === option.code}
                onChange={() => setSelectAssigned(option)}
              />
              <span>{option.name}</span>
            </label>
          ))}
        </div>

        <button
          type="button"
          disabled={!hasActiveFilters}
          onClick={handleClearAllFilters}
          className={cn(
            "hidden text-sm transition-colors lg:block",
            hasActiveFilters
              ? "cursor-pointer text-foreground hover:text-primary"
              : "text-muted-foreground/50",
          )}
        >
          {translateOr(t, "ES_IM_CLEAR_ALL_FILTERS", "clear all filters")}
        </button>
      </div>
    </div>
  );
}
