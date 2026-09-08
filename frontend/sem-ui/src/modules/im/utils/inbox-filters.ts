import { LIVELIHOOD_INCIDENT_BUSINESS_SERVICE } from "../constants/workflow";

const DAY = 24 * 60 * 60 * 1000;

export interface IncidentFilterInput {
  applicationNumber?: string;
  mobileNumber?: string;
  limit?: number;
  offset?: number;
  sortBy?: string;
  sortOrder?: string;
  total?: number;
  applicationStatus?: string;
  services?: string[];
  assetType?: string;
  incidentType?: string;
  incidentSubType?: string;
  facility?: string;
  assignee?: string;
  nearingSLA?: boolean;
  state?: string;
  district?: string;
  block?: string;
  isSystemFunctional?: string;
  wfStatus?: string;
  IncidentWrappers?: boolean;
  incidentId?: string;
  tenantId?: string;
}

export interface IncidentFilterResult {
  searchFilters: Record<string, unknown>;
  workflowFilters: Record<string, unknown>;
  limit?: number;
  offset?: number;
  sortBy?: string;
  sortOrder?: string;
  applicationNumber?: string;
}

function splitCsv(value: string): string[] {
  return value.includes(",") ? value.split(",") : [value];
}

export function buildIncidentInboxFilters(
  filtersArg: IncidentFilterInput,
  tenantId: string,
): IncidentFilterResult {
  const searchFilters: Record<string, unknown> = {};
  const workflowFilters: Record<string, unknown> = {};

  const {
    applicationNumber,
    mobileNumber,
    limit,
    offset,
    sortBy,
    sortOrder,
    applicationStatus,
    services,
    assetType,
    incidentType,
    incidentSubType,
    facility,
    assignee,
    nearingSLA,
    district,
    block,
    isSystemFunctional,
    wfStatus,
  } = filtersArg ?? {};

  if (filtersArg?.IncidentWrappers) {
    searchFilters.applicationNumber = filtersArg.incidentId;
  }

  if (wfStatus) {
    let convertStatus = splitCsv(wfStatus);
    if (applicationStatus) {
      const applicationStatuses = splitCsv(applicationStatus);
      const intersectionStatuses = convertStatus.filter((status) =>
        applicationStatuses.includes(status),
      );
      convertStatus = intersectionStatuses.length ? intersectionStatuses : [""];
    }
    workflowFilters.status = convertStatus;
  } else if (applicationStatus) {
    workflowFilters.status = splitCsv(applicationStatus);
  }

  // Backend `RequestSearchCriteria` has no `assetType`/`facility`/`state` fields.
  // The inbox "Asset Type" filter is the facility's own type, stored on the
  // incident as `phcSubType`; the facility filter is the single `boundaryCode`
  // field. `district`/`block` are plain strings there too (no `state` field
  // exists at all — matches DIGIT-UI's own behavior of sending it and having
  // the backend ignore it).
  if (assetType) {
    searchFilters.phcSubType = splitCsv(assetType);
  }

  if (incidentType) {
    searchFilters.incidentType = splitCsv(incidentType);
  }

  if (incidentSubType) {
    searchFilters.incidentSubType = splitCsv(incidentSubType);
  }

  if (facility) {
    searchFilters.boundaryCode = splitCsv(facility)[0];
  }
  if (block) {
    searchFilters.block = splitCsv(block)[0];
  }
  if (district) {
    searchFilters.district = splitCsv(district)[0];
  }

  if (isSystemFunctional) {
    searchFilters.systemFunctional = splitCsv(isSystemFunctional)[0];
  }

  if (assignee) {
    workflowFilters.assignee = assignee;
  }

  if (mobileNumber) {
    searchFilters.mobileNumber = mobileNumber;
  }

  if (services) {
    workflowFilters.businessService = services;
  }

  searchFilters.tenantId = tenantId;

  if (nearingSLA) {
    searchFilters.nearingSLA = 3 * DAY;
  }

  workflowFilters.moduleName = "Incident";
  workflowFilters.tenantId = tenantId;

  return {
    searchFilters,
    workflowFilters,
    limit,
    offset,
    sortBy,
    sortOrder,
    applicationNumber,
  };
}

export function buildFilterQueryFromState(filters: {
  pgrfilters?: Record<string, Array<{ code: string }>>;
  wfFilters?: Record<string, Array<{ code: string }>>;
}): { pgrQuery: Record<string, string>; wfQuery: Record<string, string> } {
  const pgrQuery: Record<string, string> = {};
  const wfQuery: Record<string, string> = {};

  for (const property of Object.keys(filters.pgrfilters ?? {})) {
    const values = filters.pgrfilters?.[property];
    if (!Array.isArray(values)) {
      continue;
    }
    const params = values.map((item) => item.code).join(",");
    if (params) {
      pgrQuery[property] = params;
    }
  }

  for (const property of Object.keys(filters.wfFilters ?? {})) {
    const values = filters.wfFilters?.[property];
    if (!Array.isArray(values)) {
      continue;
    }
    const params = values.map((item) => item.code).join(",");
    if (params) {
      wfQuery[property] = params;
    }
  }

  return { pgrQuery, wfQuery };
}

export function flattenInboxFilters(
  searchParams: {
    filters?: {
      pgrQuery?: Record<string, string>;
      wfQuery?: Record<string, string>;
    };
    limit?: number;
    offset?: number;
    nearingSLA?: boolean;
    applicationNumber?: string;
  },
  defaults: IncidentFilterInput,
): IncidentFilterInput {
  const pgrQuery = searchParams.filters?.pgrQuery ?? {};
  const wfQuery = searchParams.filters?.wfQuery ?? {};

  return {
    ...defaults,
    ...pgrQuery,
    ...wfQuery,
    limit: searchParams.limit,
    offset: searchParams.offset,
    nearingSLA: searchParams.nearingSLA,
    applicationNumber: searchParams.applicationNumber,
    services: defaults.services ?? [LIVELIHOOD_INCIDENT_BUSINESS_SERVICE],
    sortOrder: defaults.sortOrder ?? "DESC",
  };
}
