import { employeeHomePath, translateOr, useTranslate } from "@/shared";
import { Button } from "@/ui";
import { Link } from "@tanstack/react-router";
import { CheckCircle2 } from "lucide-react";
import { createPortal } from "react-dom";

interface TicketSubmittedDialogProps {
  incidentId: string;
  inboxPath: string;
}

export function TicketSubmittedDialog({
  incidentId,
  inboxPath,
}: TicketSubmittedDialogProps) {
  const { t } = useTranslate();
  const homePath = employeeHomePath();

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/20 p-4 backdrop-blur-sm">
      <div className="w-full max-w-md rounded-3xl border border-border bg-card p-8 text-center shadow-lg">
        <div className="mx-auto mb-4 flex size-16 items-center justify-center rounded-full bg-accent text-primary">
          <CheckCircle2 className="size-8" />
        </div>

        <h2 className="text-xl font-semibold text-foreground">
          {translateOr(t, "CS_COMMON_COMPLAINT_SUBMITTED", "Ticket Submitted")}
        </h2>

        <p className="mt-2 text-sm text-muted-foreground">
          {translateOr(t, "ES_COMMON_TRACK_COMPLAINT_TEXT", "Ticket No.")}{" "}
          <span className="font-medium text-foreground">{incidentId}</span>
        </p>

        <p className="mt-3 text-sm text-muted-foreground">
          {translateOr(
            t,
            "ES_IM_TICKET_SUBMITTED_DESC",
            "Your ticket has been registered. You can track the ticket status by logging into this application again",
          )}
        </p>

        <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-center">
          <Button asChild variant="outline" size="lg">
            <Link to={inboxPath}>
              {translateOr(t, "ES_IM_VIEW_INBOX", "View inbox")}
            </Link>
          </Button>
          <Button asChild size="lg">
            <Link to={homePath}>
              {translateOr(t, "CORE_COMMON_GO_TO_HOME", "Go to home")}
            </Link>
          </Button>
        </div>
      </div>
    </div>,
    document.body,
  );
}
