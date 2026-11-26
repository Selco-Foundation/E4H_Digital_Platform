---
name: grill-me
description: Interview the developer about a Jira ticket, PRD section or feature request BEFORE any code is written, to reach a shared understanding and split it into small tasks. Use when the user pastes a ticket/requirement, says "grill me", "plan this", "understand this ticket", or the request is vague or large.
---

# Grill me — understand before coding

Goal: no code until we agree on *what* to build. Wrong understanding is the most expensive token waste.

## Steps
1. **Read only what's given** (ticket text, attached images/PDF pages). Then look up only the files the ticket clearly touches (use the code map in `CLAUDE.md`; `grep` for names). Don't explore further yet.
2. **Restate** the ticket in 3–6 bullets: what the user sees/does, data in/out, where it lives in the app (installation / AMC / assessment).
3. **Ask questions in one batch** (max ~8), each with your **recommended answer**, so the developer can just reply "ok" or correct. Cover only what's unclear:
   - entry point / navigation, roles that see it (role selection, user type)
   - API: existing endpoint (grep the repo in `lib/repositories/`), or new one (→ backend question)
   - offline/cache behaviour (Isar, background sync in `background_service.dart`), error & empty states
   - MDMS-driven or hard-coded? (forms via `digit_forms_engine` / MDMS schemas)
   - validation rules, i18n keys
   - what is explicitly out of scope
4. **Self-grill mode**: if the user says "self-grill" / "use your recommendations", answer your own questions with the recommendations, state them, and go on.
5. **Output the plan**:
   - Final understanding (bullets)
   - Task list: each task ≤ ~5 files and independently testable, in order (e.g. model+repo → bloc → page+route → i18n → tests)
   - Files to touch per task (paths)
   - Backend questions (for the backend team — the developer does no backend work)
6. **Stop.** Wait for approval, then implement one task at a time with the `flutter-feature` skill. Suggest `/clear` between tasks; the plan can be pasted back as the next prompt.
