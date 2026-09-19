import type { AuthUser } from "../stores/auth-store";

export function createRequestInfo(accessToken?: string, user?: AuthUser | null) {
  return {
    apiId: "Rainmaker",
    ...(accessToken ? { authToken: accessToken } : {}),
    ...(user ? { userInfo: user } : {}),
  };
}
