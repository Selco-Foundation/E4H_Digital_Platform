import { translateOr, useTranslate } from "@/shared";
import { Button, Input, Label } from "@/ui";
import { useState } from "react";

interface InboxSearchProps {
  onSearch: (params: Record<string, string>) => void;
  initialApplicationNumber?: string;
}

export function InboxSearch({ onSearch, initialApplicationNumber = "" }: InboxSearchProps) {
  const { t } = useTranslate();
  const [complaintNo, setComplaintNo] = useState(initialApplicationNumber);

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (complaintNo.trim()) {
      onSearch({ applicationNumber: complaintNo.trim() });
      return;
    }
    onSearch({});
  };

  const clearSearch = () => {
    setComplaintNo("");
    onSearch({});
  };

  return (
    <form onSubmit={handleSubmit} className="ml-0 space-y-2 md:ml-6">
      <div className="flex flex-col gap-3 md:flex-row md:items-end">
        <div className="space-y-1">
          <Label htmlFor="serviceRequestId">
            {translateOr(t, "CS_COMMON_TICKET_NO", "Ticket No.")}
          </Label>
          <Input
            id="serviceRequestId"
            name="serviceRequestId"
            value={complaintNo}
            onChange={(event) => setComplaintNo(event.target.value)}
            className="max-w-xs"
          />
        </div>
        <div className="flex items-center gap-3">
          <Button type="submit" size="lg">
            {translateOr(t, "ES_COMMON_SEARCH", "Search")}
          </Button>
          <button
            type="button"
            onClick={clearSearch}
            className="text-sm text-primary hover:underline"
          >
            {translateOr(t, "ES_COMMON_CLEAR_SEARCH", "Clear Search")}
          </button>
        </div>
      </div>
    </form>
  );
}
