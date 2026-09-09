import { searchHrmsEmployeesByRole, useAuthStore, useTranslate } from "@/shared";
import { Button } from "@/ui";
import { useMutation } from "@tanstack/react-query";
import { Files, Trash2 } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import {
  getWorkflowActionConfig,
  isQuotationRequiredAction,
  isSupportedWorkflowAction,
  REOPEN_REASON_OPTIONS,
} from "../../constants/workflow-actions";
import type { UploadedMediaEntry } from "../../types/create-incident";
import type {
  ComplaintDetailsData,
  MdmsReasonOption,
  WorkflowDetailsData,
} from "../../types/incident-details";
import { uploadIncidentFile } from "../../services/file-upload";
import {
  fetchReasonOptions,
  updateIncidentAction,
} from "../../services/workflow";
import { buildUploadedDocuments } from "../../utils/create-incident-documents";
import {
  MAX_ACTION_DOCUMENT_SIZE_MB,
  MAX_COMMENT_LENGTH,
  MAX_IMAGE_COUNT,
  MAX_QUOTATION_SIZE_MB,
  validateActionDocumentFiles,
  validateQuotationFiles,
} from "../../utils/media-validation";
import { FormSelectField } from "../create/FormSelectField";

interface ComplaintActionDialogProps {
  action: string;
  complaintDetails: ComplaintDetailsData;
  workflowDetails: WorkflowDetailsData;
  onClose: () => void;
  onComplete: () => Promise<void>;
}

function getReasonLabel(t: (key: string) => string, action: string): string {
  if (action === "MARK_OUT_OF_SCOPE") {
    return t("WF_OUT_OF_SCOPE_REASON");
  }
  if (action === "SENDBACK") {
    return t("WF_SENDBACK_REASON");
  }
  return t("WF_REJECT_REASON");
}

function getOutOfWarrantyHelperText(
  t: (key: string) => string,
  action: string,
  endUserName: string,
): string | null {
  if (action !== "OUT_OF_WARRANTY") {
    return null;
  }
  return t("WF_OUT_OF_WARRANTY_HELPER").replace("{endUserName}", endUserName);
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) {
    return `${bytes} B`;
  }
  const kb = bytes / 1024;
  if (kb < 1024) {
    return `${Math.round(kb)} KB`;
  }
  return `${(kb / 1024).toFixed(1)} MB`;
}

interface ActionDocumentsFieldProps {
  requiresQuotation: boolean;
  documentsRequired: boolean;
  uploads: UploadedMediaEntry[];
  isUploading: boolean;
  maxFiles: number;
  onUpload: (files: FileList) => Promise<void>;
  onRemove: (index: number) => void;
  t: (key: string) => string;
}

function ActionDocumentsField({
  requiresQuotation,
  documentsRequired,
  uploads,
  isUploading,
  maxFiles,
  onUpload,
  onRemove,
  t,
}: ActionDocumentsFieldProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const maxFilesReached = uploads.length >= maxFiles;
  const label = requiresQuotation
    ? t("WF_QUOTATION_DOCUMENT")
    : t("WF_SUPPORTING_DOCUMENTS");
  const accept = requiresQuotation
    ? ".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    : ".jpg,.jpeg,.pdf,image/jpeg,application/pdf";

  return (
    <div className="space-y-2">
      <label className="text-sm font-medium text-ink-950">
        {label}
        {documentsRequired ? <span className="text-destructive"> *</span> : null}
      </label>

      <button
        type="button"
        disabled={isUploading || maxFilesReached}
        onClick={() => inputRef.current?.click()}
        className="flex w-full cursor-pointer flex-col items-center justify-center gap-2 rounded border-2 border-dashed border-ink-300 bg-card py-6 disabled:cursor-not-allowed disabled:opacity-60"
      >
        <span className="flex size-10 items-center justify-center rounded-full border border-[#D2EBD8] bg-accent text-primary">
          <Files className="size-5" />
        </span>
        <span className="text-sm text-ink-950">
          {t("WF_TAP_TO_UPLOAD")}
        </span>
      </button>
      <p className="text-xs text-ink-400">
        {maxFilesReached
          ? t("WF_MAX_FILES_REACHED").replace("{MAX_COUNT}", String(maxFiles))
          : requiresQuotation
            ? t("WF_MAX_FILES_HINT").replace(
                "{MAX_COUNT}",
                String(maxFiles),
              )
            : t("WF_SUPPORTING_DOCUMENTS_HINT").replace("{MAX_SIZE}", String(MAX_ACTION_DOCUMENT_SIZE_MB))}
      </p>
      <input
        ref={inputRef}
        type="file"
        className="hidden"
        accept={accept}
        multiple={!requiresQuotation}
        disabled={isUploading || maxFilesReached}
        onChange={(event) => {
          if (event.target.files?.length) {
            onUpload(event.target.files).catch(() => {});
            event.target.value = "";
          }
        }}
      />

      {uploads.length > 0 ? (
        <div className="space-y-3">
          {uploads.map((upload, index) => (
            <div
              key={`${upload.file.name}-${index}`}
              className="flex items-center gap-3 rounded border border-ink-300 p-4"
            >
              <span className="flex size-10 shrink-0 items-center justify-center rounded bg-muted text-[10px] font-bold text-muted-foreground uppercase">
                {upload.file.name.split(".").pop()}
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-ink-600">{upload.file.name}</p>
                <p className="text-sm text-ink-600">{formatFileSize(upload.file.size)}</p>
              </div>
              <button
                type="button"
                onClick={() => onRemove(index)}
                aria-label={t("CS_COMMON_REMOVE")}
                className="shrink-0 cursor-pointer text-ink-400 hover:text-destructive"
              >
                <Trash2 className="size-5" />
              </button>
            </div>
          ))}
        </div>
      ) : null}
    </div>
  );
}

interface AssigneeOption {
  code: string;
  name: string;
}

export function ComplaintActionDialog({
  action,
  complaintDetails,
  workflowDetails,
  onClose,
  onComplete,
}: ComplaintActionDialogProps) {
  const { t } = useTranslate();
  const user = useAuthStore((state) => state.user);
  const accessToken = useAuthStore((state) => state.accessToken);

  const actionConfig = getWorkflowActionConfig(action);

  const [comments, setComments] = useState("");
  const [reasonOptions, setReasonOptions] = useState<MdmsReasonOption[]>([]);
  const [selectedReason, setSelectedReason] = useState<MdmsReasonOption | null>(null);
  const [selectedReopenReason, setSelectedReopenReason] = useState<string>("");
  const [assigneeOptions, setAssigneeOptions] = useState<AssigneeOption[]>([]);
  const [selectedAssignee, setSelectedAssignee] = useState<AssigneeOption | null>(null);
  const [uploads, setUploads] = useState<UploadedMediaEntry[]>([]);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!actionConfig?.reasonMaster || !accessToken) {
      setReasonOptions([]);
      return;
    }
    void fetchReasonOptions(accessToken, user, [actionConfig.reasonMaster]).then((masters) => {
      setReasonOptions(
        (masters[actionConfig.reasonMaster!] ?? []).filter(
          (option) => option.active !== false,
        ),
      );
    });
  }, [accessToken, actionConfig?.reasonMaster, user]);

  useEffect(() => {
    if (!actionConfig?.needsAssignee || !accessToken) {
      setAssigneeOptions([]);
      return;
    }
    const roles =
      workflowDetails.actionState?.nextActions?.find((entry) => entry.action === action)
        ?.assigneeRoles ?? [];
    const boundaryCodes = [
      complaintDetails.incident.boundaryCode,
      complaintDetails.incident.block,
      complaintDetails.incident.district,
    ].filter((code): code is string => Boolean(code));

    void searchHrmsEmployeesByRole(roles, boundaryCodes, accessToken, user).then((employees) => {
      setAssigneeOptions(
        employees
          .filter((employee) => employee.user?.uuid)
          .map((employee) => ({
            code: employee.user!.uuid!,
            name: employee.user?.name ?? employee.code ?? employee.user!.uuid!,
          })),
      );
    });
  }, [
    accessToken,
    action,
    actionConfig?.needsAssignee,
    complaintDetails.incident.block,
    complaintDetails.incident.boundaryCode,
    complaintDetails.incident.district,
    user,
    workflowDetails.actionState,
  ]);

  const mutation = useMutation({
    mutationFn: async () => {
      if (!user || !accessToken || !actionConfig || !isSupportedWorkflowAction(action)) {
        throw new Error("AUTH_REQUIRED");
      }
      if (actionConfig.comment === "required" && !comments.trim()) {
        throw new Error("COMMENT_REQUIRED");
      }
      if (comments.trim().length > MAX_COMMENT_LENGTH) {
        throw new Error("COMMENT_TOO_LONG");
      }
      if (actionConfig.reasonMaster && !selectedReason) {
        throw new Error("REASON_REQUIRED");
      }
      if (actionConfig.fixedReopenReasons && !selectedReopenReason) {
        throw new Error("REASON_REQUIRED");
      }
      if (actionConfig.needsAssignee && !selectedAssignee) {
        throw new Error("ASSIGNEE_REQUIRED");
      }
      if (actionConfig.documents === "required" && uploads.length === 0) {
        throw new Error("FILES_REQUIRED");
      }

      return updateIncidentAction({
        complaintDetails,
        action,
        comments: comments.trim(),
        documents: buildUploadedDocuments(uploads),
        assigneeUuid: actionConfig.needsAssignee ? selectedAssignee?.code : null,
        outOfScopeReason: action === "MARK_OUT_OF_SCOPE" ? selectedReason : null,
        rejectReason: action === "REJECT" ? selectedReason : null,
        sendBackReason: action === "SENDBACK" ? selectedReason : null,
        reopenReason: actionConfig.fixedReopenReasons ? selectedReopenReason : null,
        accessToken,
        user,
      });
    },
    onSuccess: async (response) => {
      if (!response?.IncidentWrappers) {
        const message =
          response?.Errors?.[0]?.message ??
          response?.message ??
          t("CS_COMMON_SOMETHING_WENT_WRONG");
        setError(message);
        return;
      }
      await onComplete();
    },
    onError: (mutationError: Error) => {
      const code = mutationError.message;
      const message =
        code === "COMMENT_REQUIRED"
          ? t("WF_COMMENT_REQUIRED")
          : code === "COMMENT_TOO_LONG"
            ? t("WF_COMMENT_MAX_LENGTH").replace("{MAX_COUNT}", String(MAX_COMMENT_LENGTH))
            : code === "FILES_REQUIRED"
              ? t("WF_QUOTATION_REQUIRED")
              : code === "REASON_REQUIRED"
                ? t("WF_REASON_REQUIRED")
                : code === "ASSIGNEE_REQUIRED"
                  ? t("WF_ASSIGNEE_REQUIRED")
                  : t("CS_COMMON_SOMETHING_WENT_WRONG");
      setError(message);
    },
  });

  const handleUpload = async (files: FileList) => {
    if (!accessToken) {
      return;
    }

    const filesToUpload = Array.from(files);
    if (uploads.length + filesToUpload.length > MAX_IMAGE_COUNT) {
      setError(
        t("WF_MAX_FILES_REACHED").replace("{MAX_COUNT}", String(MAX_IMAGE_COUNT)),
      );
      return;
    }

    if (requiresQuotation) {
      const quotationError = validateQuotationFiles(filesToUpload);
      if (quotationError?.code === "FORMAT") {
        setError(
          t("WF_QUOTATION_IMAGE_NOT_ALLOWED"),
        );
        return;
      }
      if (quotationError?.code === "SIZE") {
        setError(
          t("WF_QUOTATION_FILE_TOO_LARGE")
            .replace("{fileName}", quotationError.fileName ?? "")
            .replace("{MAX_SIZE}", String(MAX_QUOTATION_SIZE_MB)),
        );
        return;
      }
    } else {
      const documentError = validateActionDocumentFiles(filesToUpload);
      if (documentError?.code === "FORMAT") {
        setError(
          t("WF_DOCUMENT_FORMAT_NOT_ALLOWED"),
        );
        return;
      }
      if (documentError?.code === "SIZE") {
        setError(
          t("WF_DOCUMENT_FILE_TOO_LARGE")
            .replace("{fileName}", documentError.fileName ?? "")
            .replace("{MAX_SIZE}", String(MAX_ACTION_DOCUMENT_SIZE_MB)),
        );
        return;
      }
    }
    setError(null);
    setIsUploading(true);
    try {
      const uploaded: UploadedMediaEntry[] = [];
      for (const file of filesToUpload) {
        const result = await uploadIncidentFile(
          file,
          complaintDetails.tenantId,
          accessToken,
        );
        uploaded.push({
          file,
          fileStoreId: result.fileStoreId,
          kind: "image",
        });
      }
      setUploads((prev) => [...prev, ...uploaded]);
    } finally {
      setIsUploading(false);
    }
  };

  const handleRemoveUpload = (index: number) => {
    setUploads((prev) => prev.filter((_, entryIndex) => entryIndex !== index));
  };

  if (!actionConfig) {
    return null;
  }

  const showDocuments = actionConfig.documents !== "none";
  const requiresQuotation = isQuotationRequiredAction(action);
  const reasonLabel = getReasonLabel(t, action);

  const endUserName =
    complaintDetails.incident.reporter?.name ??
    t("CS_COMMON_END_USER");
  const outOfWarrantyHelperText = getOutOfWarrantyHelperText(t, action, endUserName);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/20 p-4 backdrop-blur-sm"
      onClick={onClose}
      onKeyDown={(event) => {
        if (event.target !== event.currentTarget) {
          return;
        }
        if (event.key === "Enter" || event.key === " " || event.key === "Escape") {
          onClose();
        }
      }}
      role="button"
      tabIndex={0}
      aria-label={t("CS_COMMON_CLOSE")}
    >
      <div
        className="max-h-[90dvh] w-full max-w-lg overflow-y-auto rounded-3xl border border-border bg-card px-6 py-5 shadow-lg"
        onClick={(event) => event.stopPropagation()}
        onKeyDown={(event) => {
          if (event.key === "Escape") {
            onClose();
          }
        }}
        role="dialog"
        aria-modal="true"
      >
        <h2 className="text-xl leading-[30px] font-semibold text-ink-950">
          {t(`CS_ACTION_${action}`)}
        </h2>

        <div className="mt-4 space-y-4">
          {actionConfig.needsAssignee ? (
            <FormSelectField
              label={t("WF_ASSIGNEE")}
              required
              value={selectedAssignee?.code ?? ""}
              options={assigneeOptions}
              onChange={(option) =>
                setSelectedAssignee(
                  assigneeOptions.find((entry) => entry.code === option?.code) ?? null,
                )
              }
            />
          ) : null}

          {actionConfig.reasonMaster ? (
            <FormSelectField
              label={reasonLabel}
              required
              value={selectedReason?.code ?? ""}
              options={reasonOptions.map((reason) => {
                const fallbackName = reason.code ?? reason.localizedCode ?? "";
                return {
                  code: reason.code ?? reason.localizedCode ?? "",
                  name: t(reason.localizedCode ?? reason.code ?? ""),
                };
              })}
              onChange={(option) =>
                setSelectedReason(
                  reasonOptions.find(
                    (reason) =>
                      reason.code === option?.code ||
                      reason.localizedCode === option?.code,
                  ) ?? null,
                )
              }
            />
          ) : null}

          {actionConfig.fixedReopenReasons ? (
            <FormSelectField
              label={t("WF_REOPEN_REASON")}
              required
              value={selectedReopenReason}
              options={REOPEN_REASON_OPTIONS.map((code) => ({
                code,
                name: t(code),
              }))}
              onChange={(option) => setSelectedReopenReason(option?.code ?? "")}
            />
          ) : null}

          <div className="space-y-1.5">
            <label className="text-sm font-medium text-ink-950">
              {t("WF_COMMON_COMMENTS")}
              {actionConfig.comment === "required" ? (
                <span className="text-destructive"> *</span>
              ) : null}
            </label>
            <textarea
              className="min-h-[100px] w-full rounded border border-ink-300 bg-card px-3 py-2 text-sm placeholder:text-ink-300"
              placeholder={t("WF_COMMENTS_PLACEHOLDER")}
              maxLength={MAX_COMMENT_LENGTH}
              value={comments}
              onChange={(event) => setComments(event.target.value)}
            />
            <p className="text-right text-xs text-muted-foreground">
              {comments.length}/{MAX_COMMENT_LENGTH}
            </p>
          </div>

          {showDocuments ? (
            <ActionDocumentsField
              requiresQuotation={requiresQuotation}
              documentsRequired={actionConfig.documents === "required"}
              uploads={uploads}
              isUploading={isUploading}
              maxFiles={MAX_IMAGE_COUNT}
              onUpload={handleUpload}
              onRemove={handleRemoveUpload}
              t={t}
            />
          ) : null}

          {error ? <p className="text-sm text-destructive">{error}</p> : null}

          {outOfWarrantyHelperText ? (
            <p className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              {outOfWarrantyHelperText}
            </p>
          ) : null}
        </div>

        <div className="mt-6 flex justify-center gap-3">
          <Button type="button" variant="outline" size="lg" onClick={onClose}>
            {t("TL_COMMON_CANCEL")}
          </Button>
          <Button
            type="button"
            size="lg"
            disabled={mutation.isPending || isUploading}
            onClick={() => {
              setError(null);
              mutation.mutate();
            }}
          >
            {mutation.isPending
              ? t("CS_COMMON_SUBMITTING")
              : t("CS_COMMON_SUBMIT")}
          </Button>
        </div>
      </div>
    </div>
  );
}
