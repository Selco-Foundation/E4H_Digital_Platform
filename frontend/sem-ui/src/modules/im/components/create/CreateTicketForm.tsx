import { Button } from "@/ui";
import {
  Camera,
  Factory,
  FileText,
  Info,
  Loader2,
  Send,
  Video,
} from "lucide-react";
import { useNavigate } from "@tanstack/react-router";
import { createPortal } from "react-dom";
import { useCreateIncidentForm } from "../../hooks/use-create-incident-form";
import { DuplicateTicketsDialog } from "./DuplicateTicketsDialog";
import { FormSectionCard } from "./FormSectionCard";
import { FormSelectField } from "./FormSelectField";
import { MediaUploadZone } from "./MediaUploadZone";
import { TicketSubmittedDialog } from "./TicketSubmittedDialog";

interface CreateTicketFormProps {
  readonly inboxPath: string;
}

export function CreateTicketForm({ inboxPath }: CreateTicketFormProps) {
  const navigate = useNavigate();
  const {
    t,
    translateOr,
    form,
    fieldErrors,
    endUserOptions,
    assetOptions,
    facilityById,
    assetById,
    complaintTypes,
    showEndUserDropdown,
    isFacilitiesLoading,
    isAssetsLoading,
    imageUploads,
    videoUploads,
    uploadFiles,
    removeUpload,
    isImageUploading,
    isVideoUploading,
    disableUpload,
    duplicateTickets,
    setDuplicateTickets,
    canSubmit,
    submitError,
    setSubmitError,
    createMutation,
    validate,
    handleEndUserChange,
    handleAssetChange,
    handleComplaintTypeChange,
    updateField,
    maxImageCount,
    maxImageSizeMb,
    maxVideoCount,
    maxVideoSizeMb,
    maxCommentLength,
    submittedResponse,
  } = useCreateIncidentForm(inboxPath);

  const submittedIncidentId =
    submittedResponse?.IncidentWrappers?.[0]?.incident?.incidentId;

  const handleSubmit = () => {
    setSubmitError(null);
    if (!validate()) {
      return;
    }
    if (!canSubmit) {
      return;
    }
    createMutation.mutate();
  };

  return (
    <>
      {createMutation.isPending
        ? createPortal(
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
              <Loader2 className="size-10 animate-spin text-primary" />
            </div>,
            document.body,
          )
        : null}

      {duplicateTickets.length > 0 ? (
        <DuplicateTicketsDialog
          tickets={duplicateTickets}
          onContinue={() => setDuplicateTickets([])}
          onCancel={() => void navigate({ to: inboxPath })}
        />
      ) : null}

      {submittedIncidentId ? (
        <TicketSubmittedDialog incidentId={submittedIncidentId} inboxPath={inboxPath} />
      ) : null}

      <form
        className="space-y-6"
        onSubmit={(event) => {
          event.preventDefault();
          handleSubmit();
        }}
      >
        <FormSectionCard
          icon={Factory}
          title={translateOr(t, "ASSET_DETAILS", "Asset Details")}
          description={translateOr(
            t,
            "ASSET_DETAILS_DESC",
            "Select the end user and asset for this ticket",
          )}
        >
          <div className="grid gap-4 md:grid-cols-2">
            {showEndUserDropdown ? (
              <FormSelectField
                label={translateOr(t, "INCIDENT_END_USER", "End User")}
                required
                value={form.endUser?.facilityId ?? ""}
                options={endUserOptions}
                disabled={isFacilitiesLoading}
                error={fieldErrors.endUser}
                onChange={(option) =>
                  handleEndUserChange(
                    option ? (facilityById.get(option.code) ?? null) : null,
                  )
                }
              />
            ) : form.endUser ? (
              <div className="min-w-0 space-y-1.5">
                <p className="text-sm font-medium text-foreground">
                  {translateOr(t, "INCIDENT_END_USER", "End User")}
                </p>
                <p className="rounded-md border border-input bg-muted/30 px-3 py-2 text-sm text-foreground">
                  {form.endUser.facilityPocName}
                </p>
              </div>
            ) : null}

            <FormSelectField
              label={translateOr(t, "INCIDENT_ASSET", "Asset")}
              required
              value={form.asset?.assetId ?? ""}
              options={assetOptions}
              disabled={!form.endUser || isAssetsLoading}
              error={fieldErrors.asset}
              onChange={(option) =>
                handleAssetChange(option ? (assetById.get(option.code) ?? null) : null)
              }
            />
          </div>
        </FormSectionCard>

        <FormSectionCard
          icon={FileText}
          title={translateOr(t, "TICKET_DETAILS", "Ticket Details")}
          description={translateOr(
            t,
            "TICKET_DETAILS_DESC",
            "Describe the problem so we can help faster",
          )}
        >
          <div className="grid gap-4 md:grid-cols-2">
            <FormSelectField
              label={translateOr(t, "TICKET_TYPE", "Issue Type")}
              required
              value={form.complaintType?.code ?? ""}
              options={complaintTypes}
              disabled={!form.asset}
              error={fieldErrors.complaintType}
              onChange={(option) => handleComplaintTypeChange(option)}
            />
          </div>
        </FormSectionCard>

        <FormSectionCard
          icon={Info}
          title={translateOr(t, "ADDITIONAL_DETAILS", "Additional Details")}
          description={translateOr(
            t,
            "ADDITIONAL_DETAILS_DESC",
            "Provide more information or media to help us resolve the issue",
          )}
        >
          <div className="space-y-6">
            <div className="space-y-1.5">
              <label htmlFor="incident-comments" className="text-sm font-medium text-foreground">
                {translateOr(t, "INCIDENT_COMMENTS", "Comments")}
              </label>
              <textarea
                id="incident-comments"
                className="min-h-[120px] w-full rounded-md border border-input bg-card px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                placeholder={translateOr(
                  t,
                  "INCIDENT_COMMENTS_PLACEHOLDER",
                  "Add any additional comments here...",
                )}
                maxLength={maxCommentLength}
                value={form.comments}
                onChange={(event) => updateField("comments", event.target.value)}
              />
              <div className="flex items-center justify-between gap-2">
                {fieldErrors.comments ? (
                  <p className="text-xs text-destructive">{fieldErrors.comments}</p>
                ) : (
                  <span />
                )}
                <p className="text-xs text-muted-foreground">
                  {form.comments.length}/{maxCommentLength}
                </p>
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <MediaUploadZone
                label={translateOr(t, "INCIDENT_UPLOAD_IMAGE", "Upload Images")}
                hint={translateOr(t, "INCIDENT_TAP_UPLOAD_IMAGES", "Tap to upload images")}
                helperText={translateOr(
                  t,
                  "INCIDENT_IMAGE_UPLOAD_HELPER",
                  `Up to ${maxImageCount} images, JPG/JPEG/PNG, ${maxImageSizeMb}MB max each`,
                )}
                error={fieldErrors.image}
                icon={Camera}
                accept=".png,.jpg,.jpeg,image/*"
                multiple
                disabled={disableUpload || imageUploads.length >= maxImageCount}
                uploading={isImageUploading}
                uploads={imageUploads}
                kind="image"
                onSelect={(files) => void uploadFiles(files, "image")}
                onRemove={(fileStoreId) => removeUpload("image", fileStoreId)}
              />
              <MediaUploadZone
                label={translateOr(t, "INCIDENT_UPLOAD_VIDEO", "Upload Videos")}
                hint={translateOr(t, "INCIDENT_TAP_UPLOAD_VIDEOS", "Tap to upload videos")}
                helperText={translateOr(
                  t,
                  "INCIDENT_VIDEO_UPLOAD_HELPER",
                  `Up to ${maxVideoCount} videos, MP4/MOV/AVI/WMV, ${maxVideoSizeMb}MB max each`,
                )}
                error={fieldErrors.video}
                icon={Video}
                accept=".mp4,.avi,.mov,.wmv,video/*"
                disabled={disableUpload || videoUploads.length >= maxVideoCount}
                uploading={isVideoUploading}
                uploads={videoUploads}
                kind="video"
                onSelect={(files) => void uploadFiles(files, "video")}
                onRemove={(fileStoreId) => removeUpload("video", fileStoreId)}
              />
            </div>
          </div>
        </FormSectionCard>

        {submitError ? (
          <p className="rounded-md border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
            {submitError}
          </p>
        ) : null}

        <div className="flex justify-end">
          <Button
            type="submit"
            size="lg"
            className="gap-2"
            disabled={!canSubmit || createMutation.isPending}
          >
            <Send className="size-4" />
            {translateOr(t, "FILE_INCIDENT", "Submit ticket")}
          </Button>
        </div>
      </form>
    </>
  );
}
