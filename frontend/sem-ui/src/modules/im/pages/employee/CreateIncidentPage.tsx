import { contextPath, employeeHomePath, translateOr, useTranslate } from "@/shared";
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
        title={translateOr(t, "ES_IM_RAISE_NEW_TICKET", "Raise New Ticket")}
        breadcrumbs={[
          { label: translateOr(t, "CORE_COMMON_OVERVIEW", "Overview"), to: homePath },
          { label: translateOr(t, "ES_IM_INBOX", "Inbox"), to: inboxPath },
          { label: translateOr(t, "ES_IM_TICKET_CREATE", "Raise ticket") },
        ]}
      />

      <CreateTicketForm inboxPath={inboxPath} />
    </div>
  );
}
