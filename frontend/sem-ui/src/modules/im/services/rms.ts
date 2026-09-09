import { apiClient, type AuthUser } from "@/shared";
import { createRequestInfo } from "@/shared/api/request-info";
import type {
  FacilityPauseStatus,
  ManageFacilityPauseInput,
  PausedFacilityFilters,
  PausedFacilitySearchResponse,
} from "../types/rms";

/**
 * DIGIT-UI's RMS Paused Facilities screen — endpoints confirmed against the
 * real backend `RMSController.java` (`backend/e4h-services/rms-service`):
 * `/ticket/paused_facility` (list), `/ticket/pause/_search` (status check),
 * `/ticket/pause` (submit pause/resume).
 */
export async function searchPausedFacilities(
  filters: PausedFacilityFilters,
  tenantId: string,
  limit: number,
  offset: number,
  accessToken: string,
  user?: AuthUser | null,
): Promise<PausedFacilitySearchResponse> {
  const { data } = await apiClient.post<PausedFacilitySearchResponse>(
    "/rms-service/v1/ticket/paused_facility",
    {
      RequestInfo: createRequestInfo(accessToken, user),
      Facility: {
        tenantId: [tenantId],
        ...filters,
        limit,
        offset,
      },
    },
  );
  return data;
}

export async function fetchFacilityPauseStatus(
  facilityId: string,
  tenantId: string,
  accessToken: string,
  user?: AuthUser | null,
): Promise<FacilityPauseStatus> {
  const { data } = await apiClient.post<FacilityPauseStatus>(
    "/rms-service/v1/ticket/pause/_search",
    {
      RequestInfo: createRequestInfo(accessToken, user),
      FacilitySearch: { facilityId, tenantId },
    },
  );
  return data;
}

export async function manageFacilityPause(
  input: ManageFacilityPauseInput,
  accessToken: string,
  user?: AuthUser | null,
): Promise<FacilityPauseStatus> {
  const { data } = await apiClient.post<FacilityPauseStatus>(
    "/rms-service/v1/ticket/pause",
    {
      RequestInfo: createRequestInfo(accessToken, user),
      PauseFacility: input,
    },
  );
  return data;
}
