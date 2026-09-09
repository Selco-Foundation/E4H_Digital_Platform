import { useTranslate } from "@/shared";
import {getAttachmentKind, getFileName} from "@/modules/im/utils/file";
import {FileIcon, FileText} from "lucide-react";

export interface ComplaintVideoEntry {
  master?: string | null;
  original?: string | null;
}

interface ComplaintMediaListProps {
  images: string[];
  videos: ComplaintVideoEntry[];
  imageGridClassName?: string;
}

export function ComplaintMediaList({
  images,
  videos,
  imageGridClassName = "grid max-w-[760px] grid-cols-2 gap-3 sm:grid-cols-[repeat(auto-fill,minmax(150px,180px))]",
}: ComplaintMediaListProps) {
  const { t } = useTranslate();
  const attachmentLabel = t("CS_COMMON_ATTACHMENT");

  if (!images.length && !videos.length) {
    return null;
  }

  return (
    <div className="space-y-4">
      {images.length > 0 ? (
        <div className={imageGridClassName}>
          {images.map((src, index) => {
            const label = `${attachmentLabel} ${index + 1}`;
            const kind = getAttachmentKind(src);

            if (kind === "image") {
              return (
                <a
                  key={src}
                  href={src}
                  target="_blank"
                  rel="noreferrer"
                  aria-label={label}
                  className="overflow-hidden rounded-lg border border-border bg-muted/30"
                >
                  <img
                    src={src}
                    alt={label}
                    className="aspect-square w-full object-cover"
                  />
                </a>
              );
            }

            const fileName = getFileName(src);
            const ext = fileName.split(".").pop()?.toUpperCase();
            const Icon = kind === "pdf" ? FileText : FileIcon;

            return (
              <a
                key={src}
                href={src}
                target="_blank"
                rel="noreferrer"
                aria-label={label}
                className="flex aspect-square w-full flex-col items-center justify-center gap-2 overflow-hidden rounded-lg border border-border bg-muted/30 p-3 text-center transition-colors hover:bg-muted/50"
              >
                <Icon className="h-8 w-8 shrink-0 text-muted-foreground" aria-hidden="true" />
                <span className="line-clamp-2 w-full break-words text-xs text-muted-foreground">
                  {fileName}
                </span>
                {ext ? (
                  <span className="rounded bg-muted px-1.5 py-0.5 text-[10px] font-medium uppercase text-muted-foreground">
                    {ext}
                  </span>
                ) : null}
              </a>
            );

          })}
        </div>
      ) : null}

      {videos.length > 0 ? (
        <div className="grid max-w-[450px] gap-3">
          {videos.map((video, index) => (
            <div key={`${video.original ?? video.master ?? index}`} className="space-y-2">
              {video.original ? (
                <video
                  controls
                  className="w-full rounded-lg border border-border"
                  src={video.original}
                />
              ) : video.master ? (
                <a
                  href={video.master}
                  target="_blank"
                  rel="noreferrer"
                  className="text-sm text-primary hover:underline"
                >
                  {t("CS_COMMON_VIEW_VIDEO")}
                </a>
              ) : null}
            </div>
          ))}
        </div>
      ) : null}
    </div>
  );
}
