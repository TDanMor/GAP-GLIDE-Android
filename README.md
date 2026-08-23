# Gap Glide

Gap Glide is a minimalist arcade game for Android where players navigate a player character through gaps between obstacles. The game focuses on high-precision timing and simple touch controls.

## Features
- **Minimalist Gameplay**: Simple mechanics focused on "gliding" through gaps.
- **Difficulty Levels**: Easy, Medium, and Hard modes to challenge all skill levels.
- **Offline First**: No internet required. The game does not track you or display advertisements.
- **Local Persistence**: Stores only your personal high score locally on your device.

## Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Graphics**: Compose Canvas for high-performance game rendering.
- **Data Storage**: AndroidX DataStore (Preferences) for local high scores.

## Requirements
- Android Studio Ladybug (or newer)
- Android SDK 36 (Android 16)
- A physical Android device or Emulator running API 24+

## How to Run
1. Open the project in Android Studio.
2. Select the `app` run configuration.
3. Choose a device/emulator.
4. Click **Run** (Play button).

To build a release version, use the Gradle task:
`./gradlew assembleRelease`

## License
All rights reserved — not yet licensed for reuse.
