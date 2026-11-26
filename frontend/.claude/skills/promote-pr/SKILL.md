---
name: promote-pr
description: Promote a PR already merged into develop on to staging (UAT) or main (production) by cherry-picking it onto a fresh branch and opening a PR against the target. Use when asked to move, promote or cherry-pick a merged change to staging or main.
---

Branches: `develop` (integration) → `staging` (UAT) → `main` (production).
Promote only after the PR is merged into `develop`.

1. `git fetch origin`.
2. Check how it merged: `git show --no-patch --format="%H %P" <sha>`. One
   parent means a squash merge (pick that one commit). More than one means
   pick the underlying commits.
3. `git checkout <target> && git pull origin <target>`. Always pick onto a
   fresh pull, never a stale local branch.
4. Branch off the target, not `develop`, as `cherry-pick/<short-desc>`.
5. Run `GIT_EDITOR=true git cherry-pick <sha>...`. If a related PR (for
   example a backend companion fix) is needed for the change to work, pick it
   onto the same branch too. Ask if it's unclear which commits belong.
6. Build-check again, because a clean pick doesn't guarantee it builds on the
   target. Build every module the picked commits touch.
7. Push, and open a PR against the target branch, never `develop`.

Rules:

- Never retarget the original PR's branch at `staging` or `main`.
- Reuse the original PR's title and `## Summary` word for word, minus any
  bot-appended sections (for example CodeRabbit's "Summary by CodeRabbit").
  If extra commits were picked, add a line saying what else is included.
- Read the original PR with
  `gh pr view <n> --json title,body,mergeCommit --jq '{title, body, sha: .mergeCommit.oid}'`.
