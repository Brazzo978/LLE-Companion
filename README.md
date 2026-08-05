# L.L.E Companion

Small native Android companion for `com.codex.lle64`. So L.L.E itself remains offline.

## Remote version file

Checks https://raw.githubusercontent.com/Brazzo978/L.L.E-Legacy-Lockscreen-Effects/refs/heads/codex/lle-unified/LLE_VERSION.txt for the current version.

```text
1.0.5.3
```

Companion checks this file when it opens. If the user enables notifications, WorkManager also checks it approximately once a day when the device is online and the battery is not low. APK downloads never start in background.

Release tags must follow this exact form:

- tag: `v1.0.5.3`

## Distribution

This is a Google Play-oriented build. Companion never downloads or installs APKs it just helps the user to do so , while doing update check.

The app includes two lightweight, looping tutorials: one for downloading the stable APK from GitHub and one fullscreen installation walkthrough.

