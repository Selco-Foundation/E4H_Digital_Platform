import type { UploadedMediaEntry, VerificationDocument } from "../types/create-incident";

// .docx's MIME type is 71 chars, over the backend's 64-char documentType limit, so
// non-media files use their extension instead; images/videos keep their MIME type since
// downstream code classifies them by its "image"/"video" prefix.
function getDocumentType(file: File): string {
  if (file.type.startsWith("image/") || file.type.startsWith("video/")) {
    return file.type;
  }
  const extension = file.name.split(".").pop();
  return extension ? extension.toUpperCase() : "DOCUMENT";
}

export function buildUploadedDocuments(uploads: UploadedMediaEntry[]): VerificationDocument[] {
  const documents: VerificationDocument[] = [];
  let videoCount = 0;

  for (const upload of uploads) {
    if (upload.kind === "video") {
      videoCount += 1;
      const videoUid = `video${videoCount}`;
      if (upload.masterFileStoreId) {
        documents.push({
          fileStoreId: upload.masterFileStoreId,
          documentUid: videoUid,
          documentType: "HLS",
          additionalDetails: {},
        });
      }
      documents.push({
        fileStoreId: upload.fileStoreId,
        documentUid: videoUid,
        documentType: upload.file.type,
        additionalDetails: {},
      });
      continue;
    }

    if (upload.kind === "fir") {
      documents.push({
        fileStoreId: upload.fileStoreId,
        documentUid: "",
        documentType: "FIR_DOCUMENT",
        additionalDetails: {},
      });
      continue;
    }

    documents.push({
      fileStoreId: upload.fileStoreId,
      documentUid: "",
      documentType: getDocumentType(upload.file),
      additionalDetails: {},
    });
  }

  const seen = new Set<string>();
  return documents.filter((doc) => {
    if (!doc.fileStoreId || seen.has(doc.fileStoreId)) {
      return false;
    }
    seen.add(doc.fileStoreId);
    return true;
  });
}
