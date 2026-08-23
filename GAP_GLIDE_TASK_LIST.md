# Gap Glide — Milestone Task List

> This is the practical task list derived from the project’s original large vision and the approved simplified Android MVP. Use it together with `GAP_GLIDE_MASTER_PLAN.md` and `AGENTS.md`.
>
> **Rule:** Complete tasks in order. One task = one Gemini approval, one implementation/test cycle, and normally one focused Git commit. Do not start a later milestone while the earlier milestone is unverified.

## Status Legend

- [x] Completed
- [~] In progress / awaiting confirmation
- [ ] Not started
- [!] Requires a separate planning decision before implementation

---

## Milestone 0 — Stable MVP Baseline

**Goal:** Prove that the current offline Android MVP works as a real release candidate.

### 0.1 Core Game MVP

- [x] Create native Android project for Gap Glide.
- [x] Set package/application ID to `com.nbsas.gapglide`.
- [x] Create one-screen architecture with Start, Playing, and Game Over states.
- [x] Implement Compose Canvas game rendering.
- [x] Implement tap-to-fly player input.
- [x] Implement gravity and player movement.
- [x] Implement obstacle-pair generation and movement.
- [x] Implement random vertical obstacle gaps.
- [x] Implement boundary and obstacle collision detection.
- [x] Implement current score.
- [x] Implement restart flow.
- [x] Implement Easy, Medium, and Hard difficulty settings.
- [x] Implement local high score using DataStore.

### 0.2 Technical Cleanup

- [x] Remove `INTERNET` permission.
- [x] Remove unused dependencies such as Retrofit, Room, CameraX, Navigation 3, Adaptive Compose, location, Coil, and Accompanist permissions.
- [x] Set `minSdk = 24`.
- [x] Set `compileSdk = 36` and `targetSdk = 36`.
- [x] Set release version to `versionCode = 1`, `versionName = "1.0.0"`.
- [x] Enable R8 code shrinking and resource shrinking for release builds only.
- [x] Build debug variant.
- [x] Build unsigned release variant.

### 0.3 Device Validation

- [x] Install and test the debug build on a OnePlus 9.
- [x] Verify basic gameplay with no bugs observed so far.
- [x] Update `GAP_GLIDE_MASTER_PLAN.md` with OnePlus 9 validation result.
- [x] Commit the validation documentation with message: `Record OnePlus 9 device validation`.

### 0.4 Signed Release Test — Next

- [ ] Create a signing keystore outside the Git repository.
- [ ] Securely store keystore file, alias, passwords, and recovery details outside Git.
- [ ] Generate a signed **release APK**.
- [ ] Install the signed release APK on the OnePlus 9.
- [ ] Repeat the MVP test checklist on the signed release build.
- [ ] Record the signed-release test outcome in `GAP_GLIDE_MASTER_PLAN.md`.
- [ ] Commit only documentation/configuration changes; never commit APKs, AABs, keystores, or passwords.

### 0.5 Remote Backup

- [ ] Create a private GitHub repository named `gap-glide`.
- [ ] Confirm the remote repository starts empty: do not create a remote README, `.gitignore`, or licence.
- [ ] Add the remote only after reviewing the exact repository URL.
- [ ] Push verified local Git history.
- [ ] Confirm no APK, AAB, keystore, passwords, build folders, or `local.properties` appear on GitHub.

**Milestone 0 completion gate:** A signed release APK works on a real device, the master plan is updated, and the verified source code has a private remote backup.

---

## Milestone 1 — Essential Polish

**Goal:** Improve presentation and game feel without changing the core architecture.

### 1.1 App Identity

- [ ] Plan a minimalist adaptive launcher icon: two obstacle bars with a small player circle in the gap.
- [ ] Create adaptive foreground/background icon assets.
- [ ] Replace default Android launcher icon.
- [ ] Test the icon on the OnePlus 9 launcher and at different mask shapes if available.
- [ ] Commit the icon task separately.

### 1.2 Gameplay Tuning

- [ ] Play at least 20 runs on Easy, Medium, and Hard.
- [ ] Record perceived difficulty, average score, unfair deaths, and control responsiveness.
- [ ] Tune only documented constants: gravity, jump impulse, obstacle speed, spawn interval, and gap size.
- [ ] Confirm each difficulty is visibly distinct and still fair.
- [ ] Commit tuning separately.

### 1.3 Visual and UX Polish

- [ ] Improve background, obstacle, player, score, and overlay colours.
- [ ] Verify high text contrast and readable score display.
- [ ] Improve Start and Game Over overlay wording.
- [ ] Ensure buttons are large enough and within safe areas.
- [ ] Test on at least one phone after each UI change.

### 1.4 Optional Game Feel

- [ ] Plan simple local sound effects for tap, score, and crash.
- [ ] Add sound only if it remains offline and lightweight.
- [ ] Add optional vibration/haptic feedback only if it is easy to test and disable where appropriate.
- [ ] Consider small particle or screen-shake effects only after confirming smooth performance.

**Milestone 1 completion gate:** The app has custom branding, fair difficulty, readable UI, and optional polish effects without performance regression.

---

## Milestone 2 — Private and Play Release Readiness

**Goal:** Prepare a distribution-quality release without introducing online features.

### 2.1 Release Identity

- [ ] Confirm final app name: `Gap Glide`.
- [ ] Confirm final application ID: `com.nbsas.gapglide`.
- [ ] Confirm release version number.
- [ ] Finalise adaptive launcher icon.
- [ ] Prepare a concise app description.

### 2.2 Store Assets

- [ ] Capture portrait screenshots on a physical device.
- [ ] Create a feature graphic if required by the chosen distribution channel.
- [ ] Prepare Play Store short description and long description.
- [ ] Prepare an app category and content-rating answers.

### 2.3 Privacy and Legal Basics

- [ ] Write a simple privacy policy.
- [ ] State accurately: the game stores only a local high score and does not transmit user data in the current version.
- [ ] Re-check manifest and Gradle dependencies before publishing.
- [ ] Complete Google Play Data Safety information honestly.
- [ ] Create a support email address if publishing publicly.

### 2.4 Release Artifacts and Testing

- [ ] Build a signed release APK for private testers.
- [ ] Build a signed Android App Bundle (`.aab`) for Google Play.
- [ ] Upload the AAB to Google Play internal testing.
- [ ] Add trusted testers.
- [ ] Test install/update flow using the Play-distributed build.
- [ ] Fix blocking issues and increment `versionCode` for each new upload.

**Milestone 2 completion gate:** A signed AAB has passed internal testing, store assets are ready, and privacy/data declarations match the actual app.

---

## Milestone 3 — Offline Content Expansion

**Goal:** Increase replay value without changing the offline-first product promise.

- [ ] Add visual theme palettes.
- [ ] Add alternative backgrounds and obstacle styles using lightweight Canvas drawing.
- [ ] Add locally stored cosmetic unlocks only if their implementation stays small.
- [ ] Add local challenge objectives or achievement-style milestones.
- [ ] Consider a custom difficulty setting only after the three presets are balanced.
- [ ] Review performance and APK size after every content addition.

**Planning rule:** Each item must be separately planned and approved. Do not add multiple features in one agent task.

---

## Milestone 4 — Monetisation Decision [!]

**Goal:** Decide whether to monetise only after gameplay and private/public testing show the game is enjoyable.

Possible directions:

- [ ] One-time paid application.
- [ ] Cosmetic-only optional purchases.
- [ ] Optional rewarded ads.
- [ ] Paid ad-removal option.

Before implementing any option:

- [ ] Create a new architecture and privacy plan.
- [ ] Reassess the no-Internet rule.
- [ ] Reassess Data Safety disclosures, dependencies, permissions, testing, and user consent requirements.
- [ ] Ensure no pay-to-win or forced advertisement design.

---

## Milestone 5 — Original Advanced Vision [!]

**Goal:** Evaluate the larger original concept as a separate product phase.

Original ideas deferred from MVP:

- [ ] Campaign mode with 20+ stages.
- [ ] More scenario/theme packs.
- [ ] Bot opponents with multiple difficulty levels.
- [ ] Local multiplayer for 2–8 people.
- [ ] LAN, hotspot, Bluetooth, Wi-Fi Direct, or room-code joining.
- [ ] Mixed human-plus-bot modes.
- [ ] Anonymous online leaderboard.
- [ ] Android monetisation and web/iOS PWA strategy.

Before starting any of these:

- [ ] Create a new planning document.
- [ ] Decide whether native Compose remains appropriate or whether architecture/platform direction should change.
- [ ] Estimate technical cost, privacy impact, testing matrix, and maintenance burden.
- [ ] Obtain explicit approval for the new scope.

---

## Global Definition of Done

For every task:

1. The task is explicitly approved.
2. Only planned files are modified.
3. The relevant app build succeeds.
4. Manual testing is completed where applicable.
5. Gemini reports actual build output, warnings, and known limitations.
6. `git status` is reviewed.
7. No secrets or generated release artifacts are staged.
8. A focused commit is created only with explicit approval.
9. The master plan or task list is updated only for meaningful milestone progress.
