import { useQuery, useQueryClient } from "react-query";
import { OrganizationService } from "../services/Organization";

const safeArray = (v) => (Array.isArray(v) ? v : []);
const pickFirst = (v) => (Array.isArray(v) && v.length ? v[0] : null);

const extractBoundaryParts = (boundaryCode) => {
  if (!boundaryCode || typeof boundaryCode !== "string") return { state: "", district: "", block: "" };
  const parts = boundaryCode.split("_").filter(Boolean);
  const state = parts.length > 1 ? parts[1] : "";
  const district = parts.length > 2 ? parts[2] : "";
  const block = parts.length > 3 ? parts[3] : "";
  return { state, district, block };
};

const mapOrganizations = (raw) => {
  return safeArray(raw).map((org) => {
    const address = pickFirst(org && org.orgAddress) || null;
    const boundaryCode = (address && address.boundaryCode) || "";
    const parts = extractBoundaryParts(boundaryCode);

    return {
      id: org && org.id,
      tenantId: org && org.tenantId,
      name: org && org.name,
      code: org && org.code,
      orgType: org && org.orgType,
      orgSubType: org && org.orgSubType,
      orgStatus: org && org.orgStatus,
      pocName: org && org.orgOrgPocName, // keep your old mapping if needed
      pocName: org && org.orgPocName,
      pocPhone: org && org.orgPocPhone,
      pocEmail: org && org.orgPocEmail,
      boundaryCode,
      state: parts.state,
      district: parts.district,
      block: parts.block,
      hqAddress: (address && address.hqAddress) || "",
      isActive: org && org.isActive,
    };
  });
};

const getTotal = (res) => {
  if (!res) return 0;
  if (typeof res.TotalCount === "number") return res.TotalCount;
  if (typeof res.totalCount === "number") return res.totalCount;

  const pagination = res.pagination;
  if (pagination && typeof pagination.totalCount === "number") return pagination.totalCount;

  if (res.TotalCount) return Number(res.TotalCount) || 0;
  if (res.totalCount) return Number(res.totalCount) || 0;
  if (pagination && pagination.totalCount) return Number(pagination.totalCount) || 0;

  return 0;
};

/**
 * fixedOrgType: "PLATFORM" | "VENDOR" (required for your split pages)
 */
const useOrganization = (filter, limit, offset, fixedOrgType) => {
  const tenantId = Digit.ULBService.getCurrentTenantId();

  const f = filter || {};
  const organizationFilterQuery = f.organizationFilterQuery || {};
  const organizationSearchQuery = f.organizationSearchQuery || {};

  const searchCriteria = {};

  if (organizationSearchQuery.name) searchCriteria.name = organizationSearchQuery.name;

  // ✅ FORCE ORG TYPE PER PAGE (ignore any orgType filters from UI)
  if (fixedOrgType) searchCriteria.orgType = fixedOrgType;

  if (organizationFilterQuery.orgSubType && organizationFilterQuery.orgSubType.length) {
    searchCriteria.orgSubType = organizationFilterQuery.orgSubType;
  }
  if (organizationFilterQuery.orgStatus && organizationFilterQuery.orgStatus.length) {
    searchCriteria.orgStatus = organizationFilterQuery.orgStatus;
  }

  if (organizationFilterQuery.state && organizationFilterQuery.state.length) {
    searchCriteria.state = organizationFilterQuery.state;
  }
  if (organizationFilterQuery.district && organizationFilterQuery.district.length) {
    searchCriteria.district = organizationFilterQuery.district;
  }
  if (organizationFilterQuery.block && organizationFilterQuery.block.length) {
    searchCriteria.block = organizationFilterQuery.block;
  }

  const queryClient = useQueryClient();

  // ✅ include fixedOrgType so PLATFORM/VENDOR cache never clashes :contentReference[oaicite:0]{index=0}
  const queryKey = ["ORGANIZATIONS", tenantId, fixedOrgType, limit, offset, searchCriteria];

  const queryFn = async () => {
    const res = await OrganizationService.searchOrganizations({
      tenantId,
      offset,
      limit,
      searchCriteria,
    });

    const list = (res && (res.organisations || res.organizations)) || [];

    return {
      organizations: mapOrganizations(list),
      total: getTotal(res),
      raw: res,
    };
  };

  const q = useQuery(queryKey, queryFn);

  return {
    isLoading: q.isLoading,
    isError: q.isError,
    error: q.error,
    data: q.data,
    revalidate: () => queryClient.invalidateQueries(["ORGANIZATIONS"]),
  };
};

export default useOrganization;