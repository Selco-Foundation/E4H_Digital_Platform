import {
  contextPath,
  loadModules,
  translateOr,
  useAuthStore,
  useTranslate,
} from "@/shared";
import { Button, StatTile } from "@/ui";
import { Link } from "@tanstack/react-router";
import { Clock, FileText, Plus } from "lucide-react";
import { useEffect } from "react";
import { EndUserAssetsList } from "./EndUserAssetsList";
import { useEndUserAssets } from "../hooks/use-end-user-assets";
import { useImInboxSummary } from "../hooks/use-im-inbox-summary";
import { canCreateIncident, hasImAccess, isEndUser } from "../utils/access";

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
        label={translateOr(t, "TOTAL_IM", "Total")}
        value={isLoading ? "-" : (data?.totalCount ?? "-")}
        link={`${basePath}/inbox`}
      />
      <StatTile
        icon={<Clock className="h-6 w-6" />}
        iconClassName="bg-warning text-warning-foreground"
        label={translateOr(t, "TOTAL_NEARING_SLA", "Nearing SLA")}
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
        <span className="lg:hidden">{translateOr(t, "ES_IM_RAISE_TICKET_SHORT", "Raise Ticket")}</span>
        <span className="hidden lg:inline">{translateOr(t, "ES_IM_RAISE_NEW_TICKET", "Raise New Ticket")}</span>
      </Link>
    </Button>
  );
}

export function ImDetails() {
  const user = useAuthStore((state) => state.user);
  const endUser = isEndUser(user?.roles);
  const { assets, isLoading: isAssetsLoading } = useEndUserAssets({ enabled: endUser });

  if (!hasImAccess(user?.roles) || !endUser) {
    return null;
  }

  return <EndUserAssetsList assets={assets} isLoading={isAssetsLoading} />;
}
