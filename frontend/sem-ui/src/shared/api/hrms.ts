import { tenantId } from "../config/global-config";
import { createRequestInfo } from "./request-info";
import { apiClient } from "./client";
import type { AuthUser } from "../stores/auth-store";

export interface HrmsJurisdiction {
  boundaryType?: string;
  boundary?: string;
}

export interface HrmsEmployee {
  code?: string;
  jurisdictions?: HrmsJurisdiction[];
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
