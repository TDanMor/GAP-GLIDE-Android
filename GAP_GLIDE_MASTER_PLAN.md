# Gap Glide — Master Project Plan

> Living project document. Keep this file in the repository root and update it when scope, milestones, or major technical decisions change.

## 1. Project Identity

| Item | Value |
|---|---|
| Product name | Gap Glide |
| Android application ID | `com.nbsas.gapglide` |
| Primary platform | Android |
| Current release | `versionCode = 1`, `versionName = "1.0.0"` |
| Project type | Offline, single-player, hyper-casual 2D arcade game |
| Core promise | A fast, simple, satisfying tap-to-fly game with no account, no network dependency, and no pay-to-win mechanics. |

## 2. Product Vision

Gap Glide is a minimalist tap-to-fly game. The player guides a small character through gaps between moving obstacles. A tap applies upward momentum; gravity pulls the character downward. Passing an obstacle earns points; collision with an obstacle or screen boundary ends the run.

The design priorities are:

- Immediate, responsive control.
- Clear geometric visuals and high contrast.
- Stable performance on ordinary Android phones.
- Fast restart loop.
- Offline-first use.
- A small, maintainable native Android codebase.

## 3. Scope Rules

### 3.1 MVP: Included

The Android MVP includes:

- One-player endless gameplay.
- Portrait orientation.
- Start, Playing, and Game Over states.
- Tap-to-fly movement and gravity.
- Randomised vertical gaps in moving obstacle pairs.
- Player-to-obstacle and player-to-boundary collision.
- Real-time current score.
- Persistent local best score.
- Easy, Medium, and Hard difficulty choices.
- Restart after a game over.
- Minimal Canvas-drawn visuals.
- Local unit and instrumented tests where practical.

### 3.2 Explicitly Excluded From MVP

Do not add these without a deliberate planning decision and an approved issue:

- Multiplayer, LAN, Wi-Fi Direct, Bluetooth, room codes, or bots.
- Accounts, login, profiles, cloud saves, or backend services.
- Analytics, advertising, tracking, or user data collection.
- Online leaderboards.
- In-app purchases, subscription, cosmetics store, or pay-to-win mechanics.
- Unity, C#, external game engines, or a web/PWA version.
- Navigation 3, Compose Adaptive, dependency injection, database layers, or unnecessary frameworks.
- Complex 3D graphics, shaders, asset pipelines, or animation systems.

## 4. Technical Architecture

### 4.1 Stack

| Area | Decision |
|---|---|
| Language | Kotlin |
| UI and rendering | Jetpack Compose and Compose Canvas |
| Game-loop mechanism | Lifecycle-aware Compose coroutine using `LaunchedEffect` and `withFrameNanos` |
| State | Simple local Compose state and game-state models |
| Persistence | Jetpack DataStore Preferences, local high score only |
| Input | Touch input; any editor/emulator support must not compromise phone input |
| Orientation | Portrait |
| Internet | Offline only; no `INTERNET` permission |
| Minimum SDK | 24 (Android 7.0) |
| Compile/target SDK | 36 (Android 16) |
| Release optimisation | R8 minification and resource shrinking for release only |

### 4.2 Architecture Principles

- Keep code understandable before making it clever.
- Prefer a small number of Kotlin files and explicit logic.
- Keep gameplay logic separate from Canvas drawing where practical.
- Do not add a library merely for convenience if Android/Compose already provides the capability.
- Do not introduce navigation for Start/Playing/Game Over; use overlays in a single game screen.
- Do not allocate unnecessary objects inside the frame loop.
- Make physics constants easy to tune from one clear location.
- Do not modify unrelated files when implementing a feature.

### 4.3 Primary Code Responsibilities

| File | Responsibility |
|---|---|
| `MainActivity.kt` | Application entry point and top-level Compose setup. |
| `GameScreen.kt` | Canvas drawing, HUD, start/game-over overlays, and screen-level input. |
| `GameState.kt` | Models, enums, and immutable or simple state types for player, obstacle, score, difficulty, and game phase. |
| `GameEngine.kt` | Frame updates, gravity, jump impulse, obstacle lifecycle, scoring, collision detection, and difficulty parameters. |

Normal Android project files, theme files, resources, manifest files, tests, Gradle configuration, and ProGuard/R8 rules are also part of the project. The “four-file” rule applies to the primary custom game logic, not required Android files.

### 4.4 Game States

```text
Start
  └─ user selects difficulty and presses Play
       └─ Playing
            ├─ user taps: upward impulse
            ├─ player passes obstacle: score +1
            └─ collision: GameOver
                 └─ user presses Restart: Playing
```

## 5. Current Baseline

### 5.1 Completed

The current baseline is reported to include:

- Native Android app named Gap Glide.
- Package and application ID: `com.nbsas.gapglide`.
- Kotlin, Jetpack Compose, and Compose Canvas.
- Frame-independent loop using `withFrameNanos` within `LaunchedEffect`.
- Tap-to-fly physics and gravity.
- Procedural obstacle pairs with random gap positions.
- Circle-to-AABB collision detection.
- Start, Playing, and Game Over overlays in one screen.
- Score and local high score through DataStore.
- Easy, Medium, and Hard settings.
- Portrait mode, safe-area-aware UI, and edge-to-edge game world.
- No Internet permission, no analytics, and no networking code.
- R8/resource shrinking enabled for release builds only.
- Debug and release variants reported as successfully built.

### 5.2 Current Git Baseline

| Item | Value |
|---|---|
| Commit | `f18be3562d6cc8f989a45952a8868a2f4027a9e1` |
| Commit message | `Initial Gap Glide Android MVP` |
| Remote | None configured yet |
| Repository status at baseline | Clean working tree |

## 6. Verification Before New Features

AI-generated implementation claims are not sufficient proof of device readiness. Complete this checklist on a physical Android device before expanding features:

- [x] App installs and opens without a crash.
- [x] Start screen shows Gap Glide and all difficulty options.
- [x] Easy, Medium, and Hard visibly change speed and/or gap size.
- [x] Tap creates an immediate upward movement.
- [x] Gravity feels stable and predictable.
- [x] Upper and lower boundary collisions end the run.
- [x] Obstacle collisions end the run consistently.
- [x] Score increments exactly once per cleared obstacle.
- [x] Restart resets the current score and game objects.
- [x] Best score persists after closing and reopening the app.
- [x] Portrait orientation stays locked.
- [x] UI controls avoid camera cut-outs, status bar, and navigation areas.
- [x] No red errors occur in Logcat during play.
- [x] At least ten minutes of play produces no freeze, stutter, memory issue, or major overheating.

**Validated on: OnePlus 9 (2026-08-23)**

## 7. Delivery Roadmap

### Milestone 0 — Baseline and Device Validation

**Goal:** Prove the existing MVP works on a real Android phone.

Tasks:

1. [x] Run debug build on a physical device.
2. [x] Complete the device test checklist in Section 6.
3. Record and fix actual bugs only. (None observed so far).
4. Build and install a signed release APK for private testing. (Deferred until after Milestone 1 polish).
5. Make a Git commit for every verified bug fix.

Definition of done:

- [x] The game has been tested on at least one physical Android device.
- [x] No blocking bugs are known.
- [ ] A signed private-test APK has been installed and played.

### Milestone 1 — Product Polish

**Goal:** Improve game feel without changing core architecture.

Priority tasks:

1. Replace the default launcher icon with a simple adaptive Gap Glide icon.
2. Tune each difficulty using playtests.
3. Improve colours, readable score UI, and Game Over presentation.
4. Add small sound effects and optional vibration only if they remain fully offline.
5. Add small particles or screen shake only after performance testing.
6. Improve accessibility basics: high contrast, readable text, and touch-target sizing.

Definition of done:

- Branding is no longer default Android branding.
- The game is clearly playable at each difficulty.
- Effects do not degrade performance or introduce dependencies.

### Milestone 2 — Release Preparation

**Goal:** Make a private test-ready and Play-ready release package.

Tasks:

1. Finalise application icon, title, screenshots, and short description.
2. Confirm `versionCode` and `versionName`.
3. Create and securely store a signing key outside the Git repository.
4. Generate a signed release APK for private testing.
5. Create a signed Android App Bundle (`.aab`) for Play internal testing.
6. Prepare a short privacy policy describing local-only high-score storage and no data transmission.
7. Complete Google Play data safety declarations accurately.
8. Use Play internal testing before public production release.

Definition of done:

- A signed AAB builds successfully.
- Store listing assets and privacy statement are ready.
- Internal testers can install and test the game.

### Milestone 3 — Content Expansion

**Goal:** Add replay value while retaining offline, simple architecture.

Candidate features, in order:

1. Additional visual palettes/background themes.
2. Unlockable themes stored locally.
3. Better difficulty tuning or a custom challenge mode.
4. Achievement-like local milestones, with no account required.
5. Carefully limited accessibility and visual settings.

Rule: Each feature must be proposed as a small plan, reviewed, implemented, tested, and committed separately.

### Milestone 4 — Monetisation Decision

**Goal:** Decide whether monetisation is desirable only after the core game is tested and enjoyable.

Possible future options:

- One-time paid game.
- Cosmetic-only unlocks.
- Optional rewarded advertising on Android.
- Ad-removal purchase.

Constraints:

- No pay-to-win mechanics.
- No forced online requirement for the core game.
- Adding advertising, payments, or analytics changes privacy, dependency, release, and testing requirements. Create a separate design decision document before implementation.

### Milestone 5 — Advanced Original Vision

These are separate large projects, not extensions to add casually:

- Anonymous online leaderboard.
- Bot opponents.
- Local multiplayer for 2–8 players.
- LAN/hotspot/room-code multiplayer.
- Mixed human-and-bot modes.
- Campaign mode with 20+ stages.
- Scenario packs.
- iOS or PWA version.

Before entering this milestone, create a new architecture plan. The current Compose MVP should not be forced into networking or cross-platform support without reassessment.

## 8. Git and Repository Rules

### 8.1 Commit Discipline

- Work on one focused task at a time.
- Build and test before committing.
- Use clear imperative commit messages.
- Do not combine unrelated refactors, design changes, and bug fixes in one commit.
- Review `git status` and the staged-file list before every commit.

Recommended messages:

```text
Fix player collision at screen boundary
Tune hard difficulty obstacle gap
Add adaptive Gap Glide launcher icon
Prepare signed release configuration
```

### 8.2 Never Commit

Do not commit:

```text
.gradle/
build/
app/build/
local.properties
.idea/
*.iml
*.apk
*.aab
*.jks
*.keystore
keystore.properties
google-services.json
.env
signing keys, passwords, local device files, generated build outputs
```

### 8.3 Commit

Commit:

```text
Kotlin source code
AndroidManifest.xml
Resources and launcher icons
Gradle build files and wrapper
Version catalog
ProGuard/R8 rules
README.md
AGENTS.md
GAP_GLIDE_MASTER_PLAN.md
Unit and instrumented tests
.gitignore
```

### 8.4 Remote Backup

After real-device validation, create a private remote repository:

- Repository name: `gap-glide`
- Visibility: Private
- Do not initialise the remote with a README, `.gitignore`, or licence if the local project already has them.
- Push the verified local commit only after reviewing the remote URL.

## 9. AI Agent Operating Instructions

Use these rules in `AGENTS.md` or when prompting Gemini.

### 9.1 Before Any Change

The agent must:

1. Read this master plan and `AGENTS.md`.
2. State the requested task in one sentence.
3. List every file it plans to modify or create.
4. Explain the approach in five bullets or fewer.
5. Ask for approval before editing if the task affects more than a small isolated bug fix.

### 9.2 While Implementing

The agent must:

- Implement only the approved task.
- Avoid unrelated refactoring.
- Preserve offline-first behaviour unless explicitly approved otherwise.
- Avoid adding dependencies unless necessary and explicitly justified.
- Keep Start, Playing, and Game Over within one game screen unless a new plan is approved.
- Avoid claims of real-device tests that it cannot actually perform.

### 9.3 After Implementing

The agent must:

1. List changed files.
2. Build the applicable variant.
3. Report exact build results and warnings.
4. Give a manual testing checklist.
5. Report known limitations honestly.
6. Stop and wait for user testing and approval before the next task.

### 9.4 Before Git Commit or Push

The agent must:

1. Show `git status`.
2. Show the exact staged files.
3. Confirm no secrets, APKs, AABs, build folders, signing files, or machine-specific files are staged.
4. Ask for approval of the commit message and remote target.
5. Never create a remote, commit, or push without explicit approval.

## 10. Current Next Actions

1. Milestone 1.1: Custom adaptive launcher icon.
2. Milestone 0.5: Private GitHub repository backup.
3. Milestone 0.4: Signed release APK (Deferred until after Milestone 1 polish).
4. Milestone 2: Signed release APK and AAB preparation.

## 11. Change Log

| Date | Change | Status |
|---|---|---|
| 2026-08-23 | MVP scope simplified from a large Unity/multiplayer concept to native offline Android game. | Completed |
| 2026-08-23 | Android app package changed to `com.nbsas.gapglide`. | Completed |
| 2026-08-23 | Removed Internet permission and unused dependencies; release shrinking enabled. | Completed |
| 2026-08-23 | Initial local Git commit created: `f18be3562d6cc8f989a45952a8868a2f4027a9e1`. | Completed |
| 2026-08-23 | Physical-device validation on OnePlus 9: Pass. No bugs observed. | Completed |
| 2026-08-23 | Custom adaptive launcher icon implemented and verified on OnePlus 9. | Completed |
| TBD | Signed release APK. | Pending |
| TBD | Private Git remote backup. | Pending |
