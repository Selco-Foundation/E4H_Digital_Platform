---
name: github
description: Read or act on GitHub issues (tickets), PRs, CI checks and review comments for this repo with token-lean gh commands.
---

Tickets and PRs both live in `Selco-Foundation/E4H_Digital_Platform`.

Always request only the fields you need (`--json` + `--jq`). Never dump a full issue thread, full diff or full CI log.

## Issues (tickets)

**Read a ticket**

`gh issue view <n> --json number,title,state,labels,assignees,body --jq '{number, title, state, labels: [.labels[].name], assignees: [.assignees[].login], body}'`

**Comments** (only the latest few)

`gh issue view <n> --json comments --jq '.comments[-5:][] | {author: .author.login, createdAt, body}'`

**Find tickets**

`gh issue list --assignee "@me" --state open --json number,title,labels --jq '.[] | {number, title, labels: [.labels[].name]}' --limit 20`

`gh issue list --label "<label>" --state open --json number,title --limit 20`

`gh issue list --search "<words> in:title" --json number,title,state --limit 10`

Common labels: `Frontend`, `Backend`, `UI`, `MDMS Data`, `Data Issue`, `Adhoc Prod Bugs`, `enhancement`.

**PRs linked to a ticket**

`gh pr list --search "<n> in:body" --state all --json number,title,state --limit 5`

**Writes** (`gh issue create`, `comment`, `edit`, `close`) only when the developer asks; show the exact command first.

- Create with a repo template: `gh issue create --template "Bug report"` (also `"Feature request"`, `"User story template"`), or pass `--title` and `--body-file -`.
- Link a PR to its ticket with `Closes #<n>` in the PR body.

## Pull requests

**Overview**

`gh pr view <n> --json number,title,state,baseRefName,headRefName,mergeStateStatus,reviewDecision --jq .`

**Changed files first, then only the diff you need**

`gh pr diff <n> --name-only`

`git fetch origin <headRef> && git diff origin/develop...origin/<headRef> -- <path>`

**CI**

`gh pr checks <n>`, then for a failure only: `gh run view <run-id> --log-failed | tail -80`

**Review comments (inline)**

`gh api repos/{owner}/{repo}/pulls/<n>/comments --jq '.[] | {id, path, line, user: .user.login, body}'`

Reply in the thread, not top-level: `gh api repos/{owner}/{repo}/pulls/<n>/comments/<id>/replies -X POST -f body='…'`

**Find PRs**

`gh pr list --author "@me" --json number,title,headRefName --limit 10`

`gh pr list --head <branch> --json number,url --jq '.[0]'`

**Create**: branch off `develop`; always pass `--title`; body via `--body-file -`:

`gh pr create --base develop --title "<area>: <summary>" --body-file - <<'EOF' … EOF`

**If `gh pr edit` fails** with the "Projects (classic) deprecation" GraphQL error:

`gh api repos/{owner}/{repo}/pulls/<n> -X PATCH -f title='…' -f body='…'`

## Never

`gh pr merge`, `gh pr close`, `gh issue delete`, deleting branches, or `gh repo` changes, unless the developer explicitly asks.
