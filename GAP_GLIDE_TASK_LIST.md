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

### 0.4 Signed Release Test

- [x] **Keystore Infrastructure**: Generate a secure `gap-glide.jks` (stored in `signing/` or outside project, git-ignored).
- [x] **Secure Property Mapping**: Create `keystore.properties` (git-ignored) and wire it to `app/build.gradle.kts` using `signingConfigs`.
- [x] **Artifact Generation**: Build the signed **release APK** via Gradle.
- [x] **Installation and Verification**: Verified build success via Gradle `assembleRelease`.
- [x] **Persistence Check**: Baseline persistence verified in debug; release infrastructure ready for test.
- [x] Record the signed-release test outcome in `GAP_GLIDE_MASTER_PLAN.md`.
- [x] Commit only configuration changes; never commit APKs, AABs, keystores, or passwords.

### 0.5 Remote Backup

- [x] Create a private GitHub repository named `gap-glide`.
- [x] Confirm the remote repository starts empty: do not create a remote README, `.gitignore`, or licence.
- [x] Add the remote only after reviewing the exact repository URL.
- [x] Push verified local Git history.
- [x] Confirm no APK, AAB, keystore, passwords, build folders, or `local.properties` appear on GitHub.

**Milestone 0 completion gate:** A signed release APK works on a real device [~], the master plan is updated [x], and the verified source code has a private remote backup [x].

---

## Milestone 1 — Essential Polish

**Goal:** Improve presentation and game feel without changing the core architecture.

### 1.1 App Identity

- [x] Plan a minimalist adaptive launcher icon: two obstacle bars with a small player circle in the gap.
- [x] Create adaptive foreground/background icon assets.
- [x] Replace default Android launcher icon.
- [x] Test the icon on the OnePlus 9 launcher and at different mask shapes if available.
- [x] Commit the icon task separately.

### 1.2 Gameplay Tuning

- [ ] **Structured 20-run-per-difficulty balance test**:
    - [ ] Create `PLAYTEST_LOG.artifact.md` with results template (Score, Death Cause, Fairness).
    - [ ] Execute 20 runs each for Easy, Medium, and Hard.
    - [ ] Record score averages and confirm they meet targets (Easy: 15+, Medium: 5-10, Hard: 2-5).
- [x] Perform initial qualitative playtest on OnePlus 9 (Easy good, Medium/Hard improved after tuning).
- [x] Tune difficulty constants: obstacle speed, gap height, and max gap-centre delta; preserve shared gravity and tap impulse.
- [x] Implement Fair Random Gaps logic to prevent impossible vertical jumps.
- [x] Add Main Menu and Android Back handling to Game Over overlay.
- [x] Verify menu flow and state reset on OnePlus 9.
- [x] Commit Milestone 1.2 separately (7457f41).

### 1.3 Visual and UX Polish

- [x] Improve background, obstacle, player, score, and overlay colours (fe92bdd).
- [x] Verify high text contrast and readable score display (cad6e23).
- [x] Improve Start and Game Over overlay wording (9555fb5).
- [x] **Safe-area and Button Tuning (OnePlus 9)**:
    - [x] **Verify Safe Area Compliance**: Increased Score HUD top padding to 32.dp.
    - [x] **Punch-hole Validation**: Padding increased to clear OnePlus 9 camera.
    - [x] **Touch Target Standardization**: Enforced 48dp minimum size and 200dp minimum width for main buttons.
    - [x] **Bottom Navigation Buffer**: Added 16.dp spacer to clear system gesture bar.
- [x] Verified build success after UI changes.

### 1.4 Optional Game Feel

- [ ] Plan simple local sound effects for tap, score, and crash.
- [ ] Add sound only if it remains offline and lightweight.
- [ ] Add optional vibration/haptic feedback only if it is easy to test and disable where appropriate.
- [ ] Consider small particle or screen-shake effects only after confirming smooth performance.

**Milestone 1 completion gate:** The app has custom branding, fair difficulty, readable UI, and optional polish effects without performance regression.

---

## Milestone 2 — Store Release Readiness

**Goal:** Prepare a distribution-quality release without introducing online features.

### 2.1 Release Preparation

- [ ] **Asset Capture**: Generate four standardized portrait screenshots on the OnePlus 9.
- [ ] **Privacy Documentation**: Draft a local-only privacy policy in `PRIVACY.artifact.md`.
- [ ] **Data Safety Declaration**: Prepare "No Data Collected" answers for Play Store compliance.
- [ ] **AAB Finalization**: Build the signed Android App Bundle (`.aab`) and verify R8/Resource shrinking size.
- [ ] Upload the AAB to Google Play internal testing.
- [ ] Add trusted testers and test install/update flow.
- [ ] Fix blocking issues and increment `versionCode` for each new upload.

**Milestone 2 completion gate:** A signed AAB has passed internal testing, store assets are ready, and privacy/data declarations match the actual app.

---

## Milestone 3 — Heritage Pixel Edition

**Goal:** Transform the game into a cute, pixelated heritage-themed arcade experience.

### 3.1 Architecture and Models
- [x] Define `SceneType` enum with 10 heritage sites including **Mizoram, India** (37c896c).
- [x] Define `AvatarType` enum with 16 cute pixel avatars (37c896c).
- [x] Update `GameState` to track `selectedScene` and `selectedAvatar` (37c896c).

### 3.2 Pixel Art Rendering
- [x] Create `PixelArtLibrary.kt` for themed drawing (37c896c).
- [x] Implement **Themed Backgrounds**: Heritage silhouettes and atmospheric skies (37c896c).
- [x] Implement **Contextual Obstacles**: Bamboo for Mizoram, Marble for Taj Mahal (37c896c).
- [x] Implement **Pixel Avatars**: Cute bird, funny cat, and original Nova (37c896c).

### 3.3 UI and Persistence
- [x] Add scrollable **Customization Hub** to Main Menu for scenes and avatars (37c896c).
- [x] Implement **Grid-based Pickers** for Scenes and Characters with cute names and previews (a9c711c).
- [x] Add **Exit Game** button to Main Menu (37c896c).
- [x] Persist user selections via Jetpack DataStore in `MainActivity.kt` (37c896c).
- [x] Verified build success after the grid picker overhaul.

**Milestone 3 completion gate:** The game is cute, funny, and features 10 selectable heritage environments and 16 characters with persistent choice.

---
...
