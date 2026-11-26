---
name: localization-keys
description: Add, reuse or debug localization keys in installation-ui or micro-ui - choosing the module bundle and key prefix, reusing existing keys, and fixing a key that shows raw in the UI. Use when a change adds or removes user-facing labels, or a label renders as its key.
---

The `t()` rules in `frontend/AGENTS.md` (wrap every string, never pass raw
data, mark translated MDMS enums) always apply. This skill covers adding and
debugging the keys themselves.

## Where a key lives

- A key used by more than one module goes in `rainmaker-common`. A module
  loads only its own `rainmaker-<module>` bundle plus `rainmaker-common`, so a
  shared key registered under one module won't resolve in another.
- Before adding a key, check whether an existing one already has the same
  text, and reuse it if so.
- **micro-ui (`im`):** loads `rainmaker-im` (from `moduleCode "IM"`), plus
  `rainmaker-common` and `rainmaker-<state>`. Keys used only in `im` go in
  `rainmaker-im`. Existing prefixes: `CS_` (most UI copy), `ES_`, `RMS_`,
  `WF_` (workflow actions/states), `INCIDENT_`, `IM_`, `SERVICEDEFS_`. Match
  the prefix of neighbouring keys.

## Adding keys

- Never write translations into files inside this project, and never call
  the localization service API. The developer uploads them.
- List every new key with its English message and module in your summary, in
  the upload shape `{code, message, module, locale}`.

## A key shows raw in the UI

- The code must match exactly. A stray character in the localization service
  (for example a trailing `.`) makes the UI show the raw key.
- Check the cached bundle first: `localStorage["Digit.Locale.<locale>.<module>"]`
  in the browser. Search it for the code.
- Translations are cached in `localStorage` under `Digit.Locale.*`. After a
  localization change, remove those keys (or clear site storage, which logs
  the user out) and reload.
