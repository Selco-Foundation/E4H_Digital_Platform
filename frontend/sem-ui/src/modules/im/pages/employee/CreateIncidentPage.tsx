import { contextPath, employeeHomePath, useTranslate } from "@/shared";
import { TopBar } from "@/ui";
import { CreateTicketForm } from "../../components/create/CreateTicketForm";
import { IM_ROUTES } from "../../constants/routes";

export function CreateIncidentPage() {
  const { t } = useTranslate();
  const basePath = `/${contextPath()}`;
  const homePath = employeeHomePath();
  const inboxPath = `${basePath}${IM_ROUTES.inbox}`;

  return (
    <div className="space-y-6">
      <TopBar
        title={t("ES_IM_RAISE_NEW_TICKET")}
        breadcrumbs={[
          { label: t("CORE_COMMON_OVERVIEW"), to: homePath },
          { label: t("ES_IM_INBOX"), to: inboxPath },
          { label: t("ES_IM_TICKET_CREATE") },
        ]}
      />

      <CreateTicketForm inboxPath={inboxPath} />
    </div>
  );
}
