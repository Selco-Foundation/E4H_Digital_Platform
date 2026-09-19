export interface LivelihoodFacilityAddress {
  district?: string | null;
  block?: string | null;
  city?: string | null;
}

export interface LivelihoodFacility {
  tenantId: string;
  facilityId: string;
  facilityName?: string;
  facilityPocName: string;
  facilityPocUsername?: string;
  facilityPocPhone?: string;
  facilityPocEmail?: string;
  endUserUuid?: string;
  boundaryCode: string;
  facilityStatus?: string | null;
  address?: LivelihoodFacilityAddress;
  isOnmReady?: boolean;
}

export interface LivelihoodAsset {
  assetId: string;
  tenantId: string;
  facilityId: string;
  boundaryCode: string;
  assetTypeId: string;
  name: string;
  serialNumber?: string;
  modelNumber?: string;
  isOperational?: boolean;
  documentFileStoreId?: string;
}

export interface FacilityBulkSearchCriteria {
  limit?: number;
  offset?: number;
  isOnmReady?: boolean;
  tenantId: string[];
  state?: string[];
  district?: string[];
  block?: string[];
  boundaryCodes?: string[];
}
