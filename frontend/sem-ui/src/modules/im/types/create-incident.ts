import type { LivelihoodAsset, LivelihoodFacility } from "./facility-asset";

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

export interface CreateIncidentFormValues {
  endUser: LivelihoodFacility | null;
  asset: LivelihoodAsset | null;
  complaintType: SelectOption | null;
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
