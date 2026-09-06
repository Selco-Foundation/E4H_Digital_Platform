import { translateOr, useTranslate } from "@/shared";
import { ClipboardList } from "lucide-react";
import type { ComplaintDetailsData } from "../../types/incident-details";
import { translateDetailValue } from "../../utils/complaint-details";
import { FormSectionCard } from "../create/FormSectionCard";

interface ComplaintSummarySectionProps {
  complaintDetails: ComplaintDetailsData;
}

export function ComplaintSummarySection({
  complaintDetails,
}: ComplaintSummarySectionProps) {
  const { t } = useTranslate();

  return (
    <FormSectionCard
      icon={ClipboardList}
      title={translateOr(t, "CS_HEADER_TICKET_DETAILS", "Ticket Details")}
      titleClassName="text-base font-semibold text-ink-950"
      divider
    >
      <dl className="grid gap-4 sm:grid-cols-2">
        {complaintDetails.rows.map((row) => (
          <div key={row.labelKey} className="min-w-0 space-y-1">
            <dt className="text-sm font-normal text-ink-600">
              {translateOr(t, row.labelKey, row.labelKey)}
            </dt>
            <dd className="text-sm font-medium break-words text-ink-950">
              {translateDetailValue(row.value, t)}
            </dd>
          </div>
        ))}
      </dl>
    </FormSectionCard>
  );
}
