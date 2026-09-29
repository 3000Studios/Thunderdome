# Thunder Dome Agent Contract

This repository is the canonical Android source for Thunder Dome (Gradle root name `AeroStrike`, application ID `com.aistudio.aerostrike.xrkfpz`). GitHub remote: `3000Studios/Thunderdome`; production branch: `main`.

These rules are shared by Codex, Google Antigravity, and any other coding agent working in this checkout.

## Safety and ownership

- Inspect `git status` and the relevant diff before editing. Preserve unrelated or pre-existing work.
- Never overwrite, revert, stash, reset, clean, commit, or push another agent's changes unless the owner explicitly directs it.
- Do not run two writing agents against the same files. Split work by non-overlapping file ownership or use one writer.
- Keep secrets out of source, logs, screenshots, prompts, and commits. Treat `.env` as private; `.env.example` may contain names and safe placeholders only.
- Do not invent business facts, credentials, approvals, links, prices, reviews, analytics, or deployment results.

## Current shared-work boundary

At the time this bridge was created, the following files already contained uncommitted gameplay work. Treat them as owner work until reviewed and committed:

- `app/src/main/java/com/example/game/engine/EnvironmentSystem.kt`
- `app/src/main/java/com/example/game/engine/GameEngine.kt`
- `app/src/main/java/com/example/game/render/GameRenderer.kt`
- `app/src/main/java/com/example/ui/screens/CombatScreen.kt`

Re-read `git status` and `git diff` every time; this list is a warning, not a substitute for checking live state.

## Implementation standard

- Inspect and extend the existing Kotlin/Compose architecture; do not create competing implementations.
- Make the smallest complete change and avoid new dependencies unless they clearly outperform existing or native capabilities.
- Mobile is the default: prevent clipping and overlap, preserve one-handed controls, use at least 44dp touch targets, support accessibility semantics, and respect reduced motion where applicable.
- Refactor fragile code only in the touched area and only when required for a safe complete result.
- Never weaken Android, Firebase, network, authentication, or secret-handling controls for convenience.

## Validation

From the repository root, run the checks relevant to the change. The default full local gate is:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug --no-daemon --console=plain
```

A successful build is not an install or device validation. For UI/gameplay work, separately verify the exact APK on an authorized device or emulator, launch it, exercise the changed path, and inspect fresh visual evidence before claiming device completion.

## Handoff protocol

Before handing work to another agent:

1. Stop editing and record the current branch, `git status`, files touched, checks run, failures, and the exact next action in `AGENT_HANDOFF.md`.
2. Identify which files the receiving agent may edit and which files remain reserved.
3. The receiving agent must re-check live status and diffs before acting; a handoff note is context, not proof.
4. Only verified facts may be marked complete. Distinguish code changed, build passed, APK installed, app launched, and gameplay visually verified.

Do not commit `AGENT_HANDOFF.md`; it is local coordination state and is ignored by Git.
