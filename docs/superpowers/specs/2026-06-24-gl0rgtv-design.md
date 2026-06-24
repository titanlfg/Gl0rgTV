# Gl0rgTV Design

## Summary

Gl0rgTV is a sideloaded Android TV app for watching Kick streams with a S0undTV-like living-room experience. It is a self-contained APK with no separate backend. It uses native TV UI for browsing, search, favorites, recents, settings, and playback. It uses an embedded Kick WebView session for login-dependent features such as followed streamers.

The app is unofficial and not affiliated with Kick. The first release targets sideload distribution, not Play Store submission.

## Goals

- Provide a remote-friendly Android TV app for Kick live streams.
- Support browsing live/trending streams, channel search, local favorites, and recent channels.
- Support user login inside the app through Kick's website, then attempt followed-streamer discovery through session-backed Kick web requests. If the session-backed path fails, show a clear unavailable state and keep local favorites working.
- Prefer native playback with Media3/ExoPlayer when a direct stream URL can be discovered.
- Fall back to an embedded Kick player when native playback is unavailable.
- Keep unofficial Kick integration isolated so broken parsers/session handling can be fixed or replaced without rewriting the UI.
- Produce a release APK suitable for sideloading.

## Non-Goals

- No Play Store release in v1.
- No separate backend in v1.
- No official OAuth token exchange in v1, because Kick requires a client secret and shipping that secret in an APK is unsafe.
- No chat in v1.
- No chat sending, moderation, subscriptions, rewards, or creator tooling in v1.
- No stream proxying or media relay.

## Main Risks

Gl0rgTV depends on unofficial Kick website/session behavior. Kick can change pages, embedded player behavior, cookies, or endpoint responses at any time. Login, followed channels, stream discovery, and native playback may break.

To control that risk, all Kick-specific scraping, endpoint calls, session reads, and stream URL extraction live behind narrow interfaces. UI code never reads raw Kick HTML, cookies, or undocumented JSON directly.

## Architecture

The app is a native Android TV Kotlin project.

Core stack:

- Kotlin
- Jetpack Compose for TV
- AndroidX Media3/ExoPlayer
- WebView fallback player
- OkHttp for network requests
- kotlinx.serialization for structured JSON
- Jsoup for Kick HTML parsing
- DataStore for settings
- Room for local favorites and history

Primary modules:

- `app`: Android entry point, navigation, dependency wiring.
- `ui`: TV screens and reusable focusable components.
- `player`: ExoPlayer wrapper, WebView fallback player, quality selection.
- `kick`: Kick client, parsers, session provider, stream resolver.
- `library`: local favorites and recents.
- `settings`: persisted user preferences.

## Kick Integration

`KickSessionProvider` owns WebView login state. The login screen opens Kick in an embedded WebView. After login, Android `CookieManager` stores Kick cookies. The app uses those cookies for Kick web requests that need user state.

`KickClient` exposes stable app-level operations:

- `getLiveStreams()`
- `searchChannels(query)`
- `getChannel(slug)`
- `getFollowedChannels()`
- `resolvePlayableStream(slug)`

Implementations may use public Kick pages, embedded player pages, undocumented JSON embedded in HTML, or other browser-visible endpoints. The rest of the app only sees typed models.

If followed channels cannot be reliably read from the session-backed Kick website, v1 will show local favorites as the primary personalized row and mark followed-channel support as unavailable.

## Screens

Home:

- Followed Live row when logged in and available.
- Favorites row.
- Trending or live streams row.
- Recent channels row.
- Empty states for guest users and offline favorites.

Search:

- TV keyboard input.
- Results grouped by live channels and channels/categories when data is available.
- Remote-friendly focus order and quick open behavior.

Channel:

- Channel avatar/banner where available.
- Live status, title, category, viewer count, mature flag, and thumbnail. Missing fields render as unknown or hidden, never as fake values.
- Watch button.
- Add/remove local favorite button.

Player:

- Native ExoPlayer first.
- Embedded Kick player fallback.
- Remote controls for play/pause, back, quality, captions if exposed, and player mode.
- Mature-content gate before playback when metadata marks stream mature.

Login:

- Embedded Kick WebView.
- Clear explanation that login is unofficial and may break.
- Sign out clears WebView cookies and local session state.

Settings:

- Start screen preference.
- Autoplay toggle.
- Mature-content filter/gate.
- Preferred player mode: auto, native first, WebView only.
- Preferred quality, applied only when the stream resolver exposes selectable variants.
- Clear cache.
- Clear login.
- About and version.

## Data Storage

Local data:

- Favorite channel slug, display name, avatar URL, last known live state.
- Recent channel slug, display name, thumbnail/avatar URL, last watched time.
- Settings values.
- Lightweight cached Kick responses and thumbnails with a short TTL. No authenticated HTML pages are cached.

Sensitive data:

- The app does not store Kick passwords.
- WebView cookies remain in Android WebView/CookieManager storage.
- Sign out clears Kick cookies and app session state.

## Error Handling

Network failures show retryable TV-friendly errors.

Parser failures show degraded UI instead of crashing. For example, if viewer count parsing fails, channel cards can still show title/thumbnail.

Playback resolution failure automatically falls back to WebView player when enabled. If both native and fallback playback fail, the player shows a retry action and a link-style action to reopen with fallback mode.

Login/followed-channel failures do not block guest use. The app falls back to local favorites and search.

## Release

Distribution is sideload-first:

- Debug APK for local testing.
- Signed release APK for GitHub Releases or direct download.
- Basic sideload instructions.
- Clear unofficial-app disclaimer.

App name: Gl0rgTV.

App icon uses Kick green as the primary color.

Package name will use a unique namespace, for example `tv.gl0rg.kick`.

## Testing

Unit tests:

- Kick parsers with saved HTML/JSON fixtures.
- Stream resolver behavior for direct URL, fallback required, and unavailable cases.
- Favorites and recents repository.
- Settings persistence.

UI tests:

- Home row focus navigation.
- Search input and result navigation.
- Channel page watch/favorite actions.
- Settings toggles.

Manual smoke tests:

- Fresh install on Android TV emulator.
- Sideload release APK.
- Browse live streams.
- Search and open channel.
- Play stream with native player or fallback.
- Log in through WebView.
- Verify followed streamers row if Kick page/session data allows it.
- Sign out clears session.

## Future Work

- Official auth through an optional backend if the user later accepts a hosted service.
- Chat read overlay.
- Chat sending.
- Better emote rendering.
- Notifications for favorite channels going live.
- Auto-update checker for sideload releases.
