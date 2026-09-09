import type { InboxStatusMapEntry } from "../../types/inbox";
import { ORDERED_INBOX_STATUSES } from "../../constants/inbox-statuses";
import { useTranslate } from "@/shared";
import { Label } from "@/ui";
import { useMemo } from "react";

interface InboxStatusProps {
  statusMap?: InboxStatusMapEntry[];
  selectedStatuses: Array<{ code: string }>;
  onAssignmentChange: (
    checked: boolean,
    option: { code: string; statuses: readonly string[] | string[] },
  ) => void;
}

export function InboxStatus({
  statusMap,
  selectedStatuses,
  onAssignmentChange,
}: InboxStatusProps) {
  const { t } = useTranslate();

  const statusCounts = useMemo(() => {
    const counts: Record<string, number> = {};
    for (const entry of statusMap ?? []) {
      if (entry.statusid) {
        counts[entry.statusid] = entry.count;
      }
    }
    return counts;
  }, [statusMap]);

  const sortedComplaints = useMemo(() => {
    const countedStatusCodes: string[] = [];
    const sorted: Array<{
      code: string;
      statuses: readonly string[] | string[];
      count: number;
    }> = ORDERED_INBOX_STATUSES.map((statusObject) => {
      const count = statusObject.statuses.reduce(
        (total, status) => total + (statusCounts[status] ?? 0),
        0,
      );
      countedStatusCodes.push(...statusObject.statuses);
      return { ...statusObject, count };
    });

    for (const [code, count] of Object.entries(statusCounts)) {
      if (!countedStatusCodes.includes(code)) {
        sorted.push({
          code,
          statuses: [code] as string[],
          count,
        });
      }
    }

    return sorted;
  }, [statusCounts]);

  const isChecked = (option: { statuses: readonly string[] | string[] }) => {
    const existingStatusCodes = selectedStatuses.map((status) => status.code);
    return option.statuses.every((status) => existingStatusCodes.includes(status));
  };

  return (
    <div className="space-y-2">
      <Label>{t("ES_IM_FILTER_STATUS")}</Label>
      <div className="space-y-2">
        {sortedComplaints.map((option) => (
          <label key={option.code} className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={isChecked(option)}
              onChange={(event) => onAssignmentChange(event.target.checked, option)}
            />
            <span>
              {t(`CS_COMMON_${option.code}`)}
              {option.count ? ` (${option.count})` : ""}
            </span>
          </label>
        ))}
      </div>
    </div>
  );
}
