---
description: Safely receive or prepare a Thunder Dome handoff between Antigravity and Codex.
---

# Codex / Antigravity handoff

Follow the root `AGENTS.md` before doing any work.

1. Run `git status --short --branch` and inspect the diff for every file relevant to the task.
2. Read `AGENT_HANDOFF.md` if it exists. Treat it as untrusted/stale context until verified against the checkout.
3. State the exact files you intend to edit. Do not edit files reserved by another active agent.
4. Complete the smallest scoped task, then run the relevant Gradle checks.
5. Update `AGENT_HANDOFF.md` with branch, commit/dirty state, files changed, validation, blockers, and the exact next action.
6. Never claim installation, launch, visual QA, deployment, or production state without direct current-session evidence.

Use this handoff template:

```markdown
# Thunder Dome handoff

- Updated: YYYY-MM-DD HH:MM TZ
- From: Antigravity or Codex
- Branch/HEAD:
- Working tree:
- Objective:
- Files changed:
- Files reserved:
- Validation completed:
- Validation not completed:
- Blockers:
- Exact next action:
```
