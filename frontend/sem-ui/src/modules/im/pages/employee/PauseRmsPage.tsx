import {
  aggregateBoundaryCodes,
  employeeHomePath,
  contextPath,
  tenantId,
  translateOr,
  useAuthStore,
  useBoundary,
  useFacility,
  useJurisdictionStore,
  useTranslate,
} from "@/shared";
import { Button, TopBar } from "@/ui";
import { useMutation } from "@tanstack/react-query";
import { useSearch } from "@tanstack/react-router";
import { useEffect, useMemo, useState } from "react";
import { FormSelectField } from "../../components/create/FormSelectField";
import { IM_ROUTES } from "../../constants/routes";
import { fetchFacilityPauseStatus, manageFacilityPause } from "../../services/rms";

const MAX_REASON_LENGTH = 256;

function todayIsoDate(): string {
  return new Date().toISOString().slice(0, 10);
}

interface BoundaryOption {
  code: string;
  name: string;
  parentCode?: string;
}

interface FacilityOption {
  code: string;
  name: string;
  facilityId: string;
  facilityName?: string;
  parentCode?: string;
}

/**
 * Matches DIGIT-UI's PauseRMS.js: District → Block → Health Care Centre
 * cascading dropdowns (boundary-service + facility-service, same hooks as
 * the inbox filter), a pause-duration date, and a reason field. Selecting a
 * facility checks its current pause status via `/ticket/pause/_search` to
 * prefill the form and offer Resume instead of Pause.
 */
export function PauseRmsPage() {
  const { t } = useTranslate();
  const basePath = `/${contextPath()}`;
  const homePath = employeeHomePath();
  const imRootPath = `${basePath}${IM_ROUTES.imRoot}`;
  const pausedListPath = `${basePath}${IM_ROUTES.pausedRmsFacilities}`;

  const search = useSearch({ strict: false }) as { facilityId?: string };
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const stateTenantId = tenantId();
  const boundaries = useJurisdictionStore((state) => state.boundaries);
  const jurisdictionCodes = aggregateBoundaryCodes(boundaries);

  const [district, setDistrict] = useState<BoundaryOption | null>(null);
  const [block, setBlock] = useState<BoundaryOption | null>(null);
  const [facility, setFacility] = useState<FacilityOption | null>(null);
  const [pausedUntil, setPausedUntil] = useState(todayIsoDate());
  const [reason, setReason] = useState("");
  const [isPaused, setIsPaused] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const [districtMenu, setDistrictMenu] = useState<BoundaryOption[]>([]);
  const [blockMenu, setBlockMenu] = useState<BoundaryOption[]>([]);
  const [facilityMenu, setFacilityMenu] = useState<FacilityOption[]>([]);
  const [facilityBoundaryCodes, setFacilityBoundaryCodes] = useState<string[]>(["-"]);
  const [facilityBoundaryParents, setFacilityBoundaryParents] = useState<Map<string, string | undefined>>(
    new Map(),
  );

  const { data: boundaryData } = useBoundary(jurisdictionCodes);
  const { data: facilityData } = useFacility(facilityBoundaryCodes);

  useEffect(() => {
    if (boundaryData?.facilities) {
      setFacilityBoundaryCodes(
        boundaryData.facilities.map((entry) => entry.code).filter(Boolean),
      );
      setFacilityBoundaryParents(
        new Map(boundaryData.facilities.map((entry) => [entry.code, entry.parentCode])),
      );
    }
  }, [boundaryData]);

  useEffect(() => {
    if (!boundaryData?.districts) {
      return;
    }
    setDistrictMenu(
      boundaryData.districts
        .map((entry) => ({
          code: entry.code,
          name: translateOr(t, `Boundary_${entry.code}`, entry.code),
          parentCode: entry.parentCode,
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    );
  }, [boundaryData, t]);

  useEffect(() => {
    if (!boundaryData?.blocks || !district) {
      setBlockMenu([]);
      return;
    }
    setBlockMenu(
      boundaryData.blocks
        .filter((entry) => entry.parentCode === district.code)
        .map((entry) => ({
          code: entry.code,
          name: translateOr(t, `Boundary_${entry.code}`, entry.code),
          parentCode: entry.parentCode,
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    );
  }, [boundaryData, district, t]);

  useEffect(() => {
    if (!facilityData?.facilities || !block) {
      setFacilityMenu([]);
      return;
    }
    setFacilityMenu(
      facilityData.facilities
        .filter((entry) => facilityBoundaryParents.get(entry.boundaryCode) === block.code)
        .map((entry) => ({
          code: entry.boundaryCode,
          name: entry.facilityName ?? translateOr(t, `Boundary_${entry.boundaryCode}`, entry.boundaryCode),
          facilityId: entry.facilityId,
          facilityName: entry.facilityName,
          parentCode: facilityBoundaryParents.get(entry.boundaryCode),
        }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    );
  }, [facilityData, block, facilityBoundaryParents, t]);

  useEffect(() => {
    if (!facility?.facilityId || !accessToken) {
      return;
    }
    void fetchFacilityPauseStatus(facility.facilityId, stateTenantId, accessToken, user).then(
      (status) => {
        setIsPaused(Boolean(status.isPaused));
        setPausedUntil(status.pausedUntil ? status.pausedUntil.slice(0, 10) : todayIsoDate());
        setReason(status.isPaused ? (status.reason ?? "") : "");
      },
    );
  }, [facility?.facilityId, accessToken, stateTenantId, user]);

  // Deep link from the paused-facilities table's row link (`?facilityId=...`):
  // resolve pause status directly by id, without waiting on the
  // District/Block/Facility cascade to be manually re-selected.
  useEffect(() => {
    if (!search.facilityId || !accessToken || facility) {
      return;
    }
    setFacility({ code: "", name: search.facilityId, facilityId: search.facilityId });
  }, [search.facilityId, accessToken, facility]);

  const mutation = useMutation({
    mutationFn: async (action: "PAUSE" | "RESUME") => {
      if (!facility || !accessToken) {
        throw new Error("FACILITY_REQUIRED");
      }
      if (action === "PAUSE" && !reason.trim()) {
        throw new Error("REASON_REQUIRED");
      }
      return manageFacilityPause(
        {
          action,
          tenantId: stateTenantId,
          facilityId: facility.facilityId,
          facilityName: facility.facilityName,
          boundaryCode: facility.code,
          pausedUntil: action === "PAUSE" ? `${pausedUntil}T23:59:59Z` : undefined,
          reason: action === "PAUSE" ? reason.trim() : undefined,
        },
        accessToken,
        user,
      );
    },
    onSuccess: (response) => {
      if (!response?.success) {
        setError(
          response?.message ?? translateOr(t, "CS_COMMON_SOMETHING_WENT_WRONG", "Something went wrong!"),
        );
        return;
      }
      setError(null);
      setSuccessMessage(
        response.isPaused
          ? translateOr(t, "RMS_FACILITY_PAUSED", "Facility paused successfully")
          : translateOr(t, "RMS_FACILITY_RESUMED", "Facility resumed successfully"),
      );
      setIsPaused(Boolean(response.isPaused));
    },
    onError: (mutationError: Error) => {
      setError(
        mutationError.message === "FACILITY_REQUIRED"
          ? translateOr(t, "RMS_SELECT_FACILITY", "Please select a facility")
          : mutationError.message === "REASON_REQUIRED"
            ? translateOr(t, "RMS_REASON_REQUIRED", "Please enter a reason")
            : translateOr(t, "CS_COMMON_SOMETHING_WENT_WRONG", "Something went wrong!"),
      );
    },
  });

  const facilityOptions = useMemo(
    () => facilityMenu.map((entry) => ({ code: entry.code, name: entry.name })),
    [facilityMenu],
  );

  return (
    <div className="space-y-6">
      <TopBar
        title={translateOr(t, "ES_IM_PAUSE_RMS", "Pause RMS")}
        breadcrumbs={[
          { label: translateOr(t, "CORE_COMMON_OVERVIEW", "Overview"), to: homePath },
          { label: translateOr(t, "ES_IM_HEADER_INCIDENTS", "Tickets"), to: imRootPath },
          { label: translateOr(t, "RMS_PAUSED_FACILITIES", "RMS Paused Facilities"), to: pausedListPath },
          { label: translateOr(t, "ES_IM_PAUSE_RMS", "Pause RMS") },
        ]}
      />

      <div className="livelihood-card max-w-xl space-y-4 p-6">
        <FormSelectField
          label={translateOr(t, "CS_DISTRICT", "District")}
          required
          value={district?.code ?? ""}
          options={districtMenu}
          onChange={(option) => {
            setDistrict(districtMenu.find((entry) => entry.code === option?.code) ?? null);
            setBlock(null);
            setFacility(null);
          }}
        />
        <FormSelectField
          label={translateOr(t, "CS_BLOCK", "Block")}
          required
          value={block?.code ?? ""}
          options={blockMenu}
          disabled={!district}
          onChange={(option) => {
            setBlock(blockMenu.find((entry) => entry.code === option?.code) ?? null);
            setFacility(null);
          }}
        />
        <FormSelectField
          label={translateOr(t, "CS_HEALTH_CARE", "Health Care Centre")}
          required
          value={facility?.code ?? ""}
          options={facilityOptions}
          disabled={!block}
          onChange={(option) => {
            setFacility(facilityMenu.find((entry) => entry.code === option?.code) ?? null);
            setSuccessMessage(null);
            setError(null);
          }}
        />

        {!isPaused ? (
          <div className="space-y-1.5">
            <label className="text-sm font-medium text-ink-950">
              {translateOr(t, "RMS_PAUSE_DURATION", "RMS Pause Duration")}
              <span className="text-destructive"> *</span>
            </label>
            <input
              type="date"
              className="w-full rounded border border-ink-300 bg-card px-3 py-2 text-sm"
              min={todayIsoDate()}
              value={pausedUntil}
              onChange={(event) => setPausedUntil(event.target.value)}
            />
          </div>
        ) : null}

        {!isPaused ? (
          <div className="space-y-1.5">
            <label className="text-sm font-medium text-ink-950">
              {translateOr(t, "RMS_PAUSE_REASON", "RMS Pause Reason")}
              <span className="text-destructive"> *</span>
            </label>
            <textarea
              className="min-h-[90px] w-full rounded border border-ink-300 bg-card px-3 py-2 text-sm"
              maxLength={MAX_REASON_LENGTH}
              value={reason}
              onChange={(event) => setReason(event.target.value)}
            />
            <p className="text-right text-xs text-muted-foreground">
              {reason.length}/{MAX_REASON_LENGTH}
            </p>
          </div>
        ) : (
          <p className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
            {translateOr(t, "RMS_ALREADY_PAUSED", "This facility is already paused")}
            {reason ? `: ${reason}` : ""}
          </p>
        )}

        {error ? <p className="text-sm text-destructive">{error}</p> : null}
        {successMessage ? <p className="text-sm text-success-foreground">{successMessage}</p> : null}

        <Button
          type="button"
          size="lg"
          disabled={!facility || mutation.isPending}
          onClick={() => mutation.mutate(isPaused ? "RESUME" : "PAUSE")}
        >
          {isPaused
            ? translateOr(t, "RMS_RESUME", "Resume")
            : translateOr(t, "ES_IM_PAUSE_RMS", "Pause RMS")}
        </Button>
      </div>
    </div>
  );
}
