import { contextPath, useTranslate } from "@/shared";
import { Button } from "@/ui";
import { Link } from "@tanstack/react-router";
import { createPortal } from "react-dom";

interface DuplicateTicket {
  ticketId: string;
  ticketTenantId: string;
}

interface DuplicateTicketsDialogProps {
  tickets: DuplicateTicket[];
  onContinue: () => void;
  onCancel: () => void;
}

export function DuplicateTicketsDialog({
  tickets,
  onContinue,
  onCancel,
}: DuplicateTicketsDialogProps) {
  const { t } = useTranslate();
  const basePath = `/${contextPath()}/employee/im`;

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-md rounded-lg bg-card p-6 shadow-lg">
        <h2 className="text-center text-lg font-semibold">
          {t("IM_ALERT_POTENTIAL_DUPLICATES")}
        </h2>
        <p className="mt-3 text-center text-sm text-muted-foreground">
          {t("IM_ALERT_POTENTIAL_DUPLICATES_DESC")}
        </p>
        <p className="mt-2 max-h-40 overflow-auto text-center text-sm">
          {t("IM_ALERT_POTENTIAL_DUPLICATES_EXISTING")}:{" "}
          {tickets.map((ticket, index) => (
            <span key={ticket.ticketId}>
              <Link
                to={`${basePath}/complaint/details/${ticket.ticketId}/${ticket.ticketTenantId}`}
                className="text-primary hover:underline"
                target="_blank"
              >
                {ticket.ticketId}
              </Link>
              {index < tickets.length - 1 ? ", " : ""}
            </span>
          ))}
        </p>
        <p className="mt-3 text-center text-sm text-muted-foreground">
          {t("IM_ALERT_POTENTIAL_DUPLICATES_ACTION_DESC")}
        </p>
        <div className="mt-6 flex justify-center gap-3">
          <Button type="button" variant="outline" size="lg" onClick={onContinue}>
            {t("TL_COMMON_YES")}
          </Button>
          <Button type="button" size="lg" onClick={onCancel}>
            {t("TL_COMMON_NO")}
          </Button>
        </div>
      </div>
    </div>,
    document.body,
  );
}
