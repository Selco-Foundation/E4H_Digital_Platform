# SEM UI

Custom React app for Saura e-Mitra (SEM), built alongside the existing DIGIT-UI based
`installation-ui` app rather than replacing it. Structure mirrors the Livelihood project's
`livelihood-ui`: Vite, TypeScript, pnpm, TanStack Router, React Query, Zustand, shadcn/ui,
and Tailwind CSS.

## Prerequisites

- **Node.js 24+** (see `.nvmrc`) — a `preinstall` check refuses to install on older Node
- **pnpm 10+** (via Corepack — not a separate apt install)

## First-time setup (pnpm not found)

If `pnpm` is missing, use **nvm + Corepack**:

```bash
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"

nvm install 24
nvm use 24
corepack enable
corepack prepare pnpm@10.12.1 --activate

node -v   # must show v24.x
pnpm -v   # must show 10.12.1
```

If `nvm use 24` doesn't change what `node -v` reports, make sure Node 24 is first on `PATH`
for this shell, or run `nvm alias default 24` to make it the shell default everywhere.

## Quick start

```bash
cd frontend/sem-ui
nvm use
pnpm install
pnpm dev
```

Open the URL Vite prints (e.g. `http://localhost:5174/sem-ui/`) — it redirects straight to
the login screen.

Create a local `.env` file with your environment values (see below) — it's gitignored, same
as `livelihood-ui`'s.

## Environment variables

| Variable | Purpose |
|----------|---------|
| `VITE_PROXY_API` | E4H/SEM backend proxy target for local dev (e.g. `https://e4h-dev.selcofoundation.org`) |
| `VITE_CONTEXT_PATH` | This app's own base path (e.g. `sem-ui`) |
| `VITE_STATE_LEVEL_TENANT_ID` | Fallback tenant id, used only if the global config below hasn't loaded yet |
| `VITE_GLOBAL_CONFIG_URL` | Remote `globalConfigs.js` URL (same one `installation-ui` already loads — injected into `index.html` at dev/build time) |

**Important — `CONTEXT_PATH` gotcha:** the shared `globalConfigs.js` file above returns
`CONTEXT_PATH: "e4hhub"`, which belongs to the *existing* DIGIT-UI deployment. This app's
`contextPath()` (`src/shared/config/global-config.ts`) deliberately does **not** read that
key from the global config — it always resolves from this app's own `VITE_CONTEXT_PATH` env
var, so this app's routes never collide with the legacy app's path. Don't "fix" this to read
from the global config again.

## App layout

```
sem-ui/
├── src/
│   ├── main.tsx, App.tsx, router.tsx, modules.ts
│   ├── shared/          # API, stores, config, i18n
│   ├── ui/              # shadcn components + Tailwind design system
│   └── modules/
│       └── core/        # Login, auth shell, employee layout
├── public/
└── docs/
```

## Global config

Runtime config uses `window.globalConfigs.getConfig("KEY")`, same mechanism as
`installation-ui`. In local dev, Vite injects the script from `VITE_GLOBAL_CONFIG_URL` in
your `.env`. Confirmed working values from the real config: `STATE_LEVEL_TENANT_ID` (`"in"`),
`LOGO_LIST`, `INVALIDROLES`. Not yet configured on the backend: `commonUiConfig.LoginBannerImages`
and `common-masters.Languages` MDMS masters — both currently come back empty, so the login
carousel shows its empty state and the language switcher only offers English until that data
is seeded.

## Localization

Same DIGIT localization service as `installation-ui`:

- Backend: `POST /localization/messages/v1/_search`
- Client: `i18next` + `react-i18next`
- Default modules loaded at startup: `rainmaker-common` and `rainmaker-{STATE_TENANT}`

```tsx
import { useTranslate } from "@/shared";

export function MyPage() {
  const { t } = useTranslate();
  return <h1>{t("ACTION_TEST_HOME")}</h1>;
}
```

## Scripts

| Command | Description |
|---------|-------------|
| `pnpm dev` | Start Vite dev server |
| `pnpm build` | Typecheck and build production bundle |
| `pnpm typecheck` | Run TypeScript checks |
| `pnpm ui:add` | Add shadcn components |

## What's ported so far

- Full design system (`src/ui`) and core `shared/` layer (auth, i18n, MDMS, boundary, HRMS,
  localization APIs) — verified against E4H's actual backend controllers, not assumed.
- Login screen, authenticated shell (`AppShell`), and a minimal home page (`src/modules/core`).
- Not yet ported: forgot-password/change-password/profile pages, `TopBar`'s per-module actions
  slot has no modules using it yet, `api/user-profile.ts`, `stores/ui-store.ts`.
