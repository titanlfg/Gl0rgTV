# Gl0rgTV UI Redesign + Login Cleanup Plan

Spec: ../specs/2026-06-26-ui-redesign-and-login.md
Build cannot run in this env (egress blocks maven.google.com). Verify by review;
maintainer compiles locally.

## Theme system
- [x] Create `ui/Theme.kt`: `Gl0rgColors` data class (background, surface,
      surfaceAlt, surfaceFocus, text, muted, accent, accentText, secondary,
      live, outline); `ThemeOption` enum (Kick, Midnight, Mono) with preset
      colors + display name; `LocalGl0rgColors`; `Gl0rgTheme(option, content)`.
- [x] Convert the 6 color symbols in `ui/TvUi.kt` to `@Composable @ReadOnlyComposable`
      properties resolving from `LocalGl0rgColors.current`.
- [x] Create `ui/ThemePreferences.kt`: SharedPreferences read/write of theme key.
- [x] `MainActivity`: load theme, hold state, wrap in `Gl0rgTheme`; align
      MaterialTheme colors with active theme.

## Components (TvUi.kt)
- [x] Refine `PreviewCard`: gradient scrim, LIVE badge, viewer pill, rounder corners.
- [x] Add `HeroFeature`: large featured live card w/ preview-on-focus + Watch CTA.
- [x] Add `CategoryChip` (pill) replacing TvButton for categories.
- [x] Add `SectionHeader`; polish side nav rail + wordmark.

## Home restructure
- [x] `Gl0rgTvApp`: compute hero stream from favorites(live)→liveStreams; pass to Home.
- [x] `HomeScreen`: hero at top, then Followed / Top Streamers / Categories chips /
      {Category} Live rows; remove redundant "Tools" block.

## Settings
- [x] `Gl0rgTvApp`: add `themeOption` + `onThemeChange` params (from MainActivity).
- [x] `SettingsScreen`: theme picker row; reflect selected theme.

## Login cleanup
- [x] `LocalCookieLoginServer`: add `LoginPhase` enum + `onPhase` callback;
      forgiving cookie normalize (strip `Cookie:`, trim, accept value/header,
      decode `+`); redesigned dark mobile-first helper HTML + success/error pages.
- [x] `Gl0rgTvApp`: track `loginPhase` state from server callbacks.
- [x] `LoginScreen`: render phase status chip, larger QR, themed step cards.

## Polish + verify
- [x] `SearchScreen`/`ChannelScreen`/`PlayerScreen`: confirm token migration reads
      cleanly; minor spacing/focus polish.
- [x] Align `res/values/colors.xml` + `themes.xml` base with default theme.
- [x] Self-review full diff for compile-safety; update README login wording if needed.
- [x] Commit + push to `claude/soundtv-ui-login-flow-azdywz`.
