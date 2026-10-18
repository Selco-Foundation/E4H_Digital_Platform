import { getModuleOverviews } from "@/module-registry";
import { translateOr, useAuthStore, useTranslate } from "@/shared";
import { TopBar } from "@/ui";

export function HomePage() {
  const { t } = useTranslate();
  const user = useAuthStore((state) => state.user);
  const { kpis, details, actions } = getModuleOverviews();
  const displayName = user?.name ?? user?.userName ?? "";

  return (
    <div className="space-y-6">
      <TopBar
        title={
          <>
            {translateOr(t, "CORE_HOME_WELCOME", "Welcome")}
            {displayName ? <span>, {displayName}</span> : null}
          </>
        }
        actions={
          actions.length > 0
            ? actions.map(({ Component, moduleId }) => <Component key={moduleId} />)
            : null
        }
      />
      {kpis.length > 0 ? (
        <div className="flex gap-3 lg:flex-wrap lg:gap-4">
          {kpis.map(({ Component, moduleId }) => (
            <Component key={moduleId} />
          ))}
        </div>
      ) : null}
      {details.map(({ Component, moduleId }) => (
        <Component key={moduleId} />
      ))}
    </div>
  );
}
