import { contextPath, translateOr, useAuthStore, useTranslate } from "@/shared";
import { Card, CardContent } from "@/ui";
import { Link } from "@tanstack/react-router";
import { canCreateIncident } from "../../utils/access";

function ImHeaderIcon() {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
      <path d="M0 0h24v24H0z" fill="currentColor" className="text-primary" />
      <path
        d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 9h-2V5h2v6zm0 4h-2v-2h2v2z"
        fill="white"
      />
    </svg>
  );
}

export function ComplaintLinks() {
  const { t } = useTranslate();
  const user = useAuthStore((state) => state.user);
  const basePath = `/${contextPath()}/employee/im`;

  const links = canCreateIncident(user?.roles)
    ? [
        {
          text: translateOr(t, "ES_IM_NEW_INCIDENT", "New Ticket"),
          link: `${basePath}/incident/create`,
        },
      ]
    : [];

  return (
    <Card className="w-full max-w-[270px]">
      <CardContent className="space-y-3 p-4">
        <div className="flex items-center gap-3">
          <div className="flex h-14 w-14 items-center justify-center rounded-md bg-primary p-3 text-primary-foreground">
            <ImHeaderIcon />
          </div>
          <span className="font-semibold">
            {translateOr(t, "ES_IM_HEADER_INCIDENTS", "Tickets")}
          </span>
        </div>
        <div className="space-y-1 text-sm">
          {links.map((item) => (
            <div key={item.link}>
              <Link to={item.link} className="text-primary hover:underline">
                {item.text}
              </Link>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
