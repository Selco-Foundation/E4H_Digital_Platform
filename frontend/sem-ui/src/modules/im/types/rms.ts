export interface PausedFacility {
  facilityId?: string;
  facilityName?: string;
  boundaryCode?: string;
  /** ISO-8601 instant (backend `Instant` field, Spring Boot's default Jackson serialization). */
  pausedUntil?: string;
  daysLeft?: number;
  reason?: string;
  pausedBy?: string;
  updatedAt?: string;
}

export interface PausedFacilitySearchResponse {
  success?: boolean;
  totalCount?: number;
  pausedFacilities?: PausedFacility[];
  error?: unknown;
}

export interface PausedFacilityFilters {
  state?: string[];
  district?: string[];
  block?: string[];
  boundaryCodes?: string[];
}

export interface FacilityPauseStatus {
  success?: boolean;
  facilityId?: string;
  isPaused?: boolean;
  pausedUntil?: string;
  daysLeft?: number;
  reason?: string;
  message?: string;
  error?: unknown;
}

export interface ManageFacilityPauseInput {
  action: "PAUSE" | "RESUME";
  tenantId: string;
  facilityId: string;
  facilityName?: string;
  boundaryCode?: string;
  pausedUntil?: string;
  reason?: string;
}
