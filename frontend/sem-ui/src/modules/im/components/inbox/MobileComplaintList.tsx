import { contextPath, translateOr, useAuthStore, useTranslate } from "@/shared";
import { Card } from "@/ui";
import { useNavigate } from "@tanstack/react-router";
import { Clock } from "lucide-react";
import { SLA_OVERDUE_MARKER } from "../../constants/workflow";
import type { InboxRow } from "../../types/inbox";
import { isEndUser } from "../../utils/access";
import { translateDetailValue } from "../../utils/complaint-details";

interface MobileComplaintListProps {
  readonly data: InboxRow[];
}

function DetailRow({ label, value }: { readonly label: string; readonly value: string }) {
  return (
    <p className="text-sm text-ink-600">
      {label}: <span className="font-semibold text-ink-950">{value}</span>
    </p>
  );
}

export function MobileComplaintList({ data }: MobileComplaintListProps) {
  const { t } = useTranslate();
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const basePath = `/${contextPath()}/employee/im`;
  const slaLabel = isEndUser(user?.roles)
    ? translateOr(t, "WF_INBOX_HEADER_DAYS_REMAINING", "Days Remaining")
    : translateOr(t, "WF_INBOX_HEADER_SLA_DAYS_REMAINING", "SLA Days Remaining");
  const overdueLabel = translateOr(t, "SLA_OVERDUE", "Overdue");

  return (
    <div className="space-y-3">
      {data.map((row) => {
        const detailsPath = `${basePath}/complaint/details/${row.incidentId}/${row.tenantId}`;

        return (
          <Card
            key={`${row.incidentId}-${row.tenantId}`}
            className="livelihood-card cursor-pointer gap-1.5 border-border p-4 shadow-none"
            onClick={() => {
              navigate({ to: detailsPath }).catch(() => {});
            }}
          >
            <p className="text-base font-bold text-ink-950">{row.incidentId}</p>
            {row.potentialDuplicate ? (
              <p className="text-xs font-medium text-destructive">
                {translateOr(t, "CS_INFO_POTENTIAL_DUPLICATE", "Potential duplicate")}
              </p>
            ) : null}
            <DetailRow label={translateOr(t, "INCIDENT_END_USER", "End User")} value={row.endUser} />
            <DetailRow
              label={translateOr(t, "INCIDENT_ASSET", "Asset")}
              value={translateDetailValue(row.assetLabel, t)}
            />
            <DetailRow
              label={translateOr(t, "CS_TICKET_TYPE", "Issue Type")}
              value={translateOr(
                t,
                `SERVICEDEFS.${row.incidentType.toUpperCase()}`,
                row.incidentType,
              )}
            />
            <DetailRow
              label={translateOr(t, "CS_TICKET_DETAILS_CURRENT_STATUS", "Current Status")}
              value={translateOr(t, `CS_COMMON_${row.status}`, row.status)}
            />
            <DetailRow
              label={translateOr(t, "WF_INBOX_HEADER_CURRENT_OWNER", "Current Owner")}
              value={row.taskOwner}
            />
            <p className="flex items-center gap-1.5 text-sm text-ink-600">
              <Clock className="size-4 shrink-0" />
              {slaLabel}:{" "}
              <span className="font-semibold text-ink-950">
                {row.sla === SLA_OVERDUE_MARKER ? overdueLabel : row.sla}
              </span>
            </p>
          </Card>
        );
      })}
    </div>
  );
}
