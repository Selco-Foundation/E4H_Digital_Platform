import { translateOr, useAuthStore, useTranslate } from "@/shared";
import { Button, SplitButton } from "@/ui";
import { useEffect, useMemo, useRef, useState } from "react";
import { SUPPORTED_WORKFLOW_ACTION_SET } from "../../constants/workflow-actions";
import type { ComplaintDetailsData, WorkflowDetailsData } from "../../types/incident-details";
import { isClosedTicket } from "../../utils/complaint-details";
import { isEndUser } from "../../utils/access";
import { ComplaintActionDialog } from "./ComplaintActionDialog";

const MAX_END_USER_REOPEN_COUNT = 2;

interface ComplaintActionBarProps {
  complaintDetails: ComplaintDetailsData;
  workflowDetails: WorkflowDetailsData;
  onActionComplete: () => Promise<void>;
}

export function ComplaintActionBar({
  complaintDetails,
  workflowDetails,
  onActionComplete,
}: ComplaintActionBarProps) {
  const { t } = useTranslate();
  const user = useAuthStore((state) => state.user);
  const [menuOpen, setMenuOpen] = useState(false);
  const [selectedAction, setSelectedAction] = useState<string | null>(null);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!menuOpen) {
      return;
    }

    const handleClickOutside = (event: MouseEvent) => {
      if (!menuRef.current?.contains(event.target as Node)) {
        setMenuOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [menuOpen]);

  const reopenCount = useMemo(
    () =>
      workflowDetails.timeline.filter(
        (checkpoint) => checkpoint.performedAction === "REOPEN",
      ).length,
    [workflowDetails.timeline],
  );

  const availableActions = useMemo(() => {
    const supported = (workflowDetails.nextActions ?? []).filter((entry) =>
      SUPPORTED_WORKFLOW_ACTION_SET.has(entry.action),
    );

    const reopenLimitReached =
      isEndUser(user?.roles) && reopenCount >= MAX_END_USER_REOPEN_COUNT;

    return reopenLimitReached
      ? supported.filter((entry) => entry.action !== "REOPEN")
      : supported;
  }, [workflowDetails.nextActions, user?.roles, reopenCount]);

  const showActions =
    !isClosedTicket(complaintDetails.incident.applicationStatus) &&
    availableActions.length > 0;

  if (!showActions) {
    return null;
  }

  const singleAction = availableActions.length === 1 ? availableActions[0] : null;

  return (
    <>
      <div className="mt-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-end">
        {singleAction ? (
          <Button
            type="button"
            size="lg"
            className="gap-2"
            onClick={() => setSelectedAction(singleAction.action)}
          >
            {translateOr(t, `CS_ACTION_${singleAction.action}`, singleAction.action)}
          </Button>
        ) : (
          <>
            <div className="mr-auto">
              <p className="text-sm font-medium text-foreground">{translateOr(t, "WF_TAKE_ACTION", "Take action")}</p>
              <p className="text-xs text-muted-foreground">
                {translateOr(t, "WF_TAKE_ACTION_DESC", "Choose an action to update this ticket")}
              </p>
            </div>
            <div className="relative w-full sm:w-auto" ref={menuRef}>
              <SplitButton
                label={translateOr(t, "WF_TAKE_ACTION", "Take action")}
                size="lg"
                className="flex w-full sm:inline-flex sm:w-auto"
                onLabelClick={() => setMenuOpen((open) => !open)}
                triggerAriaExpanded={menuOpen}
              />
              {menuOpen ? (
                <div className="absolute right-0 bottom-full z-20 mb-2 min-w-[220px] rounded-lg border border-border bg-card p-1 shadow-lg">
                  {availableActions.map((action) => (
                    <button
                      key={action.action}
                      type="button"
                      className="w-full cursor-pointer rounded-md px-3 py-2 text-left text-sm hover:bg-accent"
                      onClick={() => {
                        setSelectedAction(action.action);
                        setMenuOpen(false);
                      }}
                    >
                      {translateOr(t, `CS_ACTION_${action.action}`, action.action)}
                    </button>
                  ))}
                </div>
              ) : null}
            </div>
          </>
        )}
      </div>

      {selectedAction ? (
        <ComplaintActionDialog
          action={selectedAction}
          complaintDetails={complaintDetails}
          onClose={() => setSelectedAction(null)}
          onComplete={async () => {
            await onActionComplete();
            setSelectedAction(null);
          }}
        />
      ) : null}
    </>
  );
}
