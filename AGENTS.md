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

## Layout contract (matches the reference screenshot)
Two-line lowercase date -> weather/battery caption -> 24-dot activity bar -> two bordered glance rows
(next event, first open task + count badge) -> 4x2 tile grid. Accent `#f26609`, bg `#000000`,
surfaces `#0E0E0E`, borders `#2A2A2A`.

## Gestures
Swipe up = drawer (auto-focused search, bottom-anchored). Swipe right = Hub. Swipe left = Settings.
Long-press home = tile edit mode. Physical key long-press on home = mapped app launch.

## Deliberate simplifications (ponytail)
- No weather feed: device is offline, the widget shows `—°` behind a toggle.
- Notes/To-Do persist as JSON in SharedPreferences; swap for Room only if lists get large.
- Clock tile fires the system `SHOW_ALARMS` intent — spec says use the device clock app.
- Seal is a one-way pref flag; the spec's "factory reset only" holds because nothing in-app can clear it.
- Focus/Monk blocking is an AccessibilityService, not DeviceAdmin: same block, no device-owner setup.
