import { useTranslate } from "@/shared";
import { Input } from "@/ui";
import { useEffect, useState } from "react";

const SEARCH_DEBOUNCE_MS = 400;

interface LiveTicketSearchProps {
  onSearch: (params: Record<string, string>) => void;
  initialApplicationNumber?: string;
}

/**
 * A no-button ticket-number search: placeholder text instead of a label, and
 * debounced live search-as-you-type instead of a Search/Clear pair. Kept as
 * its own component rather than changing InboxSearch, since that one mirrors
 * livelihood-ui's own search-with-buttons pattern and other screens may still
 * want that variant.
 */
export function LiveTicketSearch({ onSearch, initialApplicationNumber = "" }: LiveTicketSearchProps) {
  const { t } = useTranslate();
  const [complaintNo, setComplaintNo] = useState(initialApplicationNumber);

  useEffect(() => {
    const trimmed = complaintNo.trim();
    const id = setTimeout(() => {
      onSearch(trimmed ? { applicationNumber: trimmed } : {});
    }, SEARCH_DEBOUNCE_MS);

    return () => clearTimeout(id);
    // Only re-run when the typed value changes — onSearch is re-created on every
    // render of the parent, and including it here would reset the debounce timer
    // on every keystroke instead of just after the user stops typing.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [complaintNo]);

  return (
    <div className="ml-0 md:ml-6">
      <Input
        id="serviceRequestId"
        name="serviceRequestId"
        value={complaintNo}
        onChange={(event) => setComplaintNo(event.target.value)}
        placeholder={t("CS_COMMON_TICKET_NO")}
        className="h-8 max-w-xs"
      />
    </div>
  );
}
