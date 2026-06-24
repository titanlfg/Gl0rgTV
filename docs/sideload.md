# Gl0rgTV Sideload Install

Gl0rgTV is an unofficial Kick viewer for Android TV. It is not affiliated with Kick.

## Install

1. Download the latest `Gl0rgTV-v0.1.0.apk` from GitHub Releases.
2. Copy it to the Android TV device or install through `adb`.
3. Enable installing apps from unknown sources for the file manager or `adb`.
4. Install the APK.
5. Open Gl0rgTV from the Android TV launcher.

## ADB Install

```powershell
adb install app/build/outputs/apk/release/app-release.apk
```

Downloader URL:

```text
https://titanlfg.github.io/Gl0rgTV
```

Direct APK URL:

```text
https://github.com/titanlfg/Gl0rgTV/releases/latest/download/Gl0rgTV.apk
```

Release builds are signed with the local Gl0rgTV release keystore. Keep `C:\Users\Titan\Documents\Keys\gl0rgtv-release.jks` backed up.

## Login

Login opens Kick in an embedded WebView. Gl0rgTV does not store Kick passwords. Sign out from Settings clears the local WebView session.

## Known Limit

Kick website changes can break login, followed streamers, stream discovery, or playback fallback.
