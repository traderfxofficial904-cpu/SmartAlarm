# Smart Alarm — Android project + GitHub Actions APK build

## Build APK using only your phone

1. Download and extract this ZIP on your phone.
2. Open https://github.com and sign in (create an account if needed).
3. Create a **new repository**. Give it a name such as `SmartAlarm`; choose **Public** or **Private**.
4. Open the extracted `SmartAlarmAndroid` folder in your phone's file manager.
5. Upload the **contents inside** `SmartAlarmAndroid` to the repository root. Important: upload the `.github` folder too. If your file manager hides dot-folders, enable "show hidden files", or upload `.github/workflows/build-apk.yml` using GitHub's web file editor and create the folders `.github/workflows`.
6. Make sure `app`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, and `.github` are at the repository's top level (not inside another nested `SmartAlarmAndroid` folder).
7. Open the repository's **Actions** tab. Select **Build Smart Alarm APK**.
8. If it asks, press **Run workflow** → **Run workflow**.
9. Wait until the workflow finishes with a green check mark.
10. Open that completed run, scroll to **Artifacts**, and download `SmartAlarm-debug-apk`.
11. Extract the downloaded artifact ZIP. Tap `app-debug.apk` to install it. Allow installation from your browser/file manager if Android asks.

The workflow also starts automatically when you push a commit to the `main` branch.

## Notes
- GitHub Actions builds the debug APK on GitHub's cloud runner; you do not need Android Studio or a computer.
- The workflow uses JDK 17 and Gradle 8.9.
- This is a debug APK, not a Play Store release-signed APK.
- After installing, grant notifications, exact alarm ("Alarms & reminders"), and full-screen alarm permissions where Android offers them. Set battery use to Unrestricted and test an alarm with the screen locked.
- Android may restrict alarms after Force Stop, permission revocation, or manufacturer-specific battery management.
