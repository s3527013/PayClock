# PayClock

A simple Android time-tracking app built with Kotlin and Jetpack Compose.

## Tech stack
- Kotlin, Java
- Android (Jetpack Compose, Material3)
- Gradle

## Features
- Compose-based UI
- Main screen scaffolded in `uk.ac.tees.mad.payclock.MainActivity`
- Simple greeting/example UI ready to extend

## Requirements
- Windows (development tested)
- Android Studio Otter | 2025.2.1
- JDK 11+
- Android SDK (recommended latest)

## Getting started
1. Clone the repository:
   `git clone https://github.com/s3527013/PayClock.git`
2. Open the project in Android Studio.
3. Let Android Studio sync and download Gradle dependencies.

## Build & run (Windows)
- Build debug APK:
  `.\gradlew.bat assembleDebug`
- Run unit tests:
  `.\gradlew.bat testDebugUnitTest`
- Install debug APK (device must be connected / emulator running):
  `adb install -r app\build\outputs\apk\debug\app-debug.apk`

You can also run and debug directly from Android Studio.

## Project structure (high level)
- `app/src/main/java/uk/ac/tees/mad/payclock` - application code
  - `MainActivity.kt` - entry activity using Jetpack Compose
- `app/src/main/res` - resources and layouts
- `build.gradle` / `gradle.properties` - build configuration

## Contributing
- Create a feature branch from `master`
- Commit with clear messages
- Open a pull request against `master`

## License
MIT — add `LICENSE` file to the repository to confirm.
