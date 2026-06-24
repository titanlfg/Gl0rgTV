# Gl0rgTV

Unofficial Android TV viewer for Kick streams. Gl0rgTV is not affiliated with Kick.

## Download

Latest sideload APK:

```text
https://github.com/titanlfg/Gl0rgTV/releases/download/v0.1.0/Gl0rgTV-v0.1.0.apk
```

## Install On Android TV

1. Open Downloader on Android TV.
2. Enter the APK URL above.
3. Allow installs from Downloader when Android asks.
4. Install and open Gl0rgTV.

## Build

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleRelease
```

Release signing uses a local `keystore.properties` file that is not committed.

## Known Limits

Kick website changes can break login, followed streamers, stream discovery, or playback fallback.
