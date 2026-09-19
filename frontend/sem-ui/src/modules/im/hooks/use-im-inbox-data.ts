import { tenantId, useAuthStore, useJurisdictionStore } from "@/shared";
import { useQuery } from "@tanstack/react-query";
import {
  LIVELIHOOD_INCIDENT_BUSINESS_SERVICE,
  RESOLVED_APPLICATION_STATUSES,
} from "../constants/workflow";
import { searchInbox } from "../services/inbox";
import type { ImInboxSearchParams } from "../types/inbox";
import { hasImAccess } from "../utils/access";
import { flattenInboxFilters } from "../utils/inbox-filters";
import {
  combineInboxResponses,
  normalizeInboxResponse,
  sumStatusCounts,
} from "../utils/inbox-transform";
import { buildSummaryRoleFilters } from "./inbox-defaults";
import { fetchAssetTypes } from "../services/mdms";

export function useImInboxSummary() {
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const employeeTenantId = useAuthStore((state) => state.employeeTenantId);
  const boundaries = useJurisdictionStore((state) => state.boundaries);

  const filters = {
    limit: 10,
    offset: 0,
    services: [LIVELIHOOD_INCIDENT_BUSINESS_SERVICE],
    sortOrder: "DESC",
    ...buildSummaryRoleFilters(user),
  };

  const enabled = Boolean(accessToken && employeeTenantId && hasImAccess(user?.roles));
  const jurisdiction = boundaries ?? { country: ["-"] };

  return useQuery({
    queryKey: ["im-inbox-summary", employeeTenantId, filters, jurisdiction],
    enabled,
    staleTime: 30_000,
    queryFn: async () => {
      const data = await searchInbox(
        employeeTenantId!,
        jurisdiction,
        filters,
        accessToken!,
        user,
      );
      const normalized = normalizeInboxResponse(data);
      const resolvedCount = sumStatusCounts(
        normalized.statusArray,
        RESOLVED_APPLICATION_STATUSES,
      );
      return {
        totalCount: normalized.total,
        nearingSlaCount: normalized.nearingSlaCount,
        resolvedCount,
        statusMap: normalized.statusArray,
      };
    },
  });
}

export function useImInboxData(searchParams: ImInboxSearchParams) {
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const employeeTenantId = useAuthStore((state) => state.employeeTenantId);
  const boundaries = useJurisdictionStore((state) => state.boundaries);

  const filters = flattenInboxFilters(searchParams, {
    limit: searchParams.limit ?? 10,
    offset: searchParams.offset ?? 0,
    services: [LIVELIHOOD_INCIDENT_BUSINESS_SERVICE],
    sortOrder: "DESC",
  });

  const enabled = Boolean(accessToken && employeeTenantId && hasImAccess(user?.roles));
  const jurisdiction = boundaries ?? { country: ["-"] };

  return useQuery({
    queryKey: [
      "im-inbox",
      employeeTenantId,
      searchParams,
      filters,
      jurisdiction,
    ],
    enabled,
    staleTime: 30_000,
    queryFn: async () => {
      const data = await searchInbox(
        employeeTenantId!,
        jurisdiction,
        filters,
        accessToken!,
        user,
      );
      const normalized = normalizeInboxResponse(data);
      const combinedRes = combineInboxResponses(normalized.items, user);
      return {
        combinedRes,
        total: normalized.total,
        statusArray: normalized.statusArray,
      };
    },
  });
}

export function useImAssetTypes() {
  const accessToken = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const stateTenantId = tenantId();

  return useQuery({
    queryKey: ["im-asset-types", stateTenantId],
    enabled: Boolean(accessToken),
    staleTime: Number.POSITIVE_INFINITY,
    queryFn: async () => {
      return fetchAssetTypes(accessToken!, user);
    },
  });
}
