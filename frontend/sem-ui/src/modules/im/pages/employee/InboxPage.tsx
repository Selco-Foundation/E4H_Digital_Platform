import {
  contextPath,
  employeeHomePath,
  translateOr,
  useAuthStore,
  useTranslate,
} from "@/shared";
import { Button, TopBar } from "@/ui";
import { Link, useNavigate, useSearch } from "@tanstack/react-router";
import { Plus } from "lucide-react";
import { DesktopInbox } from "../../components/inbox/DesktopInbox";
import { buildDefaultInboxRoleFilters } from "../../hooks/inbox-defaults";
import { useImInboxData } from "../../hooks/use-im-inbox-summary";
import type { InboxRouteSearch } from "../../routes";
import type { ImInboxFilters } from "../../types/inbox";
import { canCreateIncident } from "../../utils/access";

export function InboxPage() {
  const { t } = useTranslate();
  const user = useAuthStore((state) => state.user);
  const basePath = `/${contextPath()}/employee/im`;

  // The inbox route's path is computed at runtime via contextPath(), so there's no
  // static `Route` export to use the fully-typed search API here — read/write the
  // current route's search loosely instead, narrowed to how this page actually
  // calls it (a search-updater navigate, staying on the current route).
  const search = useSearch({ strict: false }) as InboxRouteSearch;
  const navigate = useNavigate() as (opts: {
    search: (prev: InboxRouteSearch) => InboxRouteSearch;
    replace?: boolean;
  }) => Promise<void>;

  const defaultFilters = buildDefaultInboxRoleFilters(user);
  const filters = search.filter ?? defaultFilters;
  const pageOffset = search.pageOffset ?? 0;
  const pageSize = search.pageSize ?? 10;

  const inboxParams = {
    filters,
    limit: pageSize,
    offset: pageOffset,
    ...(search.nearing === "1" ? { nearingSLA: true } : {}),
  };

  const { data: complaints, isLoading } = useImInboxData(inboxParams);
  const totalRecords = complaints?.total ?? 0;
  const canCreateTicket = canCreateIncident(user?.roles);

  const handleFilterChange = (nextFilters: ImInboxFilters) => {
    // InboxFilter's internal state-combining effect fires once on every mount
    // (including on page reload) even when nothing actually changed — only reset
    // pagination when the filters genuinely differ from what's already persisted,
    // otherwise a reload would always snap back to the first page.
    const hasChanged = JSON.stringify(nextFilters) !== JSON.stringify(filters);
    void navigate({
      search: (prev: InboxRouteSearch) => ({
        ...prev,
        filter: nextFilters,
        pageOffset: hasChanged ? 0 : prev.pageOffset,
      }),
      replace: true,
    });
  };

  const goToOffset = (nextOffset: number) => {
    void navigate({
      search: (prev: InboxRouteSearch) => ({
        ...prev,
        pageOffset: Math.max(0, nextOffset),
      }),
      replace: true,
    });
  };

  const handlePageSizeChange = (nextPageSize: number) => {
    void navigate({
      search: (prev: InboxRouteSearch) => ({
        ...prev,
        pageSize: nextPageSize,
        pageOffset: 0,
      }),
      replace: true,
    });
  };

  const homePath = employeeHomePath();

  const breadcrumbItems = [
    { label: translateOr(t, "CORE_COMMON_OVERVIEW", "Overview"), to: homePath },
    { label: translateOr(t, "ES_IM_INBOX", "Inbox") },
  ];

  return (
    <div className="space-y-6">
      <TopBar
        title={translateOr(t, "ES_IM_ALL_TICKETS", "All Tickets")}
        breadcrumbs={breadcrumbItems}
        actions={
          canCreateTicket ? (
            <Button asChild size="sm" className="gap-1.5 rounded-md px-3">
              <Link to={`${basePath}/incident/create`}>
                <Plus className="size-4" />
                {translateOr(t, "ES_IM_RAISE_NEW_TICKET", "Raise New Ticket")}
              </Link>
            </Button>
          ) : null
        }
      />

      <DesktopInbox
        data={complaints}
        isLoading={isLoading}
        onFilterChange={handleFilterChange}
        searchParams={{ filters }}
        onNextPage={() => goToOffset(pageOffset + pageSize)}
        onPrevPage={() => goToOffset(pageOffset - pageSize)}
        onPageChange={(page) => goToOffset(page * pageSize)}
        onPageSizeChange={handlePageSizeChange}
        currentPage={Math.floor(pageOffset / pageSize)}
        totalRecords={totalRecords}
        pageSizeLimit={pageSize}
      />
    </div>
  );
}
