# Prompt-Pad

Minimal offline dark launcher for the Unihertz Titan 2 Elite (4:3, 4.04", top-left camera cutout).
Package `com.hermes.promptpad`. Kotlin + Compose, single activity, ~1.4k LOC, zero AI, zero network.

## Build
- `source ~/toolchain/env.sh` first.
- Debug: `./gradlew :app:assembleDebug`; unit tests: `./gradlew :app:testDebugUnitTest`.
- Release: export KEYSTORE_FILE / KEYSTORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD from Bitwarden keys
  `PROMPTPAD_RELEASE_*` (keystore also stored base64 as `PROMPTPAD_RELEASE_KEYSTORE_B64`), then
  `./gradlew :app:assembleRelease`. R8-minified, signed, non-debuggable.
- Test AVD `promptpad43` = 1080x1440 (4:3), android-35 google_apis x86_64, hw keyboard on.

## Layout contract
Two-line lowercase date -> weather/battery caption -> two 57dp bordered glance rows -> four rounded bottom shortcuts. Katapult-derived Lato typography and black/white/orange (`#FC7703`) palette. Peak date target and four shortcuts are app-picker configurable and all gain orange borders in edit mode; clocks open the system Clock alarm view. Right-aligned Peak variants have an 8dp top gap so curved corners do not clip the clock; left-aligned variants retain safe top inset and 14dp spacing. Hub, Notes, Agenda and To Do use 26sp centered titles and safe-drawing top insets.

## Icon contract
Use the bundled Katapult monochrome icon set for mapped apps and system shortcuts. Note and To Do intentionally retain their PromptPad Material icons. Managed-profile mapped apps use the same bundled artwork with Android's work badge. Drawer icons use one fixed outer size and remain hidden until search text is entered.

## Gestures
Swipe up = profile-aware drawer (personal + managed work apps, badged icons, auto-focused bottom search). Swipe right = Hub. Swipe left = Settings. Long-press blank Home area = shortcut edit mode. Double-tap blank Home area = sleep when enabled. Hidden status bar is transiently revealed by a top-edge swipe. Physical key long-press on Home = mapped app launch.

## Removed surfaces
Activity tracking and Focus/Monk restriction modes are intentionally purged, including their permissions, preferences, settings, service, resources, and tests.

## Deliberate simplifications (ponytail)
- No weather feed: device is offline, the widget shows `—°` behind a toggle.
- Hub excludes notification group summaries so messaging apps contribute one actionable row.
- Notes/To-Do persist as JSON in SharedPreferences; completed To-Dos stay struck through until Clear. Swap for Room only if lists get large.
- Clock tile fires the system `SHOW_ALARMS` intent — spec says use the device clock app.
