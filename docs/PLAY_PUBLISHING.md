# Publishing L.L.E Companion on Google Play

## Permanent identifiers

- Play package: `com.codex.lle.companion`
- Minimum SDK: 23
- Target SDK: 36
- The package must never be renamed after the first Play artifact is uploaded.
- Every Play upload must use a new, monotonically increasing `versionCode`.

## Policy decision required before submission

The full GitHub build guides users to download and install another APK. That flow has a
material Google Play rejection risk. Before uploading to Play, choose one of these paths:

1. Recommended global Play variant: remove external APK download links and sideloading
   tutorials from the Play artifact.
2. Eligible US distribution: enroll in Google's External Content Links program and meet
   its destination, disclosure, reporting, and support requirements.
3. Keep the complete Companion outside Google Play.

Do not hide or remotely disable behavior only during review. The reviewed app and the app
served to users must behave identically.

## Signing model

Use Play App Signing with two signing roles:

- L.L.E release key: held locally and used for both the GitHub APK and the `.aab`
  uploaded to Play.
- Play app-signing key: generated and protected by Google, used for APKs delivered to users.

The local keystore and alias are:

`Documents\LLE-signing-private\lle-release.p12`

Alias: `lle-release`

Never commit a keystore, password, `keystore.properties`, APK, or AAB.
Store the keystore and password in two separate secure backup locations.

## Build the signed Play bundle

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
.\scripts\build-play-bundle.ps1
```

The script requests the password without echoing it, runs tests and lint, creates the
signed GitHub APK and Play AAB, verifies their signatures, prints their SHA-256 hashes,
and removes signing secrets from the environment.

## Play Console sequence

1. Verify the Play Console developer identity and contact information.
2. Create an app named `L.L.E Companion`.
3. Select app, free, default language, contact email, and accept required declarations.
4. Complete the store listing, privacy-policy URL, Data safety, ads, app access, target
   audience, content rating, and every other item under App content.
5. Open Internal testing and create a release.
6. Keep the recommended Google-generated Play app-signing key.
7. Upload the signed `app-release.aab`.
8. Confirm Play reports package `com.codex.lle.companion` and the intended version code.
9. Add internal testers and publish the internal release.
10. Check Android developer verification and confirm the Play signing key is registered.
11. Keep the externally distributed `com.codex.lle.companion`, `com.codex.lle`, and
    `com.codex.lle64` package names registered with L.L.E's stable signing certificate.
12. Review the pre-launch report and resolve every crash, ANR, security, and accessibility
    issue relevant to the app.
13. If the developer account is a new personal account subject to testing requirements,
    run the required closed test before requesting production access.
14. Submit the production-access request and answer using actual test evidence.
15. Create the production release only after access is granted and all policy declarations
    remain accurate.

## Before every upload

- Increment `versionCode`.
- Keep `versionName` user-facing and stable.
- Build from a clean worktree and the intended commit.
- Run tests and lint.
- Verify the APK and AAB signatures and SHA-256 hashes.
- Preserve the upload keystore and password backups.
- Confirm the store listing and Data safety answers still match the app's behavior.
