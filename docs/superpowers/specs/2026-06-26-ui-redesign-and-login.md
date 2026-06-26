# Gl0rgTV UI Redesign + Login Cleanup Design

Date: 2026-06-26. Reference model: S0undTV (github.com/S0und/S0undTV), Twitch TV client.

## Summary

Two changes:
1. Full visual redesign of the TV UI to feel modern and closer to S0undTV:
   theme system (multiple themes), restructured Home with a featured hero,
   refined cards/rails/chips, consistent focus states and typography.
2. Cleaner Kick login flow built on the existing local-webserver approach:
   redesigned mobile-first helper page, live TV-side status feedback, and more
   forgiving cookie parsing. Kept cookie-based (preserves existing web scraping).

## Constraints discovered

- Kick session cookies are `httpOnly` → unreadable by helper-page JS. So a paste
  step cannot be fully removed without an on-device WebView (rejected: TV typing).
  Kick OAuth 2.1+PKCE exists but redirect-back-to-TV + API coverage make it a
  large risky rebuild — out of scope (user chose "clean up cookie flow").
- Build cannot run in this environment: `maven.google.com`/`dl.google.com` are
  blocked by egress policy, so AndroidX/AGP deps don't resolve. Verification is
  by careful authoring + review; the maintainer builds locally (`gradlew.bat`).

## Goals

- Theme system with 3 presets (Kick, Midnight, Mono), persisted, switchable in Settings.
- Home: featured hero (live preview on focus) + refined rows + category chips.
- Reusable, polished components: HeroFeature, PreviewCard, CategoryChip, SectionHeader, side nav.
- Login: redesigned dark helper page, TV-side phase status (Waiting/Verifying/Linked),
  forgiving cookie parsing, larger QR.
- No regressions to existing navigation, playback, favorites, search, update.

## Non-Goals

- Kick OAuth/PKCE rebuild. Chat. New scraped data sources. Compose UI tests.

## Architecture

- `ui/Theme.kt` (new): `Gl0rgColors` token set, `ThemeOption` enum + presets,
  `LocalGl0rgColors` CompositionLocal, `Gl0rgTheme` provider. The existing 6
  color symbols (`KickGreen`, `Gl0rgBackground`, `Gl0rgPanel`, `Gl0rgPanelSoft`,
  `Gl0rgText`, `Gl0rgMuted`) become `@Composable @ReadOnlyComposable` properties
  resolving from `LocalGl0rgColors.current` → zero call-site churn, full theming.
- `ui/ThemePreferences.kt` (new): SharedPreferences load/save of selected theme.
  Lightweight; avoids wiring async DataStore into the app (SettingsRepository is
  currently unused by the app shell).
- `MainActivity`: holds theme state, wraps content in `Gl0rgTheme`.
- `Gl0rgTvApp`: plumbs `themeOption` + `onThemeChange` to Settings; computes the
  Home hero from existing data (top live favorite, else top live stream).
- Login: `LocalCookieLoginServer` gains a `LoginPhase` (Waiting/InvalidCookie/Linked)
  callback + redesigned HTML + reused cookie normalizer; `LoginScreen` renders the
  phase; `Gl0rgTvApp` tracks phase state.

Component boundaries unchanged: UI consumes typed repositories/clients only; Kick
scraping/session stays behind existing interfaces.

## Risks

- No compiler here → typo/compile risk. Mitigation: keep public composable
  signatures stable, mechanical token migration, match existing imports.
- Theme getters as top-level composable properties must only be referenced in
  composable scope. Verified: all 60 usages are in `@Composable` bodies.
