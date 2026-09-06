import type { JurisdictionBoundaries } from "@/shared";
import type { FacilityBulkSearchCriteria } from "../types/facility-asset";

const HIERARCHY_KEYS = ["state", "district", "block"] as const;

export function buildFacilitySearchCriteria(
  jurisdiction: JurisdictionBoundaries | null | undefined,
  tenantId: string,
): FacilityBulkSearchCriteria {
  const criteria: FacilityBulkSearchCriteria = {
    limit: 100,
    offset: 0,
    isOnmReady: false,
    tenantId: [tenantId],
  };

  if (!jurisdiction) {
    return criteria;
  }

  for (const key of HIERARCHY_KEYS) {
    const codes = jurisdiction[key];
    if (codes?.length) {
      criteria[key] = codes;
    }
  }

  if (Object.hasOwn(jurisdiction, "facility")) {
    criteria.boundaryCodes = jurisdiction.facility ?? [];
  }

  return criteria;
}

export function deriveDistrictBlockCodes(
  boundaryCode: string,
  jurisdiction: JurisdictionBoundaries | null | undefined,
): { districtCode: string; blockCode: string } {
  const districtCodes = jurisdiction?.district ?? [];
  const blockCodes = jurisdiction?.block ?? [];

  const blockCode =
    blockCodes.find((code) => boundaryCode.startsWith(code)) ??
    blockCodes[0] ??
    boundaryCode;
  const districtCode =
    districtCodes.find((code) => boundaryCode.startsWith(code)) ??
    districtCodes[0] ??
    blockCode;

  return { districtCode, blockCode };
}
