# Building Easy Español into an APK

This guide takes the repository from a zip file to an app on your phone.

## What builds this app?

Easy Español is a **Kotlin** app built with **Gradle**. Gradle comes with the project: the `gradlew` files in the top folder download and run it for you. Android Studio provides everything else Gradle needs (the Android SDK and Java).

**Buildozer isn't involved.** That's a tool for Python apps.

There are two ways onto a phone:

- **Route A, the Run button:** Android Studio builds the app and installs it on a phone plugged in by USB. No APK file is involved.
- **Route B, a terminal command:** one command produces an APK file you can copy to any phone. This doesn't depend on Android Studio's menus, which move between versions. In Otter and newer, the old Build APK menu items aren't where older guides say.

---

## Setup (once)

### 1. Install Android Studio

The copy you built EasyJapanesey with is fine. Otherwise, get it from <https://developer.android.com/studio> and accept the defaults. The first launch downloads the Android SDK, which is a few gigabytes; let it finish.

### 2. Unzip the project

Unzip `EasyEspanol.zip` to get a folder called `EasyEspanol`. It contains `settings.gradle.kts`, `gradlew`, `gradlew.bat` and an `app` folder.

**On Windows, use a short path** such as `C:\Projects\EasyEspanol`. Avoid deep folders and OneDrive-synced folders like Documents or Desktop. Windows' path-length limit and OneDrive file locking both cause baffling build errors.

### 3. Open it in Android Studio

1. On the welcome screen, click **Open**. If a project is already open, use **File → Open…**.
   - On Windows, Android Studio's newer look hides the menu bar behind the **☰** icon at the top left.
2. Select the **`EasyEspanol`** folder, the one that directly contains `settings.gradle.kts`. Click **OK**, then **Trust Project**.

### 4. Wait for the Gradle sync

A progress bar at the bottom right shows Android Studio downloading what the project needs. **The first time takes several minutes** and needs internet. It's finished when the bar disappears.

- If it offers to upgrade the Android Gradle Plugin, dismiss it. The project works as it is.
- If a message says something is missing (usually an SDK platform), click its blue link to install it. Then sync again with the elephant icon at the top right, or **File → Sync Project with Gradle Files**.

---

## Route A: install straight onto your phone

### Turn on USB debugging (once per phone)

1. On the phone, go to **Settings → About phone** and tap **Build number** seven times. On Samsung, it's under **About phone → Software information**.
2. In **Settings → System → Developer options**, turn on **USB debugging**.

### Run it

1. Plug the phone in.
   - When the phone asks "Allow USB debugging?", tick **Always allow** and tap **Allow**.
   - If it asks what the USB connection is for, choose **File transfer**.
2. In Android Studio's top toolbar, choose your phone in the device drop-down.
3. Press the green **▶ Run** button (Shift+F10 on Windows, Ctrl+R on Mac).

The app installs, opens, and stays on the phone after you unplug it.

To test without a phone, choose the **emulator** in the same drop-down instead.

---

## Route B: build an APK file from the terminal

### 1. Open the terminal inside Android Studio

- Click the **Terminal** icon in the tool strip down the left or bottom edge (it looks like `>_`).
- Or use **View → Tool Windows → Terminal**, or press **Alt+F12** (⌥F12 on a Mac).

The terminal opens in the project folder, which is where the command needs to run.

### 2. Run the build command

**Windows** (the terminal is PowerShell, so the `.\` at the start matters):

```
.\gradlew assembleDebug
```

**Mac or Linux:**

```
./gradlew assembleDebug
```

The first run downloads Gradle itself, so give it a few minutes. You're done when it prints **BUILD SUCCESSFUL**.

### 3. Collect the APK

```
EasyEspanol\app\build\outputs\apk\debug\app-debug.apk
```

You can rename it, for example to `EasyEspanol.apk`. Right-click the file in Android Studio's project tree and choose **Open In → Explorer** (Finder on a Mac) to see it in its folder.

### If you see "JAVA_HOME is not set"

The terminal doesn't always know where Android Studio keeps its built-in Java. Point it there for the current terminal window, then run the build command again.

**Windows (PowerShell):**

```
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew assembleDebug
```

**Mac:**

```
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

If Android Studio is installed somewhere else, use that location. The folder you want is called `jbr`, inside the Android Studio folder. The setting lasts until you close the terminal.

### Other errors

- **`permission denied` on a Mac:** run `chmod +x gradlew` once, then try again.
- **`gradlew is not recognised`:** you're in the wrong folder, or you left off the `.\`. Type `cd` followed by the path to the `EasyEspanol` folder, then try again.

### No terminal? Use the Gradle panel instead

1. Click the **elephant** icon on the right-hand edge to open the Gradle panel.
2. Click its **Execute Gradle Task** button (also an elephant), type `assembleDebug`, and press Enter.

This uses Android Studio's own Java, so the JAVA_HOME problem doesn't arise. The APK lands in the same place.

---

## Installing the APK on a phone

1. Copy `app-debug.apk` to the phone. You can drag it into the phone's **Download** folder over USB, use Google Drive, or email it to yourself.
2. On the phone, open it from the **Files** app or the download notification.
3. Android will say this source isn't allowed to install apps. Tap **Settings**, turn on **Allow from this source**, and go back.
4. Tap **Install**.
5. If Play Protect warns about an unknown app, tap **More details → Install anyway**. That's normal for any app you build yourself.

---

## A signed release APK (optional)

The debug APK is signed automatically with a key stored on your computer, and it's fine for your own use. A **release** APK is a little faster and smaller, and is signed with a key of your own. Setting that up is a one-off.

### 1. Make a keystore (once)

In the Android Studio terminal, run the line for your computer. It asks for a password and a few details; only the password matters.

**Windows:**

```
& "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -genkeypair -v -keystore easyespanol.jks -alias easyespanol -keyalg RSA -keysize 2048 -validity 10000
```

**Mac:**

```
"/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool" -genkeypair -v -keystore easyespanol.jks -alias easyespanol -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Tell the build where it is

1. Copy `keystore.properties.example` to `keystore.properties`, in the same top folder.
2. Put your password in both password lines.

Git ignores both this file and the `.jks` keystore, so they won't end up on GitHub.

### 3. Build

```
.\gradlew assembleRelease        (Windows)
./gradlew assembleRelease        (Mac / Linux)
```

The APK is `app\build\outputs\apk\release\app-release.apk`.

**Back up `easyespanol.jks` and the password.** Every future update must be signed with the same key.

### Which copy can replace which

Android only installs an update over an existing copy if both were signed with the same key:

- **Debug and release don't mix.** A release APK won't install over a debug one, or the other way round.
- **Each computer has its own debug key.** A debug build from a second computer won't install over one from the first.
- **You'll see "App not installed" or "conflicts with an existing package".** The only fix is to uninstall first, which deletes the progress saved on the phone.

So pick one kind of build, on one computer, and stick with it.

---

## Updating the app later

1. Change the phrases (see `docs/DATA_GUIDE.md`), then run `python tools/validate.py`.
2. In `app/build.gradle.kts`, add 1 to `versionCode`, for example `versionCode = 2`.
3. Build again (Route A or B) and install over the top.

Progress is kept, because it's stored against phrase ids that never change.

---

## The look: colours, fonts, icon

### Colour schemes

In the app, open **Settings → Look and feel** to choose from:

| Palette | Colours |
|---|---|
| **Atardecer** (default) | cobalt, violet, bougainvillea and coral |
| **Rojo y oro** | crimson and gold |
| **Azulejo** | tile blue and sea glass |
| **Jacaranda** | violet and blossom pink |
| **Olivo** | olive and gold |
| **Match my wallpaper** | taken from your phone's wallpaper |

Each palette also has a dark version. **Light or dark** chooses one, or follows the phone.

Some colours mean the same thing in every palette:

- **Green** is "I know this" and **amber** is "Still learning". Red is kept for real errors, so being unsure never looks like failing.
- **The nine chunk colours** linking Spanish to English were checked to stay readable on light and dark cards.

**Why EasyJapanesey came out brown:** its `Theme.kt` has `dynamicColor: Boolean = true`. On Android 12 and newer, that builds the colours from your **wallpaper**: blue on the emulator's default wallpaper, brown on yours. To fix it there, set it to `false` and put your blues into `Color.kt`. Easy Español only does this if you choose **Match my wallpaper**.

**To change the colours in code**, look in `app/src/main/java/com/example/easyespanol/ui/theme/`:

- **`Theme.kt`** holds each palette's colour scheme and its **brand** colours:
  - the gradient for the home tile and main buttons,
  - the background glows,
  - the six accent colours that topics take in turn.
- **`Color.kt`** holds the chunk colours and the known / still-learning colours.
- **Hex codes** are written `0xFF` followed by the usual six digits, for example `0xFF2D4BD8`.
- **To add a palette**, add a line to `AppPalette`, then a brand in `brandFor()` and a scheme in `paletteScheme()`. It appears in Settings automatically.
- **For a full set of shades from one colour**, Google's free **Material Theme Builder** (<https://material-foundation.github.io/material-theme-builder/>) exports them for Jetpack Compose.

### Fonts

Both fonts are under the SIL Open Font License, which allows bundling them in an app. Their licences ship inside the app, in `app/src/main/assets/licences/`.

- **Headings:** Bricolage Grotesque.
- **Phrases and text:** Instrument Sans.

The files are in `app/src/main/res/font/`, and `ui/theme/Type.kt` sets the sizes.

**To use other fonts:**

1. Drop the `.ttf` files into `res/font/`. The names must be lower case, with underscores only.
2. Change the two `FontFamily` definitions in `Type.kt`.

### Motion

- **Screens:** slide in from the right as you go deeper, and back out as you return.
- **Cards:** slide in the direction you move.
- **Answers:** expand into place when you tap a card.
- **Progress ring:** sweeps round once when the home screen opens.

### The icon

The icon is the Spanish flag as an adaptive icon, so it takes whatever shape your phone uses; `docs/icon_preview.png` shows it. The files are `res/drawable/ic_launcher_*.xml` and `res/mipmap-anydpi-v26/`.

To use your own picture, right-click `app` and choose **New → Image Asset**. If the phone keeps showing the old icon after an update, restart it.

---

## Troubleshooting

| Message or problem | What to do |
|---|---|
| `JAVA_HOME is not set` or `no 'java' command could be found` | See [If you see "JAVA_HOME is not set"](#if-you-see-java_home-is-not-set) above, or use the Gradle panel instead. |
| `SDK location not found` | Open the project once in Android Studio (**File → Open**). That creates the missing `local.properties` file. |
| Anything about compile SDK 36, or `android-36`, not being installed | **Tools → SDK Manager → SDK Platforms**. Tick **API level 36**, then click **Apply**. |
| The plugin needs a newer Android Studio | **Help → Check for Updates**, then reopen the project. |
| `Unsupported class file major version`, or `requires JVM 17` | **Settings → Build, Execution, Deployment → Build Tools → Gradle**. Set **Gradle JDK** to the bundled one (`jbr-…`). In the terminal, use the JAVA_HOME line above. |
| Strange errors after moving the folder | Delete `app/build` and `.gradle`, then run the build command again. |
| Phone not in the device list | Use a data cable (some only charge), set USB mode to **File transfer**, and accept the prompt on the phone. |
| "App not installed" or "conflicts with an existing package" | A copy signed with a different key is installed. Uninstall it first; this deletes progress. |
| **Listen** makes no sound, or says no Spanish voice is installed when one is | Open **Settings → Speech**. If your phone has more than one **speech engine** (Samsung phones usually have Samsung's and Google's), pick the one you installed Spanish in, then tap **Test voice**. Install voice opens the installer for whichever engine is selected. The small line of text there shows what that engine reports about Spanish; if it still won't play, send that line over. Also check the media volume. |
| A red error naming a `.kt` file and a line number | The code wasn't compiled before handover, so a small slip is possible. Copy the **first** error (with its file name and line) and send it over to be fixed. |

---

## Where things are

```
EasyEspanol/
  gradlew, gradlew.bat         the build commands used above
  keystore.properties.example  template for release signing
  app/
    build.gradle.kts           app id, versionCode, signing
    src/main/
      assets/                  phrase CSVs, font licences
      res/                     icon, fonts, app name
      java/com/example/easyespanol/
        data/                  CSV reading, settings, progress, streak, speech
        navigation/            screens and the transitions between them
        ui/                    the screens; ui/theme for colours and type
  docs/                        this guide, the data guide, icon preview
  tools/validate.py            checks the CSV files
```

The app id is `com.example.easyespanol`. That's fine for installing APKs yourself. Google Play doesn't accept `com.example` ids, so to publish there, change `applicationId` in `app/build.gradle.kts` before the first release.
