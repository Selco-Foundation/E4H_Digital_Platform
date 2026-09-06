import { translateOr, useTranslate } from "@/shared";
import { Pagination, Skeleton } from "@/ui";
import type { ImInboxFilters, InboxDataResult } from "../../types/inbox";
import { ComplaintTable } from "./ComplaintTable";
import { InboxFilter } from "./InboxFilter";
import { MobileComplaintList } from "./MobileComplaintList";

interface DesktopInboxProps {
  data?: InboxDataResult;
  isLoading: boolean;
  onFilterChange: (filters: ImInboxFilters) => void;
  searchParams: {
    filters?: ImInboxFilters;
  };
  onNextPage: () => void;
  onPrevPage: () => void;
  currentPage: number;
  totalRecords: number;
  pageSizeLimit: number;
  onPageChange: (page: number) => void;
  onPageSizeChange: (size: number) => void;
}

export function DesktopInbox({
  data,
  isLoading,
  onFilterChange,
  searchParams,
  onNextPage,
  onPrevPage,
  currentPage,
  totalRecords,
  pageSizeLimit,
  onPageChange,
  onPageSizeChange,
}: DesktopInboxProps) {
  const { t } = useTranslate();

  return (
    <div className="space-y-5">
      <InboxFilter
        complaints={data}
        onFilterChange={onFilterChange}
        searchParams={searchParams}
      />

      {isLoading ? (
        <div className="livelihood-card p-6">
          <Skeleton className="h-64 w-full" />
        </div>
      ) : data && data.combinedRes.length === 0 ? (
        <div className="livelihood-card px-6 py-16 text-center text-sm text-muted-foreground">
          {translateOr(t, "CS_INBOX_NOTHING_TO_SHOW", "No Tickets Found")}
        </div>
      ) : data?.combinedRes?.length ? (
        <>
          <div className="hidden livelihood-card overflow-hidden lg:block">
            <ComplaintTable data={data.combinedRes} />
          </div>
          <div className="lg:hidden">
            <MobileComplaintList data={data.combinedRes} />
          </div>
        </>
      ) : (
        <div className="livelihood-card px-6 py-16 text-center text-sm text-muted-foreground">
          {translateOr(t, "CS_COMMON_ERROR_LOADING_RESULTS", "Unable to load results")}
        </div>
      )}

      {totalRecords > 0 ? (
        <Pagination
          currentPage={currentPage}
          totalRecords={totalRecords}
          pageSizeLimit={pageSizeLimit}
          onNextPage={onNextPage}
          onPrevPage={onPrevPage}
          onPageChange={onPageChange}
          onPageSizeChange={onPageSizeChange}
        />
      ) : null}
    </div>
  );
}
