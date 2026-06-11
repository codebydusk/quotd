# quotd — Quote of the Day

> Quotes. Horoscopes. Bad advice. Emotional damage.  
> Daily wisdom, questionable wisdom, and everything in between.  
> From love letters to sarcastic horoscopes—one widget, endless moods.

---

## About

**quotd** is a minimal Android home screen widget that serves you a random quote, horoscope, or piece of questionable wisdom every hour. The design is intentionally flat—just text on a background, nothing more. Tap to copy. Refresh when bored. Customise per widget.

No accounts. No internet. No tracking. Just words.

---

## Features

- **Multiple widget modes** — No, Horoscope, Bad Advice, Emotional Damage, Love Letters
- **Two widget sizes** — 4×1 (compact) and 4×2 (expanded)
- **Per-widget color customisation** — Each widget instance has its own background and text color
- **Tap to copy** — Tap the quote to copy it to your clipboard; widget shows "Copied!" for 2 seconds
- **Refresh button** — Manual refresh on the right side of the widget
- **Auto-refresh** — Quotes rotate automatically every 1 hour
- **Offline-first** — All quotes are bundled locally in JSON; no internet required
- **Flat design** — Two colors. That's it.

---

## Widget Types

| Widget | Size | Description | Status |
|--------|------|-------------|--------|
| quotd · No | 4×1 | Creative ways to say no | ✅ Available |
| quotd · No | 4×2 | Creative ways to say no (expanded) | ✅ Available |
| quotd · Horoscope | 4×1 | Sarcastic cosmic wisdom | ✅ Available |
| quotd · Horoscope | 4×2 | Sarcastic cosmic wisdom (expanded) | ✅ Available |
| quotd · Bad Advice | 4×1 | Terrible life tips | 🚧 Coming Soon |
| quotd · Bad Advice | 4×2 | Terrible life tips (expanded) | 🚧 Coming Soon |
| quotd · Emotional Damage | 4×1 | Brutal honesty delivered fresh | 🚧 Coming Soon |
| quotd · Emotional Damage | 4×2 | Brutal honesty delivered fresh | 🚧 Coming Soon |
| quotd · Love Letters | 4×1 | Sweet nothings for your screen | 🚧 Coming Soon |
| quotd · Love Letters | 4×2 | Sweet nothings for your screen | 🚧 Coming Soon |

---

## Getting Started

### Adding a Widget

1. Long-press on your home screen
2. Select **Widgets**
3. Find **quotd** in the widget list
4. Choose your preferred mode and size
5. Configure colors in the setup screen
6. Tap **Apply**

### Configuring Colors

When placing a widget, a configuration screen appears with:
- **Category selector** — Pick the quote mode
- **Theme presets** — 8 curated color combinations
- Live preview of the widget with your chosen theme

### Multiple Widgets

You can place multiple widgets on the same home screen, each with:
- A different category (e.g., one "No" and one "Horoscope")
- Independent color schemes
- Separate refresh cycles

---

## Categories

### 🚫 No
Creative, hilarious, and dramatic ways to decline anything. Over 1,000 entries ranging from polite deflections to absurd excuses involving time paradoxes and imaginary pets.

### 🔮 Horoscope
The stars have spoken—and they're roasting you. ~95 sarcastic horoscope entries that skewer astrology with affection.

### 💀 Bad Advice
*(Coming Soon)* — Terrible life tips that sound just convincing enough to be dangerous.

### 💥 Emotional Damage
*(Coming Soon)* — Brutal observations that hit a little too close to home.

### 💌 Love Letters
*(Coming Soon)* — Sweet, modern, slightly unhinged love notes for the digital age.

---

## Building from Source

### Prerequisites

- Android Studio Ladybug or later
- JDK 11+
- Android SDK 36

### Build

```bash
git clone https://github.com/codebydusk/quotd.git
cd quotd
./gradlew assembleDebug
```

### Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Contributing

### Adding Quotes

1. Edit the JSON file in `app/src/main/assets/` for the target category
2. Each file is a simple JSON array of strings:
   ```json
   [
       "Your quote here.",
       "Another quote here."
   ]
   ```
3. Build and test

### Adding a New Category

1. Create a new JSON file in `app/src/main/assets/`
2. Add the category mapping in `QuoteRepository.kt`
3. Add string resources in `strings.xml`
4. Create widget info XML files in `res/xml/`
5. Register new receiver subclasses in `AndroidManifest.xml`

---

## License

This project is licensed under the GNU General Public License v3.0 — see the [LICENSE](LICENSE) file for details.
