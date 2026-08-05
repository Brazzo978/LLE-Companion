# L.L.E Companion

Small native Android companion for `com.codex.lle64`. L.L.E itself remains offline.

## Remote version file

Copy `distribution/LLE_VERSION.txt` to the root of the official repository branch used by `BuildConfig.VERSION_FILE_URL`. The file contains exactly one stable four-part version, for example:

```text
1.0.5.3
```

Companion checks this file when it opens. If the user enables notifications, WorkManager also checks it approximately once a day when the device is online and the battery is not low. APK downloads never start in background.

Release tags must follow this exact form:

- tag: `v1.0.5.3`

## Distribution

There is one Google Play-oriented build. Companion never downloads or installs APKs and does not declare `REQUEST_INSTALL_PACKAGES`. Its install/update action opens the matching official GitHub release in the device browser. This reduces Play policy risk but does not guarantee approval.

The app includes two lightweight, looping tutorials: one for downloading the stable APK from GitHub and one fullscreen installation walkthrough with playback controls.

## Local build

Use JDK 17. Optional project-local Gradle and Android state can be configured like this:

```powershell
$env:JAVA_HOME = '<path-to-jdk-17>'
$env:GRADLE_USER_HOME = "$PWD\.gradle-user"
$env:ANDROID_USER_HOME = "$PWD\.android-user"
.\gradlew.bat test lint assembleDebug
```

No release signing or publishing configuration is included.
