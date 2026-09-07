import { apiClient, type AuthUser } from "@/shared";
import { createRequestInfo } from "@/shared/api/request-info";
import type { JurisdictionBoundaries } from "@/shared";
import type {
  BoundaryOption,
  CreateIncidentResponse,
  SelectOption,
  VerificationDocument,
} from "../types/create-incident";
import {
  LIVELIHOOD_INCIDENT_BUSINESS_SERVICE,
  OPEN_DUPLICATE_APPLICATION_STATUSES,
} from "../constants/workflow";
import { searchInbox } from "./inbox";

export interface CreateIncidentInput {
  tenantId: string;
  district: BoundaryOption;
  block: BoundaryOption;
  facility: BoundaryOption;
  ticketType: SelectOption;
  ticketSubType: SelectOption;
  systemFunctional: SelectOption;
  comments?: string;
  uploadedDocuments: VerificationDocument[];
  user: AuthUser;
  accessToken: string;
}

const DUPLICATE_STATUSES = OPEN_DUPLICATE_APPLICATION_STATUSES;

export function buildVerificationDocuments(
  uploadedDocuments: VerificationDocument[],
): VerificationDocument[] {
  return uploadedDocuments.map((file) => ({
    ...file,
    documentType: file.documentType.toLowerCase().startsWith("video")
      ? "VIDEO"
      : file.documentType.toLowerCase().startsWith("image")
        ? "PHOTO"
        : file.documentType,
  }));
}

/**
 * Workflow action varies by ticket type in DIGIT-UI's im/CreateComplaint — THEFT tickets
 * take "APPLY_THEFT". The RMS-device trigger ("APPLY_RMS_DEVICE") exists in DIGIT-UI too,
 * but its exact ticketType code wasn't confirmed, so it isn't handled here yet — those
 * tickets will currently submit as a plain "APPLY".
 */
function resolveWorkflowAction(ticketTypeCode: string): string {
  return ticketTypeCode === "THEFT" ? "APPLY_THEFT" : "APPLY";
}

export function buildCreateIncidentPayload(input: CreateIncidentInput) {
  const incidentType = input.ticketType.key ?? input.ticketType.code;
  const incidentSubtype = input.ticketSubType.key ?? input.ticketSubType.code;
  const additionalDetail =
    input.uploadedDocuments.length > 0
      ? { additionalDetail: { fileStoreId: input.uploadedDocuments } }
      : {};

  return {
    incident: {
      tenantId: input.tenantId,
      district: input.district.name,
      block: input.block.name,
      boundaryCode: input.facility.code,
      incidentType,
      incidentSubtype,
      systemFunctional: input.systemFunctional.key ?? input.systemFunctional.code,
      comments: input.comments ?? "",
      source: "web",
      reporter: {
        uuid: input.user.uuid,
        userName: input.user.userName,
        name: input.user.name,
        tenantId: input.user.tenantId ?? input.tenantId,
        type: "EMPLOYEE",
      },
      ...additionalDetail,
    },
    workflow: {
      action: resolveWorkflowAction(incidentType),
      verificationDocuments: buildVerificationDocuments(input.uploadedDocuments),
    },
  };
}

export async function createIncident(
  input: CreateIncidentInput,
): Promise<CreateIncidentResponse> {
  const payload = buildCreateIncidentPayload(input);

  try {
    const { data } = await apiClient.post<CreateIncidentResponse>(
      "/im-services/v2/request/_create",
      {
        RequestInfo: createRequestInfo(input.accessToken, input.user),
        ...payload,
      },
      {
        params: { tenantId: input.tenantId },
      },
    );
    return data;
  } catch (error: unknown) {
    const axiosError = error as {
      response?: { data?: { Errors?: Array<{ message?: string }> } };
    };
    return axiosError.response?.data ?? { Errors: [{ message: "CREATE_FAILED" }] };
  }
}

export async function searchPotentialDuplicates(
  tenantId: string,
  jurisdiction: JurisdictionBoundaries,
  facilityId: string,
  incidentType: string,
  accessToken: string,
  user: AuthUser | null | undefined,
) {
  const data = await searchInbox(
    tenantId,
    jurisdiction,
    {
      limit: 100,
      offset: 0,
      services: [LIVELIHOOD_INCIDENT_BUSINESS_SERVICE],
      sortOrder: "DESC",
      facility: facilityId,
      incidentType,
      applicationStatus: DUPLICATE_STATUSES,
    },
    accessToken,
    user,
  );

  return (data.items ?? [])
    .map((item) => ({
      ticketId: item.businessObject?.incident?.incidentId ?? "",
      ticketTenantId: item.businessObject?.incident?.tenantId ?? tenantId,
    }))
    .filter((ticket) => ticket.ticketId);
}
