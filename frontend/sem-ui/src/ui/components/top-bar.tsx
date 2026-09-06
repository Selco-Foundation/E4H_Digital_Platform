import type { ReactNode } from "react";
import { Breadcrumbs, type BreadcrumbEntry } from "./breadcrumbs";
import { LanguageSwitcher } from "./language-switcher";

interface TopBarProps {
  title: ReactNode;
  description?: ReactNode;
  breadcrumbs?: BreadcrumbEntry[];
  /**
   * Extra actions rendered after the default language switcher (e.g. a
   * module's create/raise action) — beside the title on desktop, and
   * repositioned beside the breadcrumbs on mobile (the same node, shown in
   * one spot or the other via CSS, not two independent copies of state).
   */
  actions?: ReactNode;
}

/**
 * Every page's top bar: title/description/breadcrumbs plus the language
 * switcher every page gets by default, followed by whatever extra action the
 * calling page contributes. The switcher is desktop-only since AppShell's
 * mobile header already carries a compact one.
 */
export function TopBar({ title, description, breadcrumbs, actions }: TopBarProps) {
  const hasBottomRow = Boolean(breadcrumbs?.length) || Boolean(actions);
  const combinedActions = (
    <>
      <span className="hidden lg:inline-flex">
        <LanguageSwitcher />
      </span>
      {/* `actions` can be a non-null node that renders no visible DOM — e.g. a
          role-gated action component returning null — so whether to show the
          separator has to be answered by the DOM (is the slot actually
          empty), not by whether the `actions` prop itself is truthy. */}
      <span
        aria-hidden="true"
        className="hidden h-8 w-px bg-border lg:inline-block has-[+.topbar-actions:empty]:hidden"
      />
      <span className="topbar-actions contents">{actions}</span>
    </>
  );

  return (
    <div className="space-y-1">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-[32px] leading-[48px] font-semibold text-ink-950">{title}</h1>
        <div className="hidden shrink-0 items-center gap-3 lg:flex">{combinedActions}</div>
      </div>
      {description ? (
        <p className="text-sm leading-[21px] text-ink-600">{description}</p>
      ) : null}
      {hasBottomRow ? (
        <div className="flex items-start justify-between gap-3">
          <div>{breadcrumbs?.length ? <Breadcrumbs items={breadcrumbs} /> : null}</div>
          {actions ? <div className="shrink-0 lg:hidden">{actions}</div> : null}
        </div>
      ) : null}
    </div>
  );
}
