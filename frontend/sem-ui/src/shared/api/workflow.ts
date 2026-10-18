import { apiClient } from "./client";
import { createRequestInfo } from "./request-info";
import type { AuthUser } from "../stores/auth-store";

export interface WorkflowBusinessServiceState {
  uuid?: string;
  state?: string;
  applicationStatus?: string;
  isStateUpdatable?: boolean;
  actions?: Array<{
    action?: string;
    roles?: string[];
    nextState?: string;
  }>;
}

export interface WorkflowBusinessServiceResponse {
  BusinessServices?: Array<{
    states?: WorkflowBusinessServiceState[];
  }>;
}

export async function fetchWorkflowBusinessService(
  tenantId: string,
  businessService: string,
  accessToken: string,
  user?: AuthUser | null,
): Promise<WorkflowBusinessServiceResponse> {
  const { data } = await apiClient.post<WorkflowBusinessServiceResponse>(
    "/egov-workflow-v2/egov-wf/businessservice/_search",
    {
      RequestInfo: createRequestInfo(accessToken, user),
    },
    { params: { tenantId, businessServices: businessService } },
  );

  return data;
}
