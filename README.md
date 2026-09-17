# L.L.E Companion

L.L.E Companion is the optional update and setup helper for the public ARM64
edition of [L.L.E — Legacy Lockscreen Effects](https://github.com/Brazzo978/L.L.E-Legacy-Lockscreen-Effects).
It keeps L.L.E itself offline while giving users a small, native place to check
stable releases, receive optional update notifications, and replay visual
download and installation tutorials.

## Google Play closed test

The first `1.0.0` release has been published to the Google Play closed-test
track. We need at least 12 genuine Android testers who can install and use the
app, report problems, and remain opted in for at least 14 consecutive days.

Join in this order, using the same Google account for every step and for the
Play Store on the Android device:

1. [Join the L.L.E Companion Google Group](https://groups.google.com/g/lle-companion-testers).
2. [Opt in to the closed test](https://play.google.com/apps/testing/com.codex.lle.companion).
3. [Install L.L.E Companion from Google Play](https://play.google.com/store/apps/details?id=com.codex.lle.companion).
4. Keep the app installed and remain opted in for at least 14 days.

Joining the Google Group alone does not count: each tester must also open the
opt-in page and explicitly become a tester. Please send feedback through
[GitHub Issues](https://github.com/Brazzo978/LLE-Companion/issues) or
`supporto@legacylockscreeneffects.app`.

## What Companion does

- Reads the installed version of the exact public ARM64 package
  `com.codex.lle64`.
- Compares it with the latest public stable version published by the L.L.E
  project.
- Ignores private tester packages so unreleased builds never affect normal
  users.
- Offers optional, battery-aware daily update notifications.
- Shows a looping GitHub download tutorial before opening the official release.
- Shows a return prompt and a fullscreen, replayable installation tutorial.
- Opens external pages only after an explicit user action.

Companion never downloads or installs an APK. It opens the matching official
GitHub release in the device browser and leaves downloading and Android's
installation confirmation under user control.

## Stable-version contract

The current stable version is read over HTTPS from:

[`LLE_VERSION.txt`](https://raw.githubusercontent.com/Brazzo978/L.L.E-Legacy-Lockscreen-Effects/refs/heads/main/LLE_VERSION.txt)

```text
1.0.6.5
```

Companion checks the file when the app opens. When notifications are enabled,
WorkManager also schedules a unique check approximately once a day with two
constraints:

- the device must be online;
- the battery must not be low.

The schedule is opportunistic rather than an exact alarm, so Android can defer
it to protect battery life. APK downloads never start in the background.

Public L.L.E release tags must match the version file exactly:

```text
v1.0.5.3
```

## Privacy

L.L.E Companion has no ads, analytics, telemetry, crash-reporting SDK, or user
account. It does not read wallpapers, screenshots, media, contacts, location,
or Accessibility data.

Its own network activity is limited to reading the static L.L.E version file
from GitHub. The official release page opens in the user's browser only after
confirmation. See the [privacy policy](PRIVACY_POLICY.md) and the shorter
[technical privacy summary](docs/PRIVACY.md).

## Current Android build

| Property | Value |
|---|---|
| Application ID | `com.codex.lle.companion` |
| Version | `1.0.0` (`versionCode 1`) |
| Minimum Android version | Android 6.0 / API 23 |
| Target SDK | 36 |
| L.L.E package queried | `com.codex.lle64` |

The Play release is distributed as an Android App Bundle. Release builds must
remain signed through the established L.L.E upload-key workflow, and every Play
upload must use a new monotonically increasing `versionCode`.

## Maintainer documentation

- [Google Play publishing checklist](docs/PLAY_PUBLISHING.md)
- [Privacy summary](docs/PRIVACY.md)
- [Public privacy policy](PRIVACY_POLICY.md)

Before publishing a new Companion build, verify that the store declarations,
privacy statements, screenshots, tutorials, and external navigation still
match the actual app behaviour.
