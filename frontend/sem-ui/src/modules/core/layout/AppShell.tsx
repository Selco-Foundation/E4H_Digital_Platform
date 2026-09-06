import { getModuleNavItems } from "@/module-registry";
import {
  contextPath,
  employeeLoginPath,
  employeeProfilePath,
  getConfigString,
  logoutUser,
  tenantId,
  translateOr,
  useAuthStore,
  useJurisdictionStore,
  useTranslate,
  type NavItem,
} from "@/shared";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  Avatar,
  AvatarFallback,
  Button,
  LanguageSwitcher,
  Sheet,
  SheetContent,
  SheetDescription,
  SheetTitle,
  SheetTrigger,
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarSeparator,
  cn,
  toast,
} from "@/ui";
import { Link, Outlet, useNavigate, useRouterState } from "@tanstack/react-router";
import { Home, LogOut, Menu } from "lucide-react";
import { useState } from "react";

function isNavItemActive(item: NavItem, pathname: string, homePath: string): boolean {
  const matchAgainst = [item.to, ...(item.matchPrefixes ?? [])];
  return item.to === homePath
    ? pathname === item.to
    : matchAgainst.some((prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`));
}

export function AppShell() {
  const basePath = `/${contextPath()}`;
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user);
  const navItems = getModuleNavItems(user?.roles);
  const accessToken = useAuthStore((state) => state.accessToken);
  const employeeTenantId = useAuthStore((state) => state.employeeTenantId);
  const clearSession = useAuthStore((state) => state.clearSession);
  const clearJurisdiction = useJurisdictionStore((state) => state.clearJurisdiction);
  const pathname = useRouterState({ select: (state) => state.location.pathname });
  const { t } = useTranslate();
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const initials =
    user?.name?.slice(0, 2).toUpperCase() ??
    user?.userName?.slice(0, 2).toUpperCase() ??
    "SU";

  const homePath = `${basePath}/employee`;
  const allNavItems: NavItem[] = [
    {
      id: "overview",
      label: "Overview",
      labelKey: "CORE_COMMON_OVERVIEW",
      to: homePath,
      icon: Home,
    },
    ...navItems,
  ];

  return (
    <SidebarProvider className="h-svh overflow-hidden bg-sidebar">
      <Sidebar
        collapsible="none"
        style={{ borderRight: "none" }}
        className="hidden w-(--sidebar-width-icon) items-center md:w-(--sidebar-width) md:items-stretch lg:flex"
      >
        <SidebarHeader className="items-center gap-6 px-2 pt-12 pb-5 md:px-7">
          <div className="flex h-10 w-10 items-center justify-center rounded-[3px] p-1 md:h-[80px] md:w-[80px]">
            <img
              src={getConfigString("SELCO_LOGO")}
              alt={translateOr(t, "CORE_LOGO_ALT", "Selco Foundation Logo")}
              className="h-full w-full object-contain"
            />
          </div>
          <SidebarSeparator className="mx-0 h-[5px] w-full bg-white/60" />
        </SidebarHeader>
        <SidebarContent className="px-2 md:px-7">
          <SidebarGroup className="p-0">
            <SidebarGroupContent>
              <SidebarMenu>
                {allNavItems.map((item) => {
                  const Icon = item.icon;
                  const isActive = isNavItemActive(item, pathname, homePath);
                  const label = item.labelKey
                    ? translateOr(t, item.labelKey, item.label)
                    : item.label;

                  return (
                    <SidebarMenuItem key={item.id}>
                      <SidebarMenuButton
                        asChild
                        isActive={isActive}
                        className="justify-center rounded-lg hover:bg-transparent hover:text-sidebar-foreground hover:underline active:bg-transparent active:text-sidebar-foreground md:justify-start"
                      >
                        <Link to={item.to} aria-label={label}>
                          {Icon ? <Icon /> : null}
                          <span className="hidden md:inline">{label}</span>
                        </Link>
                      </SidebarMenuButton>
                    </SidebarMenuItem>
                  );
                })}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        </SidebarContent>
        <SidebarFooter className="gap-3 px-2 pb-12 md:px-7">
          <Link
            to={employeeProfilePath()}
            className="flex items-center justify-center gap-2 rounded-lg transition-opacity hover:opacity-80 md:justify-start"
          >
            <Avatar className="h-11 w-11 border-[1.5px] border-ink-300">
              <AvatarFallback className="bg-white/15 text-sidebar-foreground">
                {initials}
              </AvatarFallback>
            </Avatar>
            <div className="hidden min-w-0 flex-1 md:block">
              <p className="truncate text-sm font-medium">
                {user?.name ?? user?.userName ?? translateOr(t, "CORE_COMMON_USER_FALLBACK", "User")}
              </p>
            </div>
          </Link>
          <SidebarSeparator className="mx-0 h-[5px] w-full bg-white/60" />
          <Button
            variant="outline"
            size="sm"
            className="w-full justify-center gap-2 text-foreground md:justify-start"
            onClick={() => setConfirmOpen(true)}
          >
            <LogOut />
            <span className="hidden md:inline">
              {translateOr(t, "CORE_COMMON_LOGOUT_DIALOGUE_HEADER", "Sign out")}
            </span>
          </Button>
        </SidebarFooter>
      </Sidebar>

      <div className="flex min-h-0 w-full flex-1 flex-col lg:contents">
        <header className="flex shrink-0 items-center justify-between bg-sidebar px-4 py-4 text-sidebar-foreground lg:hidden">
          <div className="flex items-center gap-2">
            <Sheet open={mobileNavOpen} onOpenChange={setMobileNavOpen}>
              <SheetTrigger asChild>
                <Button
                  variant="ghost"
                  size="icon-sm"
                  aria-label={translateOr(t, "CORE_NAV_OPEN_MENU", "Open menu")}
                  className="text-current hover:bg-white/10 hover:text-current"
                >
                  <Menu className="size-5" />
                </Button>
              </SheetTrigger>
              <SheetContent
                side="left"
                showCloseButton={false}
                className="w-[280px] border-none bg-[#134738] p-0 text-white"
              >
                <SheetTitle className="sr-only">
                  {translateOr(t, "CORE_APP_TITLE", "Saura e-Mitra")}
                </SheetTitle>
                <SheetDescription className="sr-only">
                  {translateOr(t, "CORE_NAV_MENU_DESCRIPTION", "App navigation menu")}
                </SheetDescription>
                <div className="flex h-full flex-col justify-between px-7 pt-12 pb-7">
                  <div className="flex flex-col gap-5">
                    <div className="flex flex-col items-center gap-6">
                      <img
                        src={getConfigString("SELCO_LOGO")}
                        alt={translateOr(t, "CORE_LOGO_ALT", "Selco Foundation Logo")}
                        className="h-15 w-15 object-contain"
                      />
                      <div className="h-px w-full bg-white/60" />
                    </div>
                    <nav className="flex flex-col gap-4">
                      {allNavItems.map((item) => {
                        const Icon = item.icon;
                        const isActive = isNavItemActive(item, pathname, homePath);
                        const label = item.labelKey
                          ? translateOr(t, item.labelKey, item.label)
                          : item.label;

                        return (
                          <Link
                            key={item.id}
                            to={item.to}
                            onClick={() => setMobileNavOpen(false)}
                            className={cn(
                              "flex items-center gap-2 rounded-lg px-3 py-2 text-sm",
                              isActive
                                ? "bg-white font-semibold text-[#134738]"
                                : "font-medium text-white",
                            )}
                          >
                            {Icon ? <Icon className="size-5" /> : null}
                            {label}
                          </Link>
                        );
                      })}
                    </nav>
                  </div>

                  <div className="flex flex-col gap-3">
                    <Link
                      to={employeeProfilePath()}
                      onClick={() => setMobileNavOpen(false)}
                      className="flex min-w-0 items-center gap-2 transition-opacity hover:opacity-80"
                    >
                      <Avatar className="h-8 w-8 border-[1.5px] border-white/40">
                        <AvatarFallback className="bg-white/15 text-white">
                          {initials}
                        </AvatarFallback>
                      </Avatar>
                      <span className="truncate text-sm font-medium text-white">
                        {user?.name ?? user?.userName ?? translateOr(t, "CORE_COMMON_USER_FALLBACK", "User")}
                      </span>
                    </Link>
                    <div className="h-px w-full bg-white/40" />
                    <Button
                      variant="outline"
                      size="sm"
                      className="w-full justify-center gap-2 text-foreground"
                      onClick={() => {
                        setMobileNavOpen(false);
                        setConfirmOpen(true);
                      }}
                    >
                      <LogOut className="size-4" />
                      <span>
                        {translateOr(t, "CORE_COMMON_LOGOUT_DIALOGUE_HEADER", "Sign out")}
                      </span>
                    </Button>
                  </div>
                </div>
              </SheetContent>
            </Sheet>
            <span className="text-lg font-semibold">
              {translateOr(t, "CORE_APP_TITLE", "Saura e-Mitra")}
            </span>
          </div>
          <LanguageSwitcher compact />
        </header>

        <SidebarInset className="min-h-0 flex-1 [scrollbar-gutter:stable] overflow-y-auto bg-page px-4 py-4 lg:rounded-tl-[48px] lg:rounded-bl-[48px] lg:pt-12 lg:pr-8 lg:pb-10 lg:pl-8">
          <Outlet />
        </SidebarInset>
      </div>

      <AlertDialog open={confirmOpen} onOpenChange={setConfirmOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {translateOr(t, "CORE_COMMON_LOGOUT_DIALOGUE_HEADER", "Sign out")}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {translateOr(
                t,
                "CORE_COMMON_LOGOUT_DIALOGUE_MESSAGE",
                "Are you sure you want to sign out?",
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{translateOr(t, "CORE_COMMON_CANCEL", "Cancel")}</AlertDialogCancel>
            <AlertDialogAction
              onClick={async () => {
                try {
                  if (accessToken) {
                    await logoutUser(accessToken, employeeTenantId ?? tenantId());
                  }
                } catch {
                  // best-effort: proceed with local sign-out even if the server call fails
                } finally {
                  clearSession();
                  clearJurisdiction();
                  toast.success(translateOr(t, "CORE_LOGOUT_SUCCESS_TOAST", "Signed out successfully"));
                  void navigate({ to: employeeLoginPath() });
                }
              }}
            >
              {translateOr(t, "CORE_COMMON_LOGOUT_DIALOGUE_HEADER", "Sign out")}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </SidebarProvider>
  );
}
