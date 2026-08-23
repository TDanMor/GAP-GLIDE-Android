\# Gap Glide — Agent Instructions



\## Source of truth



Before planning, editing, testing, committing, or recommending changes, read:



\- `GAP\_GLIDE\_MASTER\_PLAN.md`

\- `README.md`



If these instructions conflict with the master plan, follow the master plan.



\## Project identity



\- App name: Gap Glide

\- Application ID: `com.nbsas.gapglide`

\- Platform: Android

\- Technology: Kotlin, Jetpack Compose, Compose Canvas, Kotlin Coroutines, DataStore

\- Game type: Offline single-player tap-to-fly arcade game

\- Orientation: Portrait

\- Minimum SDK: 24

\- Compile SDK and target SDK: 36



\## Architecture rules



\- Keep the architecture simple.

\- Use the existing one-screen design.

\- Keep Start, Playing, and Game Over as local state/overlays.

\- Do not add Navigation 3, Compose Adaptive, dependency injection, Room, Retrofit, OkHttp, CameraX, Coil, analytics, or unnecessary libraries.

\- Keep game logic understandable and avoid unrelated refactoring.

\- Avoid unnecessary allocations in the frame loop.

\- Preserve Compose Canvas rendering and the existing game engine approach.

\- Use DataStore only for local high-score persistence unless I explicitly approve more storage.



\## Scope restrictions



Do not add any of the following unless I explicitly approve a new plan:



\- Internet access or the INTERNET permission

\- Networking, backend, accounts, login, cloud save, analytics, tracking

\- Multiplayer, LAN, Bluetooth, Wi-Fi Direct, room codes, bots

\- Online leaderboard

\- Ads, in-app purchases, subscriptions, or payment systems

\- Unity, C#, web/PWA, iOS support, or a new game engine

\- Complex 3D graphics, shaders, or large external asset systems



\## Implementation workflow



Before making a non-trivial change:



1\. State the task in one sentence.

2\. List every file you propose to create or modify.

3\. Explain the approach in five bullets or fewer.

4\. Wait for my approval before editing.



While implementing:



\- Complete only the approved task.

\- Do not change unrelated code.

\- Do not add dependencies without explaining why they are required.

\- Keep the app offline-first.

\- Keep changes small and testable.



After implementing:



1\. List the files changed.

2\. Build the relevant variant.

3\. Report actual build results and warnings.

4\. Provide a concise manual testing checklist.

5\. Clearly identify known limitations.

6\. Do not claim a real-device test unless I performed it and reported the result.

7\. Stop and wait for my approval before starting another task.



\## Git rules



Before committing or pushing:



1\. Show `git status`.

2\. Show the exact staged files.

3\. Confirm no secrets, signing keys, passwords, APKs, AABs, build folders, local.properties, or machine-specific files are staged.

4\. Ask me to approve the exact commit message.

5\. Ask me to approve the exact remote repository before any push.

6\. Never create a remote, commit, or push without explicit approval.



Never commit:



```text

.gradle/

build/

app/build/

local.properties

.idea/

\*.iml

\*.apk

\*.aab

\*.jks

\*.keystore

keystore.properties

google-services.json

.env

```



\## Current priority



1\. Test Gap Glide on a real Android phone.

2\. Fix only verified bugs.

3\. Create a signed release APK for private testing.

4\. Push the verified repository to a private GitHub repository.

5\. Add a custom adaptive launcher icon.

