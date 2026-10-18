import { apiClient, type AuthUser, type JurisdictionBoundaries } from "@/shared";
import { createRequestInfo } from "@/shared/api/request-info";
import { buildIncidentInboxFilters, type IncidentFilterInput } from "../utils/inbox-filters";

/**
 * Matches DIGIT-UI's useMappedVendors.js: the vendor dropdown is scoped to
 * these three business services regardless of which service the current
 * inbox view is otherwise filtered to.
 */
const MAPPED_VENDOR_BUSINESS_SERVICES = ["Incident_Low", "Incident_Medium", "Incident_High"];

export interface MappedVendorOption {
  code: string;
  name: string;
}

interface MappedVendorSearchResponse {
  mappedVendors?: string[];
  totalCount?: number;
}

export async function fetchMappedVendors(
  filters: IncidentFilterInput,
  tenantId: string,
  jurisdictionBoundaries: JurisdictionBoundaries | null,
  accessToken: string,
  user?: AuthUser | null,
): Promise<{ vendors: MappedVendorOption[]; total: number }> {
  const { workflowFilters, searchFilters } = buildIncidentInboxFilters(
    { ...filters, services: MAPPED_VENDOR_BUSINESS_SERVICES },
    tenantId,
  );

  const { data } = await apiClient.post<MappedVendorSearchResponse>(
    "/inbox/v2/mappedVendor/_search",
    {
      RequestInfo: createRequestInfo(accessToken, user),
      inbox: {
        tenantId,
        processSearchCriteria: workflowFilters,
        jurisdictionSearchCriteria: jurisdictionBoundaries ?? { country: ["-"] },
        moduleSearchCriteria: searchFilters,
      },
    },
  );

  return {
    vendors: (data.mappedVendors ?? []).map((name) => ({ code: name, name })),
    total: data.totalCount ?? 0,
  };
}
