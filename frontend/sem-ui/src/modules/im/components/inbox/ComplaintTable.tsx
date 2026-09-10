import { contextPath, useAuthStore, useTranslate } from "@/shared";
import { cn } from "@/ui";
import { Link, useNavigate } from "@tanstack/react-router";
import { SLA_OVERDUE_MARKER } from "../../constants/workflow";
import type { InboxRow } from "../../types/inbox";
import { isEndUser } from "../../utils/access";
import { translateDetailValue } from "../../utils/complaint-details";

interface ComplaintTableProps {
  readonly data: InboxRow[];
}

function SlaBadge({ value, overdueLabel }: { value: string; overdueLabel: string }) {
  if (value === "-") {
    return <span className="livelihood-sla-badge-muted">-</span>;
  }
  if (value === SLA_OVERDUE_MARKER) {
    return <span className="livelihood-sla-badge-muted">{overdueLabel}</span>;
  }
  return <span className="livelihood-sla-badge">{value}</span>;
}

export function ComplaintTable({ data }: ComplaintTableProps) {
  const { t } = useTranslate();
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const slaColumnLabel = isEndUser(user?.roles)
    ? t("WF_INBOX_HEADER_DAYS_REMAINING")
    : t("WF_INBOX_HEADER_SLA_DAYS_REMAINING");
  const basePath = `/${contextPath()}/employee/im`;
  const overdueLabel = t("SLA_OVERDUE");

  const columns = [
    { key: "ticket", label: t("CS_COMMON_TICKET_NO") },
    { key: "type", label: t("CS_TICKET_TYPE") },
    { key: "subType", label: t("CS_TICKET_SUB_TYPE") },
    { key: "status", label: t("CS_TICKET_DETAILS_CURRENT_STATUS") },
    { key: "facility", label: t("INCIDENT_FACILITY") },
    { key: "owner", label: t("WF_INBOX_HEADER_CURRENT_OWNER") },
    { key: "sla", label: slaColumnLabel },
  ] as const;

  return (
    <div>
      <div className="overflow-x-auto">
        <table className="w-full min-w-[960px] border-collapse text-sm">
          <thead>
            <tr className="border-b border-border">
              {columns.map((column) => (
                <th
                  key={column.key}
                  className="px-5 py-3 text-left text-sm font-semibold text-ink-950"
                >
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.map((row, index) => {
              const detailsPath = `${basePath}/complaint/details/${row.incidentId}/${row.tenantId}`;

              return (
                <tr
                  key={`${row.incidentId}-${row.tenantId}`}
                  className={cn(
                    "cursor-pointer border-b border-border/70 hover:bg-muted/40",
                    index % 2 === 1 && "bg-accent",
                  )}
                  onClick={() => {
                    navigate({ to: detailsPath }).catch(() => {});
                  }}
                >
                  <td className="px-5 py-4">
                    <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
                      <Link
                        to={detailsPath}
                        className="font-semibold text-foreground hover:text-primary hover:underline"
                        onClick={(event) => event.stopPropagation()}
                      >
                        {row.incidentId}
                      </Link>
                      {row.potentialDuplicate ? (
                        <p className="inline-block w-fit rounded-md border border-primary bg-primary/10 px-1.5 py-0.5 text-xs font-bold text-primary">
                          {t("CS_INFO_POTENTIAL_DUPLICATE")}
                        </p>
                      ) : null}
                    </div>
                  </td>
                  <td className="px-5 py-4 text-foreground">
                    {t(`SERVICEDEFS.${row.incidentType.toUpperCase()}`)}
                  </td>
                  <td className="px-5 py-4 text-foreground">
                    {row.incidentSubType === "-"
                      ? "-"
                      : t(`SERVICEDEFS.${row.incidentSubType.toUpperCase()}`)}
                  </td>
                  <td className="px-5 py-4 text-foreground">
                    <span
                      className="block max-w-[180px] truncate"
                      title={t(`CS_COMMON_${row.status}`)}
                    >
                      {t(`CS_COMMON_${row.status}`)}
                    </span>
                  </td>
                  <td className="px-5 py-4 text-foreground">
                    {translateDetailValue(row.assetLabel, t)}
                  </td>
                  <td className="px-5 py-4 text-foreground">{row.taskOwner}</td>
                  <td className="px-5 py-4">
                    <SlaBadge value={row.sla} overdueLabel={overdueLabel} />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
