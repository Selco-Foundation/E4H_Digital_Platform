import { apiClient, type AuthUser } from "@/shared";
import { createRequestInfo } from "@/shared/api/request-info";
import type {
  FacilityBulkSearchCriteria,
  LivelihoodFacility,
} from "../types/facility-asset";

interface FacilityBulkSearchResponse {
  facilities?: Array<{
    tenant_id?: string;
    facility_id?: string;
    facility_name?: string;
    facility_poc_name?: string;
    facility_poc_username?: string;
    facility_poc_phone?: string;
    facility_poc_email?: string;
    end_user_uuid?: string;
    boundaryCode?: string;
    facility_status?: string | null;
    isOnmReady?: boolean;
    address?: {
      district?: string | null;
      block?: string | null;
      city?: string | null;
    };
  }>;
  totalCount?: number;
}

function mapFacility(raw: NonNullable<FacilityBulkSearchResponse["facilities"]>[number]): LivelihoodFacility {
  return {
    tenantId: raw.tenant_id ?? "",
    facilityId: raw.facility_id ?? "",
    facilityName: raw.facility_name,
    facilityPocName: raw.facility_poc_name ?? raw.facility_name ?? raw.facility_id ?? "",
    facilityPocUsername: raw.facility_poc_username,
    facilityPocPhone: raw.facility_poc_phone,
    facilityPocEmail: raw.facility_poc_email,
    endUserUuid: raw.end_user_uuid,
    boundaryCode: raw.boundaryCode ?? "",
    facilityStatus: raw.facility_status,
    address: raw.address,
    isOnmReady: raw.isOnmReady,
  };
}

export async function searchFacilitiesByJurisdiction(
  criteria: FacilityBulkSearchCriteria,
  accessToken: string,
  user?: AuthUser | null,
): Promise<{ facilities: LivelihoodFacility[]; total: number }> {
  const { data } = await apiClient.post<FacilityBulkSearchResponse>(
    "/facility-service/v2/facility/_bulk-search",
    {
      RequestInfo: createRequestInfo(accessToken, user),
      Facility: criteria,
    },
  );

  const facilities = (data.facilities ?? []).map(mapFacility);
  return {
    facilities,
    total: data.totalCount ?? facilities.length,
  };
}
