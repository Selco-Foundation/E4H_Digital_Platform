import { useTranslate } from "@/shared";
import { ClipboardList } from "lucide-react";
import type { ComplaintDetailsData } from "../../types/incident-details";
import { translateDetailValue } from "../../utils/complaint-details";
import { FormSectionCard } from "../create/FormSectionCard";
import { ComplaintMediaList, type ComplaintVideoEntry } from "./ComplaintMediaList";

interface ComplaintSummarySectionProps {
  complaintDetails: ComplaintDetailsData;
  images?: string[];
  videos?: ComplaintVideoEntry[];
}

export function ComplaintSummarySection({
  complaintDetails,
  images = [],
  videos = [],
}: ComplaintSummarySectionProps) {
  const { t } = useTranslate();
  const hasAdditionalDetails = images.length > 0 || videos.length > 0;

  return (
    <FormSectionCard
      icon={ClipboardList}
      title={t("CS_HEADER_TICKET_DETAILS")}
      titleClassName="text-base font-semibold text-ink-950"
      divider
    >
      <dl className="grid gap-4 sm:grid-cols-2">
        {complaintDetails.rows.map((row) => (
          <div key={row.labelKey} className="min-w-0 space-y-1">
            <dt className="text-sm font-normal text-ink-600">
              {t(row.labelKey)}
            </dt>
            <dd className="text-sm font-medium break-words text-ink-950">
              {translateDetailValue(row.value, t)}
            </dd>
          </div>
        ))}
      </dl>
      {hasAdditionalDetails ? (
        <div className="mt-6">
          <h3 className="mb-3 text-sm font-semibold text-ink-950">
            {t("CS_TICKET_ADDITIONAL_DETAILS")}
          </h3>
          <ComplaintMediaList images={images} videos={videos} />
        </div>
      ) : null}
    </FormSectionCard>
  );
}
