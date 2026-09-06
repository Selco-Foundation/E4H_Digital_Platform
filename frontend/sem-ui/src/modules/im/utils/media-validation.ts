export const MAX_IMAGE_COUNT = 5;
export const MAX_IMAGE_SIZE_MB = 10;
export const MAX_VIDEO_COUNT = 2;
export const MAX_VIDEO_SIZE_MB = 50;
export const MAX_COMMENT_LENGTH = 1000;
export const MAX_QUOTATION_SIZE_MB = 10;

const IMAGE_EXTENSIONS = ["jpg", "jpeg", "png"];
const VIDEO_EXTENSIONS = ["mp4", "mov", "avi", "wmv"];
const QUOTATION_EXTENSIONS = ["pdf", "doc", "docx"];
const QUOTATION_MIME_TYPES = [
  "application/pdf",
  "application/msword",
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
];

function getExtension(file: File): string {
  return file.name.split(".").pop()?.toLowerCase() ?? "";
}

export function isAllowedImageFile(file: File): boolean {
  return IMAGE_EXTENSIONS.includes(getExtension(file)) || file.type.startsWith("image/");
}

export function isAllowedVideoFile(file: File): boolean {
  return VIDEO_EXTENSIONS.includes(getExtension(file)) || file.type.startsWith("video/");
}

export function isAllowedQuotationFile(file: File): boolean {
  return (
    QUOTATION_EXTENSIONS.includes(getExtension(file)) ||
    QUOTATION_MIME_TYPES.includes(file.type)
  );
}

export type MediaKind = "image" | "video";
export type MediaValidationErrorCode = "COUNT" | "SIZE" | "FORMAT";

export interface MediaValidationError {
  code: MediaValidationErrorCode;
  fileName?: string;
}

export function validateMediaFiles(
  files: File[],
  existingCount: number,
  kind: MediaKind,
): MediaValidationError | null {
  const maxCount = kind === "image" ? MAX_IMAGE_COUNT : MAX_VIDEO_COUNT;
  const maxSizeBytes =
    (kind === "image" ? MAX_IMAGE_SIZE_MB : MAX_VIDEO_SIZE_MB) * 1024 * 1024;
  const isAllowed = kind === "image" ? isAllowedImageFile : isAllowedVideoFile;

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
