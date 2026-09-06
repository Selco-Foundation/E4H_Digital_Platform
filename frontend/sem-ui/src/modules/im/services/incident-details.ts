import { apiClient, type AuthUser } from "@/shared";
import { createRequestInfo } from "@/shared/api/request-info";
import type { VerificationDocument } from "../types/create-incident";
import type {
  FileStoreUrlResponse,
  IncidentSearchResponse,
} from "../types/incident-details";

function getThumbnailUrl(url: string): string {
  if (url.includes(",")) {
    const parts = url.split(",");
    return parts[3] || parts[0] || url;
  }
  return url;
}

/** Full-size original — variant without large/medium/small in path (digit-ui pattern). */
export function getOriginalFileUrl(url: string): string {
  if (!url.includes(",")) {
    return url;
  }
  const parts = url.split(",");
  const original = parts.find(
    (part) =>
      !part.includes("/large/") &&
      !part.includes("/medium/") &&
      !part.includes("/small/"),
  );
  return original ?? parts[0] ?? url;
}

export async function fetchFileUrls(
  fileStoreIds: string[],
  tenantId: string,
  accessToken: string,
  user?: AuthUser | null,
): Promise<FileStoreUrlResponse> {
  if (!fileStoreIds.length) {
    return { fileStoreIds: [] };
  }

  const { data } = await apiClient.get<FileStoreUrlResponse>(
    "/filestore/v1/files/url",
    {
      params: {
        tenantId,
        fileStoreIds: fileStoreIds.join(","),
      },
    },
  );

  return data;
}

export async function resolveVerificationMedia(
  documents: VerificationDocument[],
  tenantId: string,
  accessToken: string,
  user?: AuthUser | null,
) {
  const ids = documents.map((doc) => doc.fileStoreId).filter(Boolean);
  if (!ids.length) {
    return { thumbs: [], images: [], videos: [] as Array<{ master?: string | null; original?: string | null }> };
  }

  const response = await fetchFileUrls(ids, tenantId, accessToken, user);
  const urlMap = new Map(
    (response.fileStoreIds ?? []).map((entry) => [entry.id, entry.url]),
  );

  const images: string[] = [];
  const videos = new Map<string, { master?: string | null; original?: string | null }>();

  for (const doc of documents) {
    const rawUrl = urlMap.get(doc.fileStoreId);
    if (!rawUrl) {
      continue;
    }

    const fileUrl = getOriginalFileUrl(rawUrl);
    const docType = doc.documentType?.toUpperCase() ?? "";

    if (
      docType === "HLS" ||
      docType.startsWith("VIDEO") ||
      doc.documentType?.toLowerCase().startsWith("video")
    ) {
      const videoKey = doc.documentUid || doc.fileStoreId;
      if (!videos.has(videoKey)) {
        videos.set(videoKey, { master: null, original: null });
      }
      const entry = videos.get(videoKey)!;
      if (docType === "HLS") {
        entry.master = fileUrl;
      } else {
        entry.original = fileUrl;
      }
      continue;
    }

    images.push(fileUrl);
  }

  const thumbs = (response.fileStoreIds ?? []).map((entry) =>
    getThumbnailUrl(entry.url ?? ""),
  );

  return {
    thumbs,
    images,
    videos: Array.from(videos.values()),
  };
}

export async function searchIncidentById(
  tenantId: string,
  incidentId: string,
  accessToken: string,
  user?: AuthUser | null,
): Promise<IncidentSearchResponse> {
  const { data } = await apiClient.post<IncidentSearchResponse>(
    "/im-services/v2/request/_search",
    {
      RequestInfo: createRequestInfo(accessToken, user),
    },
    {
      params: { tenantId, incidentId },
    },
  );

  return data;
}
