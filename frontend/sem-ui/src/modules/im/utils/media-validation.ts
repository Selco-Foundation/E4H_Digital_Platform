export const MAX_IMAGE_COUNT = 5;
export const MAX_IMAGE_SIZE_MB = 10;
export const MAX_VIDEO_COUNT = 2;
export const MAX_VIDEO_SIZE_MB = 50;
export const MAX_COMMENT_LENGTH = 1000;
export const MAX_QUOTATION_SIZE_MB = 10;
/** DIGIT-UI's theft-ticket "Upload FIR or Police Complaint Letter" field. */
export const MAX_FIR_COUNT = 5;
export const MAX_FIR_SIZE_MB = 5;
/** DIGIT-UI's Assign/Decline "Supporting Documents" field: "Only .jpg and .pdf files. 5 MB max file size." */
export const MAX_ACTION_DOCUMENT_SIZE_MB = 5;

const IMAGE_EXTENSIONS = ["jpg", "jpeg", "png"];
const VIDEO_EXTENSIONS = ["mp4", "mov", "avi", "wmv"];
const FIR_EXTENSIONS = ["pdf", "jpg", "jpeg", "png"];
const QUOTATION_EXTENSIONS = ["pdf", "doc", "docx"];
const QUOTATION_MIME_TYPES = [
  "application/pdf",
  "application/msword",
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
];
const ACTION_DOCUMENT_EXTENSIONS = ["jpg", "jpeg", "pdf"];
const ACTION_DOCUMENT_MIME_TYPES = ["image/jpeg", "application/pdf"];

function getExtension(file: File): string {
  return file.name.split(".").pop()?.toLowerCase() ?? "";
}

export function isAllowedImageFile(file: File): boolean {
  return IMAGE_EXTENSIONS.includes(getExtension(file)) || file.type.startsWith("image/");
}

export function isAllowedVideoFile(file: File): boolean {
  return VIDEO_EXTENSIONS.includes(getExtension(file)) || file.type.startsWith("video/");
}

export function isAllowedFirFile(file: File): boolean {
  return (
    FIR_EXTENSIONS.includes(getExtension(file)) ||
    file.type === "application/pdf" ||
    file.type.startsWith("image/")
  );
}

export function isAllowedQuotationFile(file: File): boolean {
  return (
    QUOTATION_EXTENSIONS.includes(getExtension(file)) ||
    QUOTATION_MIME_TYPES.includes(file.type)
  );
}

export type MediaKind = "image" | "video" | "fir";
export type MediaValidationErrorCode = "COUNT" | "SIZE" | "FORMAT";

export interface MediaValidationError {
  code: MediaValidationErrorCode;
  fileName?: string;
}

const MEDIA_KIND_CONFIG: Record<
  MediaKind,
  { maxCount: number; maxSizeMb: number; isAllowed: (file: File) => boolean }
> = {
  image: { maxCount: MAX_IMAGE_COUNT, maxSizeMb: MAX_IMAGE_SIZE_MB, isAllowed: isAllowedImageFile },
  video: { maxCount: MAX_VIDEO_COUNT, maxSizeMb: MAX_VIDEO_SIZE_MB, isAllowed: isAllowedVideoFile },
  fir: { maxCount: MAX_FIR_COUNT, maxSizeMb: MAX_FIR_SIZE_MB, isAllowed: isAllowedFirFile },
};

export function validateMediaFiles(
  files: File[],
  existingCount: number,
  kind: MediaKind,
): MediaValidationError | null {
  const { maxCount, maxSizeMb, isAllowed } = MEDIA_KIND_CONFIG[kind];
  const maxSizeBytes = maxSizeMb * 1024 * 1024;

  if (existingCount + files.length > maxCount) {
    return { code: "COUNT" };
  }

  for (const file of files) {
    if (!isAllowed(file)) {
      return { code: "FORMAT", fileName: file.name };
    }
    if (file.size > maxSizeBytes) {
      return { code: "SIZE", fileName: file.name };
    }
  }

  return null;
}

export function validateQuotationFiles(files: File[]): MediaValidationError | null {
  const maxSizeBytes = MAX_QUOTATION_SIZE_MB * 1024 * 1024;

  for (const file of files) {
    if (!isAllowedQuotationFile(file)) {
      return { code: "FORMAT", fileName: file.name };
    }
    if (file.size > maxSizeBytes) {
      return { code: "SIZE", fileName: file.name };
    }
  }

  return null;
}

export function isAllowedActionDocumentFile(file: File): boolean {
  return (
    ACTION_DOCUMENT_EXTENSIONS.includes(getExtension(file)) ||
    ACTION_DOCUMENT_MIME_TYPES.includes(file.type)
  );
}

export function validateActionDocumentFiles(files: File[]): MediaValidationError | null {
  const maxSizeBytes = MAX_ACTION_DOCUMENT_SIZE_MB * 1024 * 1024;

  for (const file of files) {
    if (!isAllowedActionDocumentFile(file)) {
      return { code: "FORMAT", fileName: file.name };
    }
    if (file.size > maxSizeBytes) {
      return { code: "SIZE", fileName: file.name };
    }
  }

  return null;
}
