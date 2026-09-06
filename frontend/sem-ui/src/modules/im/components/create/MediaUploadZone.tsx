import { translateOr, useTranslate } from "@/shared";
import { cn } from "@/ui";
import { CheckCircle2, Info, Trash2, type LucideIcon } from "lucide-react";
import { useEffect, useId, useMemo, useRef } from "react";
import type { UploadedMediaEntry } from "../../types/create-incident";
import { formatFileSize } from "../../utils/file";

interface MediaUploadZoneProps {
  label: string;
  hint: string;
  helperText?: string;
  error?: string;
  icon: LucideIcon;
  accept: string;
  multiple?: boolean;
  disabled?: boolean;
  uploading?: boolean;
  uploads: UploadedMediaEntry[];
  kind: "image" | "video";
  onSelect: (files: FileList) => void;
  onRemove: (fileStoreId: string) => void;
}

function UploadedFileThumbnail({
  entry,
  kind,
  icon: Icon,
}: {
  readonly entry: UploadedMediaEntry;
  readonly kind: "image" | "video";
  readonly icon: LucideIcon;
}) {
  const previewUrl = useMemo(
    () => (kind === "image" ? URL.createObjectURL(entry.file) : null),
    [entry.file, kind],
  );

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  if (previewUrl) {
    return (
      <img
        src={previewUrl}
        alt={entry.file.name}
        className="size-12 shrink-0 rounded-md object-cover"
      />
    );
  }

  return (
    <div className="flex size-12 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground">
      <Icon className="size-5" />
    </div>
  );
}

function UploadedFileCard({
  entry,
  kind,
  icon,
  onRemove,
}: {
  readonly entry: UploadedMediaEntry;
  readonly kind: "image" | "video";
  readonly icon: LucideIcon;
  readonly onRemove: (fileStoreId: string) => void;
}) {
  const { t } = useTranslate();

  return (
    <div className="flex items-center gap-3 rounded-lg border border-input bg-card p-3">
      <UploadedFileThumbnail entry={entry} kind={kind} icon={icon} />
      <div className="min-w-0 flex-1 space-y-1">
        <p className="truncate text-sm font-medium text-foreground">{entry.file.name}</p>
        <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
          <span>{formatFileSize(entry.file.size)}</span>
          <span>•</span>
          <CheckCircle2 className="size-3.5 text-primary" />
          <span>{translateOr(t, "CS_COMMON_COMPLETE", "Complete")}</span>
        </div>
        <div className="h-1.5 w-full overflow-hidden rounded-full bg-muted">
          <div className="h-full w-full rounded-full bg-primary" />
        </div>
      </div>
      <button
        type="button"
        onClick={() => onRemove(entry.fileStoreId)}
        className="shrink-0 rounded-md p-1.5 text-muted-foreground transition-colors hover:bg-destructive/10 hover:text-destructive"
        aria-label={translateOr(t, "CS_COMMON_REMOVE", "Remove")}
      >
        <Trash2 className="size-4" />
      </button>
    </div>
  );
}

export function MediaUploadZone({
  label,
  hint,
  helperText,
  error,
  icon: Icon,
  accept,
  multiple = false,
  disabled = false,
  uploading = false,
  uploads,
  kind,
  onSelect,
  onRemove,
}: MediaUploadZoneProps) {
  const { t } = useTranslate();
  const inputId = useId();
  const inputRef = useRef<HTMLInputElement>(null);

  return (
    <div className="space-y-2">
      <label htmlFor={inputId} className="text-sm font-medium text-foreground">
        {label}
      </label>
      <button
        type="button"
        disabled={disabled || uploading}
        onClick={() => inputRef.current?.click()}
        className={cn(
          "flex min-h-[120px] w-full flex-col items-center justify-center gap-2 rounded-lg border border-dashed border-input bg-card px-4 py-6 text-center transition-colors",
          !disabled && !uploading && "hover:border-primary hover:bg-accent/40",
          (disabled || uploading) && "cursor-not-allowed opacity-60",
        )}
      >
        <div className="flex size-11 items-center justify-center rounded-full bg-accent text-primary">
          <Icon className="size-5" />
        </div>
        <span className="text-sm text-muted-foreground">
          {uploading ? translateOr(t, "CS_COMMON_UPLOADING", "Uploading...") : hint}
        </span>
      </button>
      <input
        ref={inputRef}
        id={inputId}
        type="file"
        className="hidden"
        accept={accept}
        multiple={multiple}
        disabled={disabled || uploading}
        onChange={(event) => {
          if (event.target.files?.length) {
            onSelect(event.target.files);
            event.target.value = "";
          }
        }}
      />
      {uploads.length > 0 ? (
        <div className="space-y-2">
          {uploads.map((entry) => (
            <UploadedFileCard
              key={entry.fileStoreId}
              entry={entry}
              kind={kind}
              icon={Icon}
              onRemove={onRemove}
            />
          ))}
        </div>
      ) : null}
      {error ? (
        <p className="flex items-center gap-1 text-xs text-destructive">
          <Info className="size-3.5 shrink-0" />
          {error}
        </p>
      ) : helperText ? (
        <p className="text-xs text-muted-foreground">{helperText}</p>
      ) : null}
    </div>
  );
}
