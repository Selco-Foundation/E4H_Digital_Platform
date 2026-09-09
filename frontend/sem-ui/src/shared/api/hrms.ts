import { tenantId } from "../config/global-config";
import { createRequestInfo } from "./request-info";
import { apiClient } from "./client";
import type { AuthUser } from "../stores/auth-store";

export interface HrmsJurisdiction {
  boundaryType?: string;
  boundary?: string;
}

export interface HrmsEmployeeUser {
  uuid?: string;
  name?: string;
  mobileNumber?: string;
  userName?: string;
}

export interface HrmsEmployee {
  code?: string;
  jurisdictions?: HrmsJurisdiction[];
  user?: HrmsEmployeeUser;
}

interface HrmsSearchResponse {
  Employees?: HrmsEmployee[];
}

export async function searchHrmsEmployee(
  employeeCode: string,
  accessToken: string,
  user?: AuthUser | null,
): Promise<HrmsEmployee | null> {
  const response = await apiClient.post<HrmsSearchResponse>(
    "/egov-hrms/employees/_search",
    {
      RequestInfo: createRequestInfo(accessToken, user),
    },
    {
      params: {
        tenantId: tenantId(),
        codes: employeeCode,
      },
    },
  );

  return response.data.Employees?.[0] ?? null;
}

/**
 * Employees eligible to be assigned a ticket for a given workflow action —
 * matches DIGIT-UI's assignment picker, which searches `/egov-hrms/employees/_search`
 * by the action's allowed roles and the ticket's own jurisdiction (confirmed
 * against the real `EmployeeSearchCriteria`, which supports `roles` and
 * `boundaryCodes` params).
 */
export async function searchHrmsEmployeesByRole(
  roles: string[],
  boundaryCodes: string[],
  accessToken: string,
  user?: AuthUser | null,
): Promise<HrmsEmployee[]> {
  if (!roles.length) {
    return [];
  }
  const response = await apiClient.post<HrmsSearchResponse>(
    "/egov-hrms/employees/_search",
    {
      RequestInfo: createRequestInfo(accessToken, user),
    },
    {
      params: {
        tenantId: tenantId(),
        roles: roles.join(","),
        ...(boundaryCodes.length ? { boundaryCodes: boundaryCodes.join(",") } : {}),
      },
    },
  );

  return response.data.Employees ?? [];
}
