import { translateOr, useTranslate } from "@/shared";
import { cn } from "@/ui/lib/utils";
import { ArrowLeft, ArrowRight, ChevronDown } from "lucide-react";

const PAGE_SIZE_OPTIONS = [10, 20, 50];

export interface PaginationProps {
  readonly currentPage: number;
  readonly totalRecords: number;
  readonly pageSizeLimit: number;
  readonly onNextPage: () => void;
  readonly onPrevPage: () => void;
  readonly onPageChange: (page: number) => void;
  readonly onPageSizeChange: (size: number) => void;
}

export function Pagination({
  currentPage,
  totalRecords,
  pageSizeLimit,
  onNextPage,
  onPrevPage,
  onPageChange,
  onPageSizeChange,
}: PaginationProps) {
  const { t } = useTranslate();
  const totalPages = Math.max(1, Math.ceil(totalRecords / pageSizeLimit));
  const canGoPrev = currentPage > 0;
  const canGoNext = (currentPage + 1) * pageSizeLimit < totalRecords;
  const pageNumbers = Array.from({ length: totalPages }, (_, index) => index);

  return (
    <div className="flex flex-wrap items-center justify-between gap-4 px-1">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <label htmlFor="pagination-page-size">
          {translateOr(t, "ES_COMMON_ITEMS_PER_PAGE", "Items per Page")}
        </label>
        <div className="relative">
          <select
            id="pagination-page-size"
            className="livelihood-filter-select h-8 w-auto pr-7"
            value={pageSizeLimit}
            onChange={(event) => onPageSizeChange(Number(event.target.value))}
          >
            {PAGE_SIZE_OPTIONS.map((size) => (
              <option key={size} value={size} className="cursor-pointer">
                {size}
              </option>
            ))}
          </select>
          <ChevronDown className="pointer-events-none absolute top-1/2 right-2 size-3.5 -translate-y-1/2 text-muted-foreground" />
        </div>
      </div>
      <div className="flex min-w-0 items-center gap-2">
        <button
          type="button"
          disabled={!canGoPrev}
          onClick={onPrevPage}
          className="inline-flex h-8 shrink-0 cursor-pointer items-center gap-2 rounded-lg px-3 py-1 text-base font-medium text-ink-950 transition-colors disabled:pointer-events-none disabled:opacity-40"
        >
          <ArrowLeft className="size-6" strokeWidth={1.5} />
          {translateOr(t, "CS_COMMON_PREVIOUS", "Previous")}
        </button>

        <div className="flex min-w-0 items-center gap-2 overflow-x-auto">
          {pageNumbers.map((page) => (
            <button
              key={page}
              type="button"
              onClick={() => onPageChange(page)}
              className={cn(
                "flex h-8 w-[34px] shrink-0 cursor-pointer items-center justify-center rounded-lg text-base font-medium transition-colors",
                page === currentPage
                  ? "bg-primary-700 text-neutral-25"
                  : "border border-neutral-300 bg-neutral-100 text-neutral-700 hover:border-primary-200 hover:bg-primary-100",
              )}
            >
              {page + 1}
            </button>
          ))}
        </div>

        <button
          type="button"
          disabled={!canGoNext}
          onClick={onNextPage}
          className="inline-flex h-8 shrink-0 cursor-pointer items-center gap-2 rounded-lg px-3 py-1 text-base font-medium text-ink-950 transition-colors disabled:pointer-events-none disabled:opacity-40"
        >
          {translateOr(t, "CS_COMMON_NEXT", "Next")}
          <ArrowRight className="size-6" strokeWidth={1.5} />
        </button>
      </div>
    </div>
  );
}
