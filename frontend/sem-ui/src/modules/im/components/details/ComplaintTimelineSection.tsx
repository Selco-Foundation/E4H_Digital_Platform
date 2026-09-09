import { translateOr, useTranslate } from "@/shared";
import { cn } from "@/ui";
import { History } from "lucide-react";
import type {
  ComplaintDetailsData,
  WorkflowDetailsData,
  WorkflowTimelineCheckpoint,
} from "../../types/incident-details";
import { isInitialComplaintAction } from "../../utils/complaint-details";
import { FormSectionCard } from "../create/FormSectionCard";
import { ComplaintActionBar } from "./ComplaintActionBar";
import { ComplaintMediaList } from "./ComplaintMediaList";

interface ComplaintTimelineSectionProps {
  timeline: WorkflowTimelineCheckpoint[];
  complaintDetails: ComplaintDetailsData;
  workflowDetails: WorkflowDetailsData;
  onActionComplete: () => Promise<void>;
}

function TimelineCaption({
  checkpoint,
  complaintDetails,
}: {
  checkpoint: WorkflowTimelineCheckpoint;
  complaintDetails: ComplaintDetailsData;
}) {
  const { t } = useTranslate();
  const additional = complaintDetails.incident.additionalDetail;

  const outOfScopeReasons = [...(additional?.outOfScopeReason ?? [])].reverse();
  const rejectReasons = [...(additional?.rejectReason ?? [])].reverse();
  const reopenReasons = [...(additional?.reopenreason ?? [])].reverse();

  const action = checkpoint.performedAction;
  const isCreateCheckpoint = isInitialComplaintAction(action);
  let reasonText: string | null = null;
  let reasonLabel: string | null = null;

  if (action === "MARK_OUT_OF_SCOPE") {
    const outOfScopeCode = String(outOfScopeReasons.shift() ?? "");
    reasonText = translateOr(t, outOfScopeCode, outOfScopeCode);
    reasonLabel = translateOr(t, "WF_OUT_OF_SCOPE_REASON", "WF_OUT_OF_SCOPE_REASON");
  } else if (action === "REJECT") {
    const rejectCode = String(rejectReasons.shift() ?? "");
    reasonText = translateOr(t, rejectCode, rejectCode);
    reasonLabel = translateOr(t, "WF_REJECT_REASON", "WF_REJECT_REASON");
  } else if (action === "REOPEN" || action === "REOPEN_RMS") {
    const reopenCode = String(reopenReasons.shift() ?? "");
    reasonText = translateOr(t, reopenCode, reopenCode);
    reasonLabel = translateOr(t, "WF_REOPEN_REASON", "WF_REOPEN_REASON");
  }
  // SENDBACK's reason isn't carried on the workflow process-history response (only
  // action/state/comment/assigner are) — its `wfComment` entries (rendered below)
  // are the only reason text available for this checkpoint.

  return (
    <div className="mt-3 space-y-3 text-xs text-muted-foreground">
      {checkpoint.auditDetails?.lastModified ? (
        <p>{checkpoint.auditDetails.lastModified}</p>
      ) : null}
      {checkpoint.assigner?.name ? <p>{checkpoint.assigner.name}</p> : null}
      {checkpoint.assigner?.mobileNumber ? (
        <p>{checkpoint.assigner.mobileNumber}</p>
      ) : null}

      {reasonText ? (
        <div>
          <p className="font-medium text-foreground">{reasonLabel}</p>
          <p>{reasonText}</p>
        </div>
      ) : null}

      {checkpoint.wfComment?.map((comment, index) => (
        <div key={`${comment}-${index}`}>
          <p className="font-medium text-foreground">
            {translateOr(t, "WF_COMMON_COMMENTS", "Comments")}
          </p>
          <p className="break-words">{comment}</p>
        </div>
      ))}

      {!isCreateCheckpoint &&
      (checkpoint.thumbnailsToShow?.fullImage?.length ||
        checkpoint.thumbnailsToShow?.videos?.length) ? (
        <div className="space-y-2">
          <p className="font-medium text-foreground">
            {translateOr(t, "CS_COMMON_ATTACHMENTS", "Attachments")}
          </p>
          <ComplaintMediaList
            images={checkpoint.thumbnailsToShow?.fullImage ?? []}
            videos={checkpoint.thumbnailsToShow?.videos ?? []}
            imageGridClassName="grid grid-cols-3 gap-2"
          />
        </div>
      ) : null}
    </div>
  );
}

export function ComplaintTimelineSection({
  timeline,
  complaintDetails,
  workflowDetails,
  onActionComplete,
}: ComplaintTimelineSectionProps) {
  const { t } = useTranslate();

  if (!timeline.length) {
    return null;
  }

  return (
    <FormSectionCard
      icon={History}
      title={translateOr(t, "CS_COMPLAINT_DETAILS_HISTORY", "Timeline")}
      titleClassName="text-base font-semibold text-ink-950"
      divider
    >
      <ol className="space-y-0">
        {timeline.map((checkpoint, index) => {
          // `timeline[0]` is the most recent process instance (see
          // fetchWorkflowDetails's `currentInstance = processInstances[0]`), so the
          // latest action is the FIRST entry here, not the last one rendered.
          const isLatest = index === 0;
          const isLastRendered = index === timeline.length - 1;
          // DIGIT-UI's own timeline labels key off the checkpoint's resulting
          // *status*, not the performed action (`t("CS_COMMON_" + checkpoint.status)`
          // in ComplaintDetails.js) — the same CS_COMMON_<code> convention already
          // confirmed for the inbox's Issue Status filter.
          const status = checkpoint.status ?? checkpoint.performedAction ?? "UNKNOWN";
          const actionKey = `CS_COMMON_${status}`;

          return (
            <li key={`${checkpoint.status}-${checkpoint.performedAction}-${index}`} className="relative flex gap-4 pb-8">
              <div className="flex flex-col items-center">
                <div
                  className={cn(
                    "z-10 flex size-3 rounded-full",
                    isLatest ? "bg-success-foreground" : "bg-muted-foreground/40",
                  )}
                />
                {!isLastRendered ? (
                  <div className="mt-1 w-px flex-1 bg-border" />
                ) : null}
              </div>
              <div className="min-w-0 flex-1 pt-[-2px]">
                <p
                  className={cn(
                    "text-sm font-semibold",
                    isLatest ? "text-success-foreground" : "text-ink-950",
                  )}
                >
                  {translateOr(t, actionKey, status)}
                </p>
                <TimelineCaption
                  checkpoint={checkpoint}
                  complaintDetails={complaintDetails}
                />
              </div>
            </li>
          );
        })}
      </ol>

      <ComplaintActionBar
        complaintDetails={complaintDetails}
        workflowDetails={workflowDetails}
        onActionComplete={onActionComplete}
      />
    </FormSectionCard>
  );
}
