import { apiClient } from "./client";

export interface QrLoginResolveParams {
  tenantId: string;
  facilityId: string;
}

export interface QrLoginResolveResponse {
  userName: string;
  mobileNumber?: string;
  userUuid?: string;
  facilityId?: string;
  facilityBoundaryCode?: string;
  scannedAsset?: unknown;
}

export async function resolveQrLogin(
  params: QrLoginResolveParams,
): Promise<QrLoginResolveResponse> {
  const { data } = await apiClient.post<QrLoginResolveResponse>(
    "/asset-registry/v1/asset/qr/_resolve",
    {
      RequestInfo: {
        apiId: "sem-ui-qr-otp",
        ver: "1.0",
        ts: 0,
        action: "RESOLVE",
        msgId: `qr-resolve-${Date.now()}`,
      },
      tenantId: params.tenantId,
      facilityId: params.facilityId,
    },
  );

  return data;
}
