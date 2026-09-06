import { translateOr } from "@/shared";
import type { ComplaintDetailsData, ComplaintDetailsRow, Incident, IncidentWorkflow } from "../types/incident-details";
import { TERMINAL_APPLICATION_STATUSES } from "../constants/workflow";
import { formatEpochToDate } from "./date-format";

export function buildComplaintDetailRows(
  incidentId: string,
  incident: Incident,
  t: (key: string) => string,
): ComplaintDetailsRow[] {
  const filedDate = incident.auditDetails?.createdTime;

  return [
    { labelKey: "CS_COMPLAINT_DETAILS_TICKET_NO", value: incidentId },
    {
      labelKey: "CS_COMPLAINT_DETAILS_APPLICATION_STATUS",
      value: `CS_COMMON_${incident.applicationStatus}`,
    },
    {
      labelKey: "CS_ADDCOMPLAINT_TICKET_TYPE",
      value: `SERVICEDEFS.${incident.incidentType.toUpperCase()}`,
    },
    {
      labelKey: "CS_ADDCOMPLAINT_ASSET",
      value: incident.boundaryCode ? `BOUNDARY_${incident.boundaryCode}` : "-",
    },
    { labelKey: "CS_ADDCOMPLAINT_BLOCK", value: incident.block ?? "-" },
    { labelKey: "CS_ADDCOMPLAINT_DISTRICT", value: incident.district ?? "-" },
    { labelKey: "CS_COMPLAINT_COMMENTS", value: incident.comments?.length?  incident.comments :  "-" },
    {
      labelKey: "CS_COMPLAINT_FILED_DATE",
      value: formatEpochToDate(filedDate),
    },
  ];
}

export function translateDetailValue(
  value: string,
  t: (key: string) => string,
): string {
  return translateOr(t, value, value);
}

export function buildComplaintDetailsData(
  incidentId: string,
  incident: Incident,
  workflow: IncidentWorkflow,
  media: {
    images: string[];
    videos: Array<{ master?: string | null; original?: string | null }>;
    thumbnails: string[];
  },
  t: (key: string) => string,
): ComplaintDetailsData {
  return {
    incidentId,
    tenantId: incident.tenantId,
    rows: buildComplaintDetailRows(incidentId, incident, t),
    incident,
    workflow,
    images: media.images,
    videos: media.videos,
    thumbnails: media.thumbnails,
  };
}

export function isClosedTicket(status?: string): boolean {
  return (TERMINAL_APPLICATION_STATUSES as readonly string[]).includes(status ?? "");
}
