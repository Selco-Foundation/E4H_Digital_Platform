import { Button } from "@/ui";
import {
  Camera,
  FileText,
  FileWarning,
  Info,
  Loader2,
  MapPin,
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
    form,
    fieldErrors,
    districtOptions,
    blockOptions,
    facilityOptions,
    ticketTypeMenu,
    ticketSubTypeMenu,
    systemFunctionalMenu,
    isBoundaryLoading,
    imageUploads,
    videoUploads,
    firUploads,
    uploadFiles,
    removeUpload,
    isImageUploading,
    isVideoUploading,
    isFirUploading,
    isTheftIssue,
    duplicateTickets,
    setDuplicateTickets,
    canSubmit,
    submitError,
    setSubmitError,
    createMutation,
    validate,
    handleDistrictChange,
    handleBlockChange,
    handleFacilityChange,
    handleTicketTypeChange,
    handleTicketSubTypeChange,
    handleSystemFunctionalChange,
    updateField,
    maxImageCount,
    maxImageSizeMb,
    maxVideoCount,
    maxVideoSizeMb,
    maxFirCount,
    maxFirSizeMb,
    maxCommentLength,
    submittedResponse,
  } = useCreateIncidentForm(inboxPath);

  const submittedIncidentId =
    submittedResponse?.IncidentWrappers?.[0]?.incident?.incidentId;
  const disableUpload = !form.ticketSubType;

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
          icon={MapPin}
          title={t("TICKET_LOCATION")}
          description={t("TICKET_LOCATION_DESC")}
        >
          <div className="grid gap-4 md:grid-cols-3">
            <FormSelectField
              label={t("INCIDENT_DISTRICT")}
              required
              value={form.district?.code ?? ""}
              options={districtOptions}
              disabled={isBoundaryLoading}
              error={fieldErrors.district}
              onChange={(option) =>
                handleDistrictChange(
                  option ? (districtOptions.find((d) => d.code === option.code) ?? null) : null,
                )
              }
            />
            <FormSelectField
              label={t("INCIDENT_BLOCK")}
              required
              value={form.block?.code ?? ""}
              options={blockOptions}
              disabled={!form.district}
              error={fieldErrors.block}
              onChange={(option) =>
                handleBlockChange(
                  option ? (blockOptions.find((b) => b.code === option.code) ?? null) : null,
                )
              }
            />
            <FormSelectField
              label={t("HEALTH_CARE_CENTRE")}
              required
              value={form.facility?.code ?? ""}
              options={facilityOptions}
              disabled={!form.block}
              error={fieldErrors.facility}
              onChange={(option) =>
                handleFacilityChange(
                  option ? (facilityOptions.find((f) => f.code === option.code) ?? null) : null,
                )
              }
            />
          </div>
        </FormSectionCard>

        <FormSectionCard
          icon={FileText}
          title={t("TICKET_DETAILS")}
          description={t("TICKET_DETAILS_DESC")}
        >
          <div className="grid gap-4 md:grid-cols-3">
            <FormSelectField
              label={t("TICKET_TYPE")}
              required
              value={form.ticketType?.code ?? ""}
              options={ticketTypeMenu}
              error={fieldErrors.ticketType}
              onChange={(option) => handleTicketTypeChange(option)}
            />
            <FormSelectField
              label={t("TICKET_SUBTYPE")}
              required
              value={form.ticketSubType?.code ?? ""}
              options={ticketSubTypeMenu}
              disabled={!form.ticketType}
              error={fieldErrors.ticketSubType}
              onChange={(option) => handleTicketSubTypeChange(option)}
            />
            <FormSelectField
              label={t("SYSTEM_FUNCTIONAL")}
              required
              value={form.systemFunctional?.code ?? ""}
              options={systemFunctionalMenu}
              error={fieldErrors.systemFunctional}
              onChange={(option) => handleSystemFunctionalChange(option)}
            />
          </div>
        </FormSectionCard>

        <FormSectionCard
          icon={Info}
          title={t("ADDITIONAL_DETAILS")}
          description={t("ADDITIONAL_DETAILS_DESC")}
        >
          <div className="space-y-6">
            <div className="space-y-1.5">
              <label htmlFor="incident-comments" className="text-sm font-medium text-foreground">
                {t("INCIDENT_COMMENTS")}
              </label>
              <textarea
                id="incident-comments"
                className="min-h-[120px] w-full rounded-md border border-input bg-card px-3 py-2 text-sm text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                placeholder={t("INCIDENT_COMMENTS_PLACEHOLDER")}
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
                label={t("INCIDENT_UPLOAD_IMAGE")}
                hint={t("INCIDENT_TAP_UPLOAD_IMAGES")}
                helperText={t("CS_MAXIMUM_IMAGES")}
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
                label={t("INCIDENT_UPLOAD_VIDEO")}
                hint={t("INCIDENT_TAP_UPLOAD_VIDEOS")}
                helperText={t("CS_MAXIMUM_VIDEOS")}
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

            {isTheftIssue ? (
              <MediaUploadZone
                label={t("INCIDENT_UPLOAD_FIR_POLICE_LETTER")}
                hint={t("INCIDENT_TAP_UPLOAD_FIR")}
                helperText={t("INCIDENT_PLEASE_UPLOAD_FIR_POLICE_LETTER")}
                error={fieldErrors.fir}
                icon={FileWarning}
                accept=".pdf,.jpg,.jpeg,.png,image/*,application/pdf"
                multiple
                disabled={disableUpload || firUploads.length >= maxFirCount}
                uploading={isFirUploading}
                uploads={firUploads}
                kind="fir"
                onSelect={(files) => void uploadFiles(files, "fir")}
                onRemove={(fileStoreId) => removeUpload("fir", fileStoreId)}
              />
            ) : null}
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
            {t("FILE_INCIDENT")}
          </Button>
        </div>
      </form>
    </>
  );
}
