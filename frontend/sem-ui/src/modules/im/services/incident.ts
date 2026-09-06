import { apiClient, type AuthUser } from "@/shared";
import { createRequestInfo } from "@/shared/api/request-info";
import type { JurisdictionBoundaries } from "@/shared";
import type {
  CreateIncidentResponse,
  SelectOption,
  VerificationDocument,
} from "../types/create-incident";
import type { LivelihoodAsset, LivelihoodFacility } from "../types/facility-asset";
import {
  LIVELIHOOD_INCIDENT_BUSINESS_SERVICE,
  OPEN_DUPLICATE_APPLICATION_STATUSES,
} from "../constants/workflow";
import { hasRole } from "../utils/access";
import { searchInbox } from "./inbox";

export interface CreateIncidentInput {
  tenantId: string;
  endUser: LivelihoodFacility;
  asset: LivelihoodAsset;
  complaintType: SelectOption;
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

export function buildCreateIncidentPayload(input: CreateIncidentInput) {
  const incidentType =
    input.complaintType.serviceCode ??
    input.complaintType.key ??
    input.complaintType.code;

  const isPocCreate = hasRole(input.user.roles, "LIVELIHOOD_POC");
  const additionalDetail =
    input.uploadedDocuments.length > 0
      ? { additionalDetail: { fileStoreId: input.uploadedDocuments } }
      : {};

  if (isPocCreate) {
    return {
      incident: {
        incidentType,
        reporterType: "COMPLAINANT",
        tenantId: input.tenantId,
        accountId: input.endUser.endUserUuid,
        reporterTenant: input.tenantId,
        facilityId: input.endUser.facilityId,
        assetId: input.asset.assetId,
        boundaryCode: input.asset.boundaryCode,
        comments: input.comments ?? "",
        createdOnBehalf: true,
        entryChannel: "POC_MANUAL",
        reporter: buildOnBehalfReporter(input.endUser, input.tenantId),
        ...additionalDetail,
      },
      workflow: {
        action: "CREATE",
        verificationDocuments: buildVerificationDocuments(input.uploadedDocuments),
      },
    };
  }

  return {
    incident: {
      tenantId: input.tenantId,
      facilityId: input.endUser.facilityId,
      assetId: input.asset.assetId,
      incidentType,
      boundaryCode: input.asset.boundaryCode,
      comments: input.comments ?? "",
      entryChannel: "DIRECT",
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
      action: "CREATE",
      verificationDocuments: buildVerificationDocuments(input.uploadedDocuments),
    },
  };
}

function buildOnBehalfReporter(endUser: LivelihoodFacility, tenantId: string) {
  return {
    uuid: endUser.endUserUuid,
    userName: endUser.facilityPocUsername,
    name: endUser.facilityPocName,
    mobileNumber: endUser.facilityPocPhone,
    emailId: endUser.facilityPocEmail,
    type: "EMPLOYEE",
    tenantId,
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
