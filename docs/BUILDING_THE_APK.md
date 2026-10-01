# Building Easy Español into an APK

This guide takes the repository from a zip file to an app on your phone, using Android Studio.

## First, what builds this app?

Easy Español is a **Kotlin** app using **Jetpack Compose**. It is built by **Gradle**, which comes inside Android Studio. You don't install or run Gradle yourself; Android Studio does it when you press its buttons.

You may be thinking of **Buildozer**. That's a different tool, for apps written in **Python** with Kivy, and it isn't used here. If you ever see a `buildozer.spec` file, you're looking at a Python project, not this one.

There are two ways to get the app onto a phone:

- **Route A: install straight from Android Studio.** This is the easiest, and needs no APK file. Use it for your own phone, plugged in by USB.
- **Route B: build an APK file.** You get a file to copy to any phone or send to someone else.

Both start with the same setup.

---

## Setup (once)

### 1. Install Android Studio

- The copy you built EasyJapanesey with will do. This project uses the same build settings.
- If you don't have it, download it from <https://developer.android.com/studio> and accept the defaults in the installer.
- The first launch downloads the Android SDK, which is a few gigabytes. Let it finish.

### 2. Unzip the project somewhere with a short path

- Unzip `EasyEspanol.zip`. You'll get a folder called `EasyEspanol` containing `settings.gradle.kts`, `gradlew` and an `app` folder.
- **Windows:** use a short path such as `C:\Projects\EasyEspanol`.
  - Avoid very deep folders: Windows has a path-length limit that can break builds.
  - Avoid OneDrive-synced folders like Documents or Desktop. OneDrive locking build files is a common cause of baffling errors.

### 3. Open it in Android Studio

1. Open Android Studio. On the welcome screen click **Open**. If a project is already open, use **File → Open…**.
2. Select the **`EasyEspanol`** folder, the one that directly contains `settings.gradle.kts`.
   - Don't select the zip file.
   - Don't select the `app` folder inside it.
3. Click **OK**. If asked whether to trust the project, choose **Trust Project**.

### 4. Wait for the Gradle sync

- A progress bar runs at the bottom right while Android Studio downloads what the project needs and sets it up. This is the "Gradle sync".
- **The first time takes several minutes** and needs an internet connection. Later opens are much quicker.
- It has finished when the progress bar disappears and the project tree on the left shows `app` with a green dot or Android icon.

If Android Studio offers to **upgrade the Android Gradle Plugin**, you can dismiss it. The project works as it is.

If a yellow or red bar says something is missing (for example an SDK platform), click the blue link in the message. It installs what's needed. Then use **File → Sync Project with Gradle Files**.

---

## Route A: install straight onto your phone

This builds the app and installs it in one go, so you never handle an APK file.

### Turn on USB debugging (once per phone)

1. On the phone, open **Settings → About phone** and tap **Build number** seven times. On Samsung phones it's under **Settings → About phone → Software information**. A message says you're now a developer.
2. Go back to **Settings**. Find **Developer options**, usually under **System**, and turn on **USB debugging**.

### Run the app

1. Plug the phone into the computer.
   - If the phone asks "Allow USB debugging?", tick **Always allow from this computer** and tap **Allow**.
   - If it asks what the USB connection is for, choose **File transfer**.
2. Choose your phone in the device drop-down in Android Studio's top toolbar, next to the green ▶ button.
3. Click the green **▶ Run 'app'** button (Shift+F10 on Windows, Ctrl+R on Mac).

The first build takes a minute or two. The app then opens on the phone and stays installed, with the Spanish flag icon, after you unplug it.

You can also use the **emulator**, the virtual phone inside Android Studio: pick it in the same drop-down instead of your phone. To do it without a cable, turn on **Wireless debugging** in Developer options, then in Android Studio open **Device Manager → Pair Devices Using Wi-Fi**.

---

## Route B: build an APK file

### Build it

1. In the menu bar choose **Build → Generate App Bundles or APKs → Generate APKs**.
   - Older versions of Android Studio call this **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
   - Can't find either? Press **Ctrl+Shift+A** (Cmd+Shift+A on Mac), type **Build APK** and press Enter. This "Find Action" box finds any menu item by name, which is handy because menus move between versions.
2. Wait for the build. A pop-up at the bottom right says the APK was generated successfully. Click **locate** in that pop-up to open the folder.
3. The file is:

   ```
   EasyEspanol/app/build/outputs/apk/debug/app-debug.apk
   ```

   You can rename it to `EasyEspanol.apk`. The name doesn't matter.

**If the menus misbehave**, build from Android Studio's **Terminal** tab (bottom of the window). This produces the same file:

```
./gradlew assembleDebug        (Mac / Linux)
gradlew assembleDebug          (Windows)
```

### Install the APK on a phone

1. Copy the APK to the phone. You can drag it into the phone's **Download** folder over USB, upload it to Google Drive, or email it to yourself.
2. On the phone, open the file, for example from the **Files** app or the Downloads notification.
3. Android will say this source isn't allowed to install apps. Tap **Settings**, turn on **Allow from this source**, and go back.
4. Tap **Install**.
5. Google Play Protect may warn about an "unknown developer" or say it hasn't seen the app before. That's normal for any app you build yourself. Tap **More details → Install anyway**.

---

## Debug and release copies

The APK above is a **debug** build. Android Studio signs it automatically with a debug key kept on your computer. For an app you use yourself, that's all you need.

A **release** build is signed with your own key, and a release APK is a bit faster and smaller. Make one with **Build → Generate Signed App Bundle or APK…**:

1. Choose **APK**, then click **Next**.
2. Click **Create new…** to make a keystore file. Choose a password, and fill in at least your name.
3. Choose the **release** build type, then click **Create**. The APK appears in `EasyEspanol/app/release/`.

**Keep the keystore file and its password safe.** Every future update must be signed with the same key.

Each installed copy is tied to the key it was signed with, and that matters when you update:

- **Debug and release don't mix.** You can't install a release APK over a debug one, or the other way round. Android says "App not installed" or "package conflicts with an existing package".
- **Different computers mean different keys.** Each computer has its own debug key, so a debug build from a second computer won't install over one from the first.
- **The only fix is to uninstall the old copy.** That deletes the progress saved on the phone.

So pick one route, debug or release, on one computer, and stick with it.

---

## Updating the app later

When you change the phrases (see `docs/DATA_GUIDE.md`) or the code:

1. Run `python tools/validate.py` to check the data.
2. Open `app/build.gradle.kts` and add 1 to `versionCode`, for example `versionCode = 2`. You can also change `versionName`, which is just the label people see.
3. Build again with Route A or Route B and install over the top.

Progress on the phone is kept, because it's saved against the phrase ids, which never change.

---

## The app icon

The icon is the Spanish flag: the red–gold–red civil version, without the coat of arms, which turns into a blur at icon size. It's an *adaptive icon*, made of vector drawings, so it stays sharp at every size and takes whatever shape your phone uses (circle, squircle and so on). On Android 13 and later it also has a single-colour version for when "themed icons" are switched on.

The files are:

```
app/src/main/res/drawable/ic_launcher_background.xml   the flag
app/src/main/res/drawable/ic_launcher_foreground.xml   empty layer
app/src/main/res/drawable/ic_launcher_monochrome.xml   themed-icon version
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml     ties the layers together
```

`docs/icon_preview.png` shows how it looks under different icon shapes.

**To use your own picture instead:**

1. Right-click the `app` folder in Android Studio and choose **New → Image Asset**.
2. Pick your image and adjust it in the preview.
3. Click **Next → Finish**. It overwrites the files above.

If the phone still shows the old icon after an update, restart the phone. Launchers cache icons.

---

## Colour schemes

### Choosing one in the app

Open **Settings → Colour scheme** and pick one of these:

- **Rojo y oro:** the default, red and gold like the flag.
- **Mediterráneo:** blues.
- **Olivo:** olive green and terracotta.
- **Match my wallpaper:** described in the next section.

**Settings → Light or dark** chooses light mode, dark mode, or following the phone.

### Why EasyJapanesey turned out brown

EasyJapanesey's theme file has this line in `ui/theme/Theme.kt`:

```kotlin
dynamicColor: Boolean = true,
```

On Android 12 and later, dynamic colour ignores the app's own colours and builds a scheme from the phone's **wallpaper**. This is Google's "Material You". Android Studio's emulator has a blue default wallpaper, so the app looked blue there. On your phone, the wallpaper produced brown.

To fix EasyJapanesey, change that line to `false`. The app then uses the colours in its `Color.kt` file, which are still Android Studio's template purples (`Purple40` and so on), so change those hex codes to the blues you want.

Easy Español only uses dynamic colour if you choose **Match my wallpaper** in Settings.

### Changing or adding colours in the code

Everything is in `app/src/main/java/com/example/easyespanol/ui/theme/`:

- **`Theme.kt`:** each palette is a list of hex colours, such as `primary = 0xFFAA151B`. `0xFF` means fully opaque, followed by the usual six-digit colour code. Each palette has a light version and a dark version.
  - The main ones are `primary` (buttons, progress bars), `primaryContainer` (the top bar), `secondaryContainer` (the level badges) and `background`.
  - To add a palette, add a line to `AppPalette` and a matching light/dark pair in `paletteScheme()`. It then appears in Settings automatically.
- **`Color.kt`:** the flag colours, and the nine colours used to link Spanish and English chunks. The dark-mode set is lighter so it stays readable on a dark background.

If you want a whole new scheme without picking every shade, Google's free **Material Theme Builder** (<https://material-foundation.github.io/material-theme-builder/>) makes one from a single colour you choose, and exports the values for Jetpack Compose.

---

## Troubleshooting

| Message or problem | What to do |
|---|---|
| `SDK location not found` | You opened the project some other way. Open it through Android Studio's **File → Open**, which creates the missing `local.properties` file automatically. |
| `Failed to find target android-36`, or anything about compile SDK 36 not being installed | **Tools → SDK Manager → SDK Platforms**. Tick the entry for **API level 36** (Android 16), then click **Apply**. |
| The project needs a newer Android Gradle Plugin, or a newer Android Studio | **Help → Check for Updates** (on a Mac: **Android Studio → Check for Updates**), then reopen the project. |
| `Unsupported class file major version`, or `Gradle requires JVM 17 or later` | **Settings → Build, Execution, Deployment → Build Tools → Gradle**, and set **Gradle JDK** to the bundled one (named `jbr-…` or "Embedded JDK"). On a Mac, Settings is under the **Android Studio** menu. |
| Sync or build stuck, or strange errors after moving the folder | **Build → Clean Project**, then **File → Invalidate Caches… → Invalidate and Restart**. |
| Phone doesn't appear in the device list | Use a data cable (some cables only charge). Set the USB mode to **File transfer**, and accept the "Allow USB debugging?" prompt on the phone. On Windows, some phone brands need their own USB driver from the maker's website. |
| "App not installed" or "conflicts with an existing package" | An older copy signed with a different key is installed (see [Debug and release copies](#debug-and-release-copies)). Uninstall it first. This deletes saved progress. |
| The **Listen** button is greyed out, or speaks with the wrong accent | The phone needs a Spanish voice. Go to **Settings → System → Languages → Text-to-speech output**. Open the settings for the speech engine, choose **Install voice data**, and pick **Spanish (Spain)** or **Spanish (Mexico)**. The exact path varies by phone; searching Settings for "text-to-speech" finds it. |
| A red error naming a `.kt` file and a line number | The Kotlin code wasn't compiled before it was handed over, so a small slip is possible. Open the **Build** window at the bottom, copy the **first** error message, and send it over to be fixed. |

---

## Where things are

```
EasyEspanol/
  app/
    build.gradle.kts           app name/id, versionCode, minimum Android version
    src/main/
      AndroidManifest.xml      app label and icon
      assets/                  the phrase and expression CSV files
      res/                     icon, app name (values/strings.xml)
      java/com/example/easyespanol/
        MainActivity.kt        starts the app
        data/                  CSV reading, markup, settings and progress, speech
        navigation/            which screen leads where
        ui/                    the screens, plus ui/theme for colours
  docs/                        this guide, the data guide, icon preview
  tools/validate.py            checks the CSV files
```

The app id is `com.example.easyespanol`. That's fine for installing APKs yourself. To publish on Google Play one day, change `applicationId` in `app/build.gradle.kts` to something of your own, such as `uk.yourname.easyespanol`, before the first release. Play doesn't accept `com.example` ids.
