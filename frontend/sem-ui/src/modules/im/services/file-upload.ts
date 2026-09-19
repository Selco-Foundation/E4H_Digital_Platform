import { apiClient } from "@/shared";

interface FileStoreResponse {
  files?: Array<{
    fileStoreId?: string;
    masterFileStoreId?: string;
  }>;
}

export async function uploadIncidentFile(
  file: File,
  tenantId: string,
  accessToken: string,
): Promise<{ fileStoreId: string; masterFileStoreId?: string }> {
  const formData = new FormData();
  formData.append("file", file, file.name);
  formData.append("tenantId", tenantId);
  formData.append("module", "Incident");

  const { data } = await apiClient.post<FileStoreResponse>(
    "/filestore/v1/files",
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
        Authorization: `Bearer ${accessToken}`,
      },
    },
  );

  const uploaded = data.files?.[0];
  if (!uploaded?.fileStoreId) {
    throw new Error("FILE_UPLOAD_FAILED");
  }

  return {
    fileStoreId: uploaded.fileStoreId,
    masterFileStoreId: uploaded.masterFileStoreId,
  };
}

export async function uploadIncidentVideo(
  file: File,
  tenantId: string,
  accessToken: string,
): Promise<{ fileStoreId: string; masterFileStoreId?: string }> {
  const formData = new FormData();
  formData.append("file", file, file.name);
  formData.append("tenantId", tenantId);
  formData.append("module", "Incident");

  const { data } = await apiClient.post<FileStoreResponse>(
    "/im-services/v2/video/upload",
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
        Authorization: `Bearer ${accessToken}`,
      },
      timeout: 600_000,
    },
  );

  const uploaded = data.files?.[0];
  if (!uploaded?.fileStoreId) {
    throw new Error("VIDEO_UPLOAD_FAILED");
  }

  return {
    fileStoreId: uploaded.fileStoreId,
    masterFileStoreId: uploaded.masterFileStoreId,
  };
}
