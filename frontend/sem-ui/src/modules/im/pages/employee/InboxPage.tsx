import {
  contextPath,
  employeeHomePath,
  useAuthStore,
  useTranslate,
} from "@/shared";
import { Button, TopBar } from "@/ui";
import { Link, useNavigate, useSearch } from "@tanstack/react-router";
import { PauseCircle, Plus } from "lucide-react";
import { DesktopInbox } from "../../components/inbox/DesktopInbox";
import { buildDefaultInboxRoleFilters } from "../../hooks/inbox-defaults";
import { useImInboxData } from "../../hooks/use-im-inbox-summary";
import type { InboxRouteSearch } from "../../routes";
import type { ImInboxFilters } from "../../types/inbox";
import { canCreateIncident, hasRole } from "../../utils/access";

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
    applicationNumber: search.applicationNumber,
    ...(search.nearing === "1" ? { nearingSLA: true } : {}),
  };

  const { data: complaints, isLoading } = useImInboxData(inboxParams);
  const totalRecords = complaints?.total ?? 0;
  const canCreateTicket = canCreateIncident(user?.roles);
  // Matches DIGIT-UI's IMCard.js: the "Pause RMS" entry point is gated to the
  // COMPLAINT_ASSESSOR role specifically (not the whole IM_ROLES set).
  const canManageRmsPause = hasRole(user?.roles, "COMPLAINT_ASSESSOR");

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

  const handleSearch = (params: { applicationNumber?: string }) => {
    void navigate({
      search: (prev: InboxRouteSearch) => ({
        ...prev,
        applicationNumber: params.applicationNumber,
        pageOffset: 0,
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
    { label: t("CORE_COMMON_OVERVIEW"), to: homePath },
    { label: t("ES_IM_INBOX") },
  ];

  return (
    <div className="space-y-6">
      <TopBar
        title={t("ES_IM_ALL_TICKETS")}
        breadcrumbs={breadcrumbItems}
        actions={
          <>
            {canManageRmsPause ? (
              <Button asChild variant="outline" size="sm" className="gap-1.5 rounded-md px-3">
                <Link to={`${basePath}/paused-rms-facilities`}>
                  <PauseCircle className="size-4" />
                  {t("ES_IM_PAUSE_RMS")}
                </Link>
              </Button>
            ) : null}
            {canCreateTicket ? (
              <Button asChild size="sm" className="gap-1.5 rounded-md px-3">
                <Link to={`${basePath}/incident/create`}>
                  <Plus className="size-4" />
                  {t("ES_IM_RAISE_NEW_TICKET")}
                </Link>
              </Button>
            ) : null}
          </>
        }
      />

      <DesktopInbox
        data={complaints}
        isLoading={isLoading}
        onFilterChange={handleFilterChange}
        searchParams={{ filters }}
        onSearch={handleSearch}
        initialApplicationNumber={search.applicationNumber}
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
