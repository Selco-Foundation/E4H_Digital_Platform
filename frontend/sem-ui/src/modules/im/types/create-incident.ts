export interface SelectOption {
  code: string;
  name: string;
  key?: string;
  menuPath?: string;
  serviceCode?: string;
  id?: string;
  status?: string;
  parentCode?: string;
}

/** A District / Block / Facility option, sourced from the boundary-relationships tree. */
export interface BoundaryOption {
  code: string;
  name: string;
  parentCode?: string;
}

export interface VerificationDocument {
  fileStoreId: string;
  documentUid: string;
  documentType: string;
  additionalDetails: Record<string, unknown>;
}

export interface UploadedMediaEntry {
  file: File;
  fileStoreId: string;
  masterFileStoreId?: string;
  kind: "image" | "video" | "fir";
}

/**
 * Mirrors DIGIT-UI's own "im" CreateComplaint screen exactly (Ticket Location:
 * District/Block/Facility, Ticket Details: Ticket Type/Ticket Subtype/System
 * Functional) rather than livelihood-ui's End User + Asset flow — SEM's real
 * DIGIT-UI im module has no separate end-user/asset selection step at all.
 */
export interface CreateIncidentFormValues {
  district: BoundaryOption | null;
  block: BoundaryOption | null;
  facility: BoundaryOption | null;
  ticketType: SelectOption | null;
  ticketSubType: SelectOption | null;
  systemFunctional: SelectOption | null;
  comments: string;
}

export interface CreateIncidentResponse {
  IncidentWrappers?: Array<{
    incident?: { incidentId?: string };
    workflow?: { action?: string };
  }>;
  Errors?: Array<{ message?: string }>;
  message?: string;
}
