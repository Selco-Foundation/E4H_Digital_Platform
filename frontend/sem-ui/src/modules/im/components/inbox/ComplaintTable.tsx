import { contextPath, translateOr, useAuthStore, useTranslate } from "@/shared";
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
    ? translateOr(t, "WF_INBOX_HEADER_DAYS_REMAINING", "Days Remaining")
    : translateOr(t, "WF_INBOX_HEADER_SLA_DAYS_REMAINING", "SLA Days Remaining");
  const basePath = `/${contextPath()}/employee/im`;
  const overdueLabel = translateOr(t, "SLA_OVERDUE", "Overdue");

  const columns = [
    { key: "ticket", label: translateOr(t, "CS_COMMON_TICKET_NO", "Ticket No.") },
    { key: "endUser", label: translateOr(t, "INCIDENT_END_USER", "End User") },
    { key: "asset", label: translateOr(t, "INCIDENT_ASSET", "Asset") },
    { key: "type", label: translateOr(t, "CS_TICKET_TYPE", "Issue Type") },
    { key: "status", label: translateOr(t, "CS_TICKET_DETAILS_CURRENT_STATUS", "Current Status") },
    { key: "owner", label: translateOr(t, "WF_INBOX_HEADER_CURRENT_OWNER", "Current Owner") },
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
                    <div>
                      <Link
                        to={detailsPath}
                        className="font-semibold text-foreground hover:text-primary hover:underline"
                        onClick={(event) => event.stopPropagation()}
                      >
                        {row.incidentId}
                      </Link>
                      {row.potentialDuplicate ? (
                        <p className="mt-1 text-xs font-medium text-destructive">
                          {translateOr(t, "CS_INFO_POTENTIAL_DUPLICATE", "Potential duplicate")}
                        </p>
                      ) : null}
                    </div>
                  </td>
                  <td className="px-5 py-4 text-foreground">{row.endUser}</td>
                  <td className="px-5 py-4 text-foreground">
                    {translateDetailValue(row.assetLabel, t)}
                  </td>
                  <td className="px-5 py-4 text-foreground">
                    {translateOr(
                      t,
                      `SERVICEDEFS.${row.incidentType.toUpperCase()}`,
                      row.incidentType,
                    )}
                  </td>
                  <td className="px-5 py-4 text-foreground">
                    <span
                      className="block max-w-[180px] truncate"
                      title={translateOr(t, `CS_COMMON_${row.status}`, row.status)}
                    >
                      {translateOr(t, `CS_COMMON_${row.status}`, row.status)}
                    </span>
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
