# Easy Español

A phrase-based Spanish learning app for Android, adapted from EasyJapanesey. Learners work through topics and scenes of everyday phrases, with each chunk of Spanish colour-linked to its English equivalent so grammar and word order are learnt by seeing them.

This repository holds the complete Android app (Kotlin and Jetpack Compose) together with its phrase content and data tools.

**To build it into an APK, follow [docs/BUILDING_THE_APK.md](docs/BUILDING_THE_APK.md).**

## Layout

```
app/
  build.gradle.kts           app id, version number, minimum Android version
  src/main/
    assets/
      phrases_spain.csv        3,553 phrases, Castilian Spanish
      phrases_latam.csv        the same 3,553 phrases, Mexican Spanish
      expressions_spain.csv    34 colloquial expressions
      expressions_latam.csv    35 colloquial expressions
    java/com/example/easyespanol/
      data/                    CSV reading, markup, settings, progress, speech
      navigation/              routes between screens
      ui/                      home, topics, study, expressions, settings, theme
    res/                       Spanish-flag app icon, app name
docs/
  BUILDING_THE_APK.md        building the APK (Run button or one terminal command), troubleshooting
  DATA_GUIDE.md              columns, markup, difficulty levels, dialect rules
  icon_preview.png           the icon under different launcher shapes
tools/
  validate.py                checks the data; run after every edit
```

## The content

- 22 topics and 216 scenes, from greetings and hotels to storytelling, the news, paperwork, residency and learning Spanish itself.
- Every phrase has a grammar level and a vocabulary level (1–3 each), so the app can filter on both independently.
  - Grammar: 1,609 phrases at level 1, 1,117 at level 2, 827 at level 3.
  - Vocabulary: 1,042 at level 1, 1,595 at level 2, 916 at level 3.
- 983 phrases differ between the Spain and Latin America files. The differences cover vocabulary (coche / carro), grammar (vosotros / ustedes, present perfect / preterite) and meaning traps.
- 83 phrases change with the speaker's gender setting, marked inline as `cansad[o|a]`.
- Both files share IDs, order, levels and English, so switching dialect keeps a learner's progress.

## Checking the data

```
python tools/validate.py
```

The script needs Python 3 and no extra packages. It prints a count of phrases per topic and level, and exits with an error listing any problems. Among other things it checks the colour-link markup, gender markup, levels, duplicate IDs, that every ID prefix keeps one topic and scene name, and that the two dialect files agree on everything except the Spanish and notes.

## The app

- **Home:**
  - A sunset-gradient tile with an azulejo pattern, a greeting and overall progress.
  - Tiles for your daily streak, cards reviewed today and phrases known.
  - A "continue" button, and entry points to topics and expressions.
- **Topics → scenes → cards:**
  - Each topic has its own colour and emoji, plus a progress ring.
  - Cards show English or Spanish first. Tapping one reveals the other side, with matching chunks in matching colours and a note underneath.
  - Each card has a **Listen** button (the phone's text-to-speech), and buttons for "I know this" (green) and "Still learning" (amber).
  - Cards slide as you move through a scene, and screens slide between each other.
- **Settings:**
  - Spain or Mexico, the speaker's gender, which side of the card comes first, and grammar and vocabulary levels.
  - Skip known phrases.
  - Five colour palettes plus "match my wallpaper", each with light and dark modes.
  - Reset progress.
- **Fonts:** Bricolage Grotesque for headings and Instrument Sans for text. Both are open-source (SIL OFL) and bundled, with their licences in `app/src/main/assets/licences/`.
- **Progress** is stored on the phone against each phrase's permanent `id`, so it survives switching dialect and installing updated content.
- **CSV reading:** the files use standard CSV quoting, and the app reads them with a quote-aware parser. Many Spanish fields contain commas, which would break a simple `split(",")`.
