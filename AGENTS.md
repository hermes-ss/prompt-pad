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
Two-line lowercase date -> weather/battery caption -> two 57dp bordered glance rows -> four rounded bottom shortcuts. Katapult-derived Lato typography and black/white/orange (`#FC7703`) palette. Peak dates open the system calendar; clocks open the system Clock alarm view. Right-aligned Peak variants have an 8dp top gap so curved corners do not clip the clock; left-aligned variants retain safe top inset and 14dp spacing. Hub, Notes, Agenda and To Do titles are top-centered with no in-app back arrow; their content begins after the 48dp title row.

## Icon contract
Use the bundled Katapult monochrome icon set for mapped apps and system shortcuts. Note and To Do intentionally retain their PromptPad Material icons. Owner-profile apps may use bundled artwork; managed-profile apps keep Android's badged launcher icon so work-profile identity is not lost. Drawer icons use one fixed outer size.

## Gestures
Swipe up = profile-aware drawer (personal + managed work apps, badged icons, auto-focused bottom search). Swipe right = Hub. Swipe left = Settings. Long-press blank Home area = shortcut edit mode. Double-tap blank Home area = sleep when enabled. Hidden status bar is transiently revealed by a top-edge swipe. Physical key long-press on Home = mapped app launch.

## Removed surfaces
Activity tracking and Focus/Monk restriction modes are intentionally purged, including their permissions, preferences, settings, service, resources, and tests.

## Deliberate simplifications (ponytail)
- No weather feed: device is offline, the widget shows `—°` behind a toggle.
- Notes/To-Do persist as JSON in SharedPreferences; swap for Room only if lists get large.
- Clock tile fires the system `SHOW_ALARMS` intent — spec says use the device clock app.
