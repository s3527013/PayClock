# PayClock

PayClock is a modern Android time-tracking application built with Kotlin and Jetpack Compose. It provides a simple yet powerful interface for users to log their work hours, manage jobs, and track earnings, with seamless data synchronization via Firebase.

## Tech Stack

-   **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose), [Material 3](https://m3.material.io/)
-   **Architecture**: MVVM (Model-View-ViewModel), Repository Pattern
-   **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
-   **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
-   **Database**: [Room](https://developer.android.com/training/data-storage/room) (for offline caching), [Firebase Firestore](https://firebase.google.com/docs/firestore) (for cloud sync)
-   **Authentication**: [Firebase Authentication](https://firebase.google.com/docs/auth)
-   **Build System**: [Gradle](https://gradle.org/)

## Features

-   **User Authentication**: Secure sign-up, login, and password reset functionality.
-   **Job Management**: Create, view, and manage different jobs.
-   **Time Logging**: Start and stop work shifts with a single tap.
-   **Break Tracking**: Log paid and unpaid breaks during a shift.
-   **Offline Support**: Work seamlessly with offline data caching.
-   **Cloud Sync**: Automatic data synchronization with Firebase when online.
-   **User Settings**: Customize app theme and other preferences.

## Requirements

-   **OS**: Windows, macOS, or Linux
-   **IDE**: Android Studio Iguana | 2023.2.1 or newer
-   **JDK**: Version 17+
-   **Android SDK**: API Level 34 (Upside Down Cake)

## Getting Started

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/s3527013/PayClock.git
    ```
2.  **Open in Android Studio**:
    -   Launch Android Studio.
    -   Select "Open" and navigate to the cloned project directory.
3.  **Sync Dependencies**:
    -   Let Android Studio sync and download the required Gradle dependencies.
4.  **Run the app**:
    -   Connect a device or start an emulator.
    -   Click the "Run" button in Android Studio.

## Build & Run

-   **Build a debug APK**:
    ```bash
    ./gradlew assembleDebug
    ```
-   **Run unit tests**:
    ```bash
    ./gradlew testDebugUnitTest
    ```
-   **Install APK on a connected device/emulator**:
    ```bash
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    ```

## Project Structure

-   `app/src/main/java/uk/ac/tees/mad/payclock`: Main application source code.
    -   `core`: Core components like the Application class and dependency injection graph.
    -   `database`: Room database definitions, DAOs, and entities.
    -   `drawer`: UI components for the navigation drawer.
    -   `features`: Feature-based packages (auth, jobs, timelog, settings).
        -   `data`: Models, repositories, and data sources.
        -   `ui`: Composables, ViewModels, and UI-related logic.
    -   `navigation`: Navigation graph and routing logic.
    -   `ui/theme`: App-wide theme and styling.
-   `app/src/main/res`: Android resources (drawables, layouts, etc.).
-   `build.gradle.kts`: Project and module-level build configurations.

## Contributing

Contributions are welcome! To contribute:

1.  Create a feature branch from `main`.
2.  Make your changes and commit with clear, descriptive messages.
3.  Open a pull request against the `main` branch.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
