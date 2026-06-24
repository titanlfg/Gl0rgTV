# Gl0rgTV

Unofficial Android TV viewer for Kick streams. Gl0rgTV is not affiliated with Kick.

## Download

Latest sideload APK:

```text
https://titanlfg.github.io/Gl0rgTV
```

Direct APK:

```text
https://github.com/titanlfg/Gl0rgTV/releases/latest/download/Gl0rgTV.apk
```

## Install On Android TV

1. Open Downloader on Android TV.
2. Enter `titanlfg.github.io/Gl0rgTV`.
3. Allow installs from Downloader when Android asks.
4. Install and open Gl0rgTV.

Future updates can be installed from Gl0rgTV Settings with Check Update.

## Build

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleRelease
```

Release signing uses a local `keystore.properties` file that is not committed.

## Known Limits

Kick website changes can break login, followed streamers, stream discovery, or playback fallback.
