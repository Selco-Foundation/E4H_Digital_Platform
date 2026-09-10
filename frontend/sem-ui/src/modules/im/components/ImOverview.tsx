import {
  contextPath,
  loadModules,
  useAuthStore,
  useTranslate,
} from "@/shared";
import { Button, StatTile } from "@/ui";
import { Link } from "@tanstack/react-router";
import { Clock, FileText, Plus } from "lucide-react";
import { useEffect } from "react";
import { useImInboxSummary } from "../hooks/use-im-inbox-summary";
import { canCreateIncident, hasImAccess } from "../utils/access";

export function ImKpis() {
  const { t } = useTranslate();
  const user = useAuthStore((state) => state.user);
  const basePath = `/${contextPath()}/employee/im`;
  const { data, isLoading } = useImInboxSummary();

  useEffect(() => {
    void loadModules(["rainmaker-im"]);
  }, []);

  if (!hasImAccess(user?.roles)) {
    return null;
  }

  return (
    <>
      <StatTile
        icon={<FileText className="h-6 w-6" />}
        iconClassName="bg-info text-info-foreground"
        label={t("TOTAL_IM")}
        value={isLoading ? "-" : (data?.totalCount ?? "-")}
        link={`${basePath}/inbox`}
      />
      <StatTile
        icon={<Clock className="h-6 w-6" />}
        iconClassName="bg-warning text-warning-foreground"
        label={t("TOTAL_NEARING_SLA")}
        value={isLoading ? "-" : (data?.nearingSlaCount ?? "-")}
        link={`${basePath}/inbox?nearing=1`}
      />
    </>
  );
}

export function ImOverviewActions() {
  const { t } = useTranslate();
  const user = useAuthStore((state) => state.user);
  const basePath = `/${contextPath()}/employee/im`;

  if (!hasImAccess(user?.roles) || !canCreateIncident(user?.roles)) {
    return null;
  }

  return (
    <Button asChild size="sm" className="gap-1.5 rounded-md px-4 text-sm font-semibold">
      <Link to={`${basePath}/incident/create`}>
        <Plus className="size-4" />
        <span className="lg:hidden">{t("ES_IM_RAISE_TICKET_SHORT")}</span>
        <span className="hidden lg:inline">{t("ES_IM_RAISE_NEW_TICKET")}</span>
      </Link>
    </Button>
  );
}

export function ImDetails() {
  return null;
}
