import type { AuthUser } from "../stores/auth-store";
import { apiClient } from "./client";
import { createRequestInfo } from "./request-info";

export interface EmployeeProfile extends Record<string, unknown> {
  uuid?: string;
  userName?: string;
  name?: string;
  mobileNumber?: string;
  emailId?: string;
  tenantId?: string;
  type?: string;
  roles?: Array<{ code?: string; name?: string; tenantId?: string }>;
}

interface UserSearchResponse {
  user?: EmployeeProfile[];
}

export async function searchCurrentUser(
  uuid: string,
  tenantId: string,
  accessToken: string,
  user?: AuthUser | null,
): Promise<EmployeeProfile | null> {
  const response = await apiClient.post<UserSearchResponse>("/user/_search", {
    RequestInfo: createRequestInfo(accessToken, user),
    tenantId,
    uuid: [uuid],
    pageSize: "100",
  });

  return response.data.user?.[0] ?? null;
}

interface UpdateUserProfileResponse {
  user?: EmployeeProfile[];
}

export async function updateUserProfile(
  profile: EmployeeProfile,
  tenantId: string,
  accessToken: string,
  user?: AuthUser | null,
): Promise<EmployeeProfile | null> {
  const response = await apiClient.post<UpdateUserProfileResponse>(
    "/user/profile/_update",
    { RequestInfo: createRequestInfo(accessToken, user), user: profile },
    { params: { tenantId } },
  );

  return response.data.user?.[0] ?? null;
}

export interface ChangePasswordInSessionPayload {
  existingPassword: string;
  newPassword: string;
  confirmPassword: string;
  username: string;
  tenantId: string;
}

export async function changePasswordInSession(
  payload: ChangePasswordInSessionPayload,
  accessToken: string,
  user?: AuthUser | null,
): Promise<void> {
  await apiClient.post(
    "/user/password/_update",
    { RequestInfo: createRequestInfo(accessToken, user), ...payload, type: "EMPLOYEE" },
    { params: { tenantId: payload.tenantId } },
  );
}
