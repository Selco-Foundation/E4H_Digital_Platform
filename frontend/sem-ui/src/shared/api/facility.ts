import { createRequestInfo } from "./request-info";
import { apiClient } from "./client";
import type { AuthUser } from "../stores/auth-store";

export interface FacilitySummary {
  boundaryCode: string;
  facilityId: string;
  facilityStatus?: string;
  facilityName?: string;
}

interface FacilitySearchResponse {
  facilities?: Array<{
    boundaryCode?: string;
    facility_id?: string;
    facility_status?: string;
    facility_name?: string;
  }>;
  totalCount?: number;
}

export async function fetchFacilities(
  boundaryCodes: string[],
  employeeTenantId: string,
  accessToken: string,
  user?: AuthUser | null,
): Promise<{ facilities: FacilitySummary[]; total: number }> {
  const response = await apiClient.post<FacilitySearchResponse>(
    "/facility-service/v2/facility/_bulk-search",
    {
      RequestInfo: createRequestInfo(accessToken, user),
      Facility: {
        tenantId: [employeeTenantId],
        boundaryCodes,
        isOnmReady: true,
        sendNonPaginatedResponse: true,
      },
    },
  );

  const facilities =
    response.data.facilities?.map((facility) => ({
      boundaryCode: facility.boundaryCode ?? "",
      facilityId: facility.facility_id ?? "",
      facilityStatus: facility.facility_status,
      facilityName: facility.facility_name,
    })) ?? [];

  return {
    facilities,
    total: response.data.totalCount ?? facilities.length,
  };
}
