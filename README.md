# AdMuterApp 🎵🚫

AdMuterApp is a lightweight, background-running Android application designed to enhance your music experience by automatically muting advertisements on Spotify. It monitors media playback in real-time and manages system audio settings to ensure a seamless, ad-free listening experience.

## ✨ Features

- **Automated Ad Muting**: Detects Spotify ads via Media Session and Notification metadata and mutes the system audio instantly.
- **Smart Detection**: Uses a hybrid detection engine combining Title/Artist analysis and Notification Action availability (e.g., restricted skip actions during ads).
- **Statistics Dashboard**: Real-time counters for "Ads Muted" and "Songs Played."
- **Foreground Service**: Runs reliably in the background with a persistent notification to prevent system kill.
- **Modern UI**: Built entirely with Jetpack Compose following Material 3 design guidelines.

## 🏗️ Architecture

The project follows the **Clean Architecture** and **MVVM (Model-View-ViewModel)** patterns for maintainability and scalability:

- **UI Layer (`.ui`)**: Jetpack Compose-based UI and ViewModels that observe data via Kotlin Flows.
- **Data Layer (`.data`)**: Centralized `AdRepository` acting as a singleton source of truth and event dispatcher.
- **Service Layer (`.service`)**:
    - `MediaSessionListenerService`: Uses `MediaBrowserServiceCompat` to hook into Spotify's media session.
    - `NotificationListener`: A fallback listener that analyzes notifications for ad detection.
- **Util Layer (`.util`)**: Decoupled detection logic (`AdActionDetector`).

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Concurrency**: Kotlin Coroutines & Flow
- **Architecture Components**: ViewModel, Lifecycle
- **Android APIs**: MediaSession, NotificationListenerService, AudioManager

## 🚀 Getting Started

### Prerequisites

- Android Device/Emulator (API 26+)
- Spotify App installed

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/AdMuterApp.git
   ```
2. Open the project in Android Studio (Ladybug or newer).
3. Build and Run the app on your device.

### Permissions Required

To function correctly, the app requires:
1. **Notification Access**: To detect ads via Spotify's playback notification.
2. **Modify Audio Settings**: To mute/unmute the music stream.
3. **Foreground Service**: To keep monitoring while the app is in the background.

## 📖 How It Works

1. **Detection**: The app listens for `onMetadataChanged` events from Spotify.
2. **Analysis**: If the track title contains "Advertisement" or if the "Skip Next" action is disabled by Spotify, the app identifies it as an ad.
3. **Action**: The `AdRepository` triggers the `AudioManager` to mute the `STREAM_MUSIC` channel.
4. **Resumption**: Once a normal song is detected, the audio is unmuted, and the "Songs Played" counter increments.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request or open an issue for bugs and feature requests.

---
*Disclaimer: This app is for educational purposes. Please support artists and services by considering premium subscriptions.*
