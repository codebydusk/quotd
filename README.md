<p align="center">
  <img src="assets/banner.png" alt="quotd banner" width="100%">
</p>

<h1 align="center">quotd — Quote of the Day</h1>

<p align="center">
  <i>A widget full of reasons, wisdom, chaos, and emotional damage.</i>
</p>

<p align="center">
  <strong>No accounts. No internet. No tracking. Just words.</strong>
</p>

---

## 🌟 About quotd

**quotd** is a minimal Android home screen widget that delivers random quotes, excuses, observations, horoscopes, bad advice, romance, emotional damage, and pure chaos directly to your home screen. 

The design is intentionally flat—just text on a background, nothing more. We put typography first. It's meant to be highly shareable, humorous, and full of personality.

---

## ✨ Features

We designed **quotd** with a simple philosophy: The text is the UI. No chrome, no clutter, and instant loads.

| Feature | Description |
|---|---|
| **8 Unique Categories** | Choose from: No, Chaos, Bad Advice, Emotional Damage, Love, Horoscope, Funny Insults, or Office Excuses. |
| **Typography Control** | Adjust Font Size (Extra Small to Extra Large) and Font Face (Sans Serif, Serif, Monospace). |
| **Dynamic Themes** | Auto (follows system dark mode), Light, or Dark. Includes curated presets: Default OS Monet, Ubuntu, Nothing OS, OLED Lime, Navy, Forest, Plum, and Crimson. |
| **Shape Shifting** | Customize the widget's corner radius (Pill, Rounded, Sharp, or OS Default). |
| **Smart Refreshing** | Manual refresh button, plus a per-widget configurable Auto-Refresh interval (1 min up to 24 hours). |
| **Dynamic Emojis** | Optional keyword-based emoji injection at runtime (toggleable per widget). |
| **Tap to Copy** | Tap the quote to copy it to your clipboard. |
| **Offline First** | All quotes are bundled locally in JSON; no internet required. |
| **Live Configuration** | No "Apply" button needed; tweak settings and see them update instantly on your widget. |

---

## 🤖 The Emoji Engine

The original quote JSON files are **always emoji-free**. We use a `KeywordEmojiDecorator` that scans quotes for contextually relevant words and sprinkles in emojis that feel natural, not random.

**How it works:**
- Scans for 200+ keywords (whole-word, case-insensitive).
- Chooses a placement strategy: inline (60%), suffix (25%), or prefix (15%).
- **20% of the time:** No emoji at all — not every quote needs one.
- **20% of the time:** Adds a second "reaction" emoji (😂, 💀, 🤷, etc.).

*The same quote looks slightly different every time. Clipboard copies always get the original, undecorated text.*

---

## 🚀 Getting Started

1. Long-press on your home screen and select **Widgets**.
2. Find **quotd** in the widget list and add the 4×1 widget.
3. Configure category, colors, typography, and corner shapes in the setup screen.
4. Adjust settings to taste—changes apply instantly. Just exit the screen when done!
5. Resize it from your launcher if you want more vertical space (it will seamlessly switch to an expanded 4×2 layout).

> **Pro-Tip**: You can place multiple widgets on the same home screen, each with independent categories, color schemes, and refresh cycles!

---

## 🛠️ For Developers & Contributors

**Welcome!** We love developers who want to bring more chaos, wisdom, or technical polish to the app. Whether you want to add a hilarious new category, submit a pull request for a feature, or just add a few quotes—we are thrilled to have you here. 

### Where you can improve the app:
- **More Quotes**: Have a killer line for "Bad Advice"? Submit it!
- **New Categories**: Think of a fun new category (e.g., "Developer Excuses", "Shower Thoughts")? We'd love to see it.
- **UI/UX Polish**: Any ideas for smoother animations or layout tweaks are always welcome.

### Adding Quotes
1. Edit the target category JSON file in `app/src/main/assets/`.
2. Add your string to the simple JSON array.
3. Build, test, and open a PR!

### Adding a New Category
1. Create a new JSON file in `app/src/main/assets/`.
2. Add the category mapping in `QuoteRepository.kt`.
3. Add the string resource in `strings.xml`.
4. Add the category entry in `QuotdWidgetConfigActivity.kt`.

### Building from Source
**Prerequisites**: Android Studio Ladybug (or later), JDK 11+, Android SDK 36.
```bash
git clone https://github.com/codebydusk/quotd.git
cd quotd
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🙏 Credits

- Huge thanks to [@danny5395](https://github.com/danny5395) for the original app idea!
- Quotes for the "No" category are provided as-is from the [no-as-a-service](https://github.com/hotheadhacker/no-as-a-service) repository by [@hotheadhacker](https://github.com/hotheadhacker).

---

## 📝 License & Author

**Sayantan Roy** — [@codebydusk](https://github.com/codebydusk)

This project is licensed under the GNU General Public License v3.0 — see the [LICENSE](LICENSE) file for details.
