# Gl0rgTV Sideload Install

Gl0rgTV is an unofficial Kick viewer for Android TV. It is not affiliated with Kick.

## Install

1. Build or download `app-debug.apk` for current sideload testing.
2. Copy it to the Android TV device or install through `adb`.
3. Enable installing apps from unknown sources for the file manager or `adb`.
4. Install the APK.
5. Open Gl0rgTV from the Android TV launcher.

## ADB Install

```powershell
adb install app/build/outputs/apk/debug/app-debug.apk
```

`assembleRelease` currently produces `app-release-unsigned.apk`. Sign it with your own release key before distributing release builds to other people.

## Login

Login opens Kick in an embedded WebView. Gl0rgTV does not store Kick passwords. Sign out from Settings clears the local WebView session.

## Known Limit

Kick website changes can break login, followed streamers, stream discovery, or playback fallback.
