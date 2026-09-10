import { contextPath, useAuthStore, useTranslate } from "@/shared";
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
    ? t("WF_INBOX_HEADER_DAYS_REMAINING")
    : t("WF_INBOX_HEADER_SLA_DAYS_REMAINING");
  const overdueLabel = t("SLA_OVERDUE");

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
              <p className="inline-block w-fit rounded-md border border-primary bg-primary/10 px-1.5 py-0.5 text-xs font-bold text-primary">
                {t("CS_INFO_POTENTIAL_DUPLICATE")}
              </p>
            ) : null}
            <DetailRow label={t("INCIDENT_END_USER")} value={row.endUser} />
            <DetailRow
              label={t("INCIDENT_ASSET")}
              value={translateDetailValue(row.assetLabel, t)}
            />
            <DetailRow
              label={t("CS_TICKET_TYPE")}
              value={t(`SERVICEDEFS.${row.incidentType.toUpperCase()}`)}
            />
            <DetailRow
              label={t("CS_TICKET_DETAILS_CURRENT_STATUS")}
              value={t(`CS_COMMON_${row.status}`)}
            />
            <DetailRow
              label={t("WF_INBOX_HEADER_CURRENT_OWNER")}
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
