export interface ImInboxFilters {
  pgrQuery?: Record<string, string>;
  wfQuery?: Record<string, string>;
  wfFilters?: {
    assignee?: Array<{ code: string }>;
    wfStatus?: Array<{ code: string }>;
  };
  pgrfilters?: {
    assetType?: Array<{ code: string; name?: string; key?: string }>;
    facility?: Array<{ code: string; name?: string }>;
    state?: Array<{ code: string; name?: string }>;
    district?: Array<{ code: string; name?: string }>;
    block?: Array<{ code: string; name?: string }>;
    applicationStatus?: Array<{ code: string }>;
  };
}

export interface ImInboxSearchParams {
  filters?: ImInboxFilters;
  limit?: number;
  offset?: number;
  nearingSLA?: boolean;
}

export interface InboxIncidentReporter {
  name?: string;
  uuid?: string;
  userName?: string;
}

export interface InboxIncident {
  incidentId: string;
  incidentType: string;
  incidentSubType?: string;
  facilityId?: string;
  assetId?: string;
  boundaryCode?: string;
  phcType?: string;
  applicationStatus: string;
  tenantId: string;
  isPotentialDuplicate?: boolean;
  boundary?: { facilityCode?: string };
  reporter?: InboxIncidentReporter;
}

export interface InboxItem {
  businessObject: {
    incident?: InboxIncident;
    slaRemaining?: number;
    totalSlaRemaining?: number;
  };
  ProcessInstance?: {
    assignes?: Array<{ uuid?: string; name?: string }>;
  };
}

export interface InboxStatusMapEntry {
  statusid: string;
  count: number;
}

export interface InboxSearchResponse {
  items: InboxItem[];
  totalCount: number;
  nearingSlaCount?: number;
  statusMap?: InboxStatusMapEntry[];
}

export interface InboxRow {
  incidentId: string;
  incidentType: string;
  assetLabel: string;
  status: string;
  taskOwner: string;
  sla: string;
  endUser: string;
  tenantId: string;
  potentialDuplicate: boolean;
}

export interface InboxDataResult {
  combinedRes: InboxRow[];
  total: number;
  statusArray: InboxStatusMapEntry[];
}

export interface ComplaintTypeOption {
  key: string;
  name: string;
  menuPath?: string;
  serviceCode?: string;
}

