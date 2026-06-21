package com.github.codebydusk.quotd_quoteoftheday.emoji

import kotlin.random.Random

/**
 * Keyword-based emoji decorator that injects contextually relevant emojis
 * into quote text at runtime. The original JSON quotes are never modified.
 *
 * Design goals:
 * - Feel human, not robotic — varied placement, sparse usage
 * - Whole-word, case-insensitive matching
 * - Pre-compiled regex patterns for sub-ms performance
 * - Same quote looks slightly different across displays
 *
 * Probability model:
 * - 20% → no emoji
 * - 60% → one keyword emoji
 * - 20% → one keyword emoji + one reaction emoji
 *
 * Placement weights:
 * - Inline (after keyword): 60%
 * - Suffix (end of text): 25%
 * - Prefix (start of text): 15%
 */
class KeywordEmojiDecorator(
    private val random: Random = Random.Default
) : EmojiDecorator {

    // ── Keyword → Emoji mapping ───────────────────────────────────────
    //
    // Built from word-frequency analysis across all quote JSONs:
    //   bad_advice, chaos, copy, emotional_damage, horoscope,
    //   insults, love, lust, no, office_excuses

    private val keywordMap: Map<String, List<String>> = mapOf(
        // ── Time & Schedule ─────────────────────────────────────────
        "calendar" to listOf("📅", "🗓️"),
        "time" to listOf("⌛", "⏰"),
        "future" to listOf("⏳", "🔮"),
        "clock" to listOf("🕐", "⏰"),
        "hour" to listOf("⏳"),
        "minute" to listOf("⏱️"),
        "morning" to listOf("🌅", "☀️"),
        "night" to listOf("🌙", "🌃"),
        "today" to listOf("📆"),
        "tomorrow" to listOf("🔜"),
        "yesterday" to listOf("⏪"),
        "weekend" to listOf("🎉"),
        "monday" to listOf("😩"),
        "friday" to listOf("🎉"),
        "midnight" to listOf("🌙"),
        "alarm" to listOf("⏰", "🔔"),
        "snooze" to listOf("😴", "⏰"),
        "deadline" to listOf("⏰", "🔥"),
        "schedule" to listOf("📅"),
        "late" to listOf("⏰"),
        "early" to listOf("🌅"),
        "waiting" to listOf("⏳"),
        "hurry" to listOf("🏃"),

        // ── Celestial & Space ───────────────────────────────────────
        "universe" to listOf("🌌", "✨"),
        "stars" to listOf("✨", "🌟", "⭐"),
        "moon" to listOf("🌙", "🌕"),
        "sun" to listOf("☀️", "🌞"),
        "planet" to listOf("🪐"),
        "cosmos" to listOf("🌌"),
        "galaxy" to listOf("🌌"),
        "sky" to listOf("🌤️"),
        "constellation" to listOf("✨"),
        "stardust" to listOf("✨", "🌟"),
        "orbit" to listOf("🪐"),
        "gravity" to listOf("🌍"),
        "asteroid" to listOf("☄️"),
        "comet" to listOf("☄️"),
        "cosmic" to listOf("🌌", "✨"),

        // ── Technology & Digital ────────────────────────────────────
        "phone" to listOf("📱", "📲"),
        "wifi" to listOf("📶"),
        "email" to listOf("📧", "✉️"),
        "internet" to listOf("🌐"),
        "computer" to listOf("💻"),
        "keyboard" to listOf("⌨️"),
        "screen" to listOf("📱"),
        "battery" to listOf("🔋"),
        "charging" to listOf("🔌"),
        "notification" to listOf("🔔"),
        "password" to listOf("🔑", "🔒"),
        "google" to listOf("🔍"),
        "matrix" to listOf("👾"),
        "robot" to listOf("🤖"),
        "algorithm" to listOf("🤖"),
        "data" to listOf("📊"),
        "zoom" to listOf("💻"),
        "texting" to listOf("💬"),
        "typing" to listOf("⌨️"),
        "buffering" to listOf("⏳", "📶"),
        "loading" to listOf("⏳"),
        "glitch" to listOf("👾"),
        "update" to listOf("🔄"),
        "reboot" to listOf("🔄"),
        "server" to listOf("🖥️"),
        "error" to listOf("⚠️", "🚫"),
        "bug" to listOf("🐛"),
        "crash" to listOf("💥"),
        "download" to listOf("⬇️"),
        "upload" to listOf("⬆️"),
        "code" to listOf("👨‍💻"),
        "app" to listOf("📱"),
        "GPS" to listOf("📍"),
        "printer" to listOf("🖨️"),
        "meme" to listOf("😂"),
        "selfie" to listOf("🤳"),
        "hashtag" to listOf("#️⃣"),
        "podcast" to listOf("🎙️"),
        "streaming" to listOf("📺"),
        "screenshot" to listOf("📸"),
        "autocorrect" to listOf("📱"),
        "inbox" to listOf("📬"),
        "tab" to listOf("📱"),
        "tabs" to listOf("📱"),

        // ── Food & Drink ────────────────────────────────────────────
        "coffee" to listOf("☕", "🫘"),
        "caffeine" to listOf("☕", "⚡"),
        "espresso" to listOf("☕"),
        "tea" to listOf("🍵"),
        "pizza" to listOf("🍕"),
        "cake" to listOf("🎂", "🍰"),
        "snack" to listOf("🍿", "🍪"),
        "snacks" to listOf("🍿", "🍪"),
        "food" to listOf("🍽️"),
        "dinner" to listOf("🍽️"),
        "lunch" to listOf("🥪"),
        "breakfast" to listOf("🥞"),
        "wine" to listOf("🍷"),
        "beer" to listOf("🍺"),
        "tequila" to listOf("🥃"),
        "whiskey" to listOf("🥃"),
        "cocktail" to listOf("🍸"),
        "ice cream" to listOf("🍦"),
        "chocolate" to listOf("🍫"),
        "popcorn" to listOf("🍿"),
        "cookie" to listOf("🍪"),
        "donut" to listOf("🍩"),
        "donuts" to listOf("🍩"),
        "burger" to listOf("🍔"),
        "fries" to listOf("🍟"),
        "french fry" to listOf("🍟"),
        "sandwich" to listOf("🥪"),
        "burrito" to listOf("🌯"),
        "taco" to listOf("🌮"),
        "spaghetti" to listOf("🍝"),
        "pasta" to listOf("🍝"),
        "salad" to listOf("🥗"),
        "sushi" to listOf("🍣"),
        "soup" to listOf("🍲"),
        "steak" to listOf("🥩"),
        "bacon" to listOf("🥓"),
        "egg" to listOf("🍳"),
        "bread" to listOf("🍞"),
        "toast" to listOf("🍞"),
        "cheese" to listOf("🧀"),
        "garlic" to listOf("🧄"),
        "pepper" to listOf("🌶️"),
        "hot sauce" to listOf("🌶️"),
        "sugar" to listOf("🍬"),
        "salt" to listOf("🧂"),
        "smoothie" to listOf("🥤"),
        "cereal" to listOf("🥣"),
        "chips" to listOf("🍟"),
        "marshmallow" to listOf("☁️"),
        "cupcake" to listOf("🧁"),
        "pancake" to listOf("🥞"),
        "lemon" to listOf("🍋"),
        "lemons" to listOf("🍋"),
        "banana" to listOf("🍌"),
        "apple" to listOf("🍎"),
        "avocado" to listOf("🥑"),
        "watermelon" to listOf("🍉"),
        "peanut butter" to listOf("🥜"),
        "takeout" to listOf("🥡"),
        "microwave" to listOf("📡"),
        "oven" to listOf("🔥"),
        "cook" to listOf("👨‍🍳"),
        "cooking" to listOf("👨‍🍳"),
        "recipe" to listOf("📝"),
        "hungry" to listOf("😋"),

        // ── Home & Comfort ──────────────────────────────────────────
        "couch" to listOf("🛋️"),
        "sofa" to listOf("🛋️"),
        "bed" to listOf("🛏️"),
        "sleep" to listOf("😴", "💤"),
        "sleeping" to listOf("😴", "💤"),
        "nap" to listOf("😴", "💤"),
        "pillow" to listOf("🛏️"),
        "blanket" to listOf("🛏️"),
        "pajamas" to listOf("😴"),
        "home" to listOf("🏠"),
        "door" to listOf("🚪"),
        "shower" to listOf("🚿"),
        "bath" to listOf("🛁"),
        "kitchen" to listOf("🍳"),
        "fridge" to listOf("🧊"),
        "refrigerator" to listOf("🧊"),
        "laundry" to listOf("🧺"),
        "dishes" to listOf("🍽️"),
        "trash" to listOf("🗑️"),
        "closet" to listOf("👕"),
        "drawer" to listOf("🗄️"),
        "vacuum" to listOf("🧹"),
        "ironing" to listOf("👔"),
        "houseplant" to listOf("🪴"),
        "plant" to listOf("🪴"),
        "plants" to listOf("🪴"),
        "cactus" to listOf("🌵"),

        // ── Animals ─────────────────────────────────────────────────
        "cat" to listOf("🐈", "😼", "🐱"),
        "dog" to listOf("🐕", "🐶"),
        "fish" to listOf("🐟", "🐠"),
        "goldfish" to listOf("🐟"),
        "dragon" to listOf("🐉"),
        "unicorn" to listOf("🦄"),
        "parrot" to listOf("🦜"),
        "sloth" to listOf("🦥"),
        "butterfly" to listOf("🦋"),
        "chicken" to listOf("🐔"),
        "pigs" to listOf("🐷"),
        "pig" to listOf("🐷"),
        "moth" to listOf("🦋"),
        "kangaroo" to listOf("🦘"),
        "raccoon" to listOf("🦝"),
        "bear" to listOf("🐻"),
        "penguin" to listOf("🐧"),
        "pigeon" to listOf("🐦"),
        "squirrel" to listOf("🐿️"),
        "owl" to listOf("🦉"),
        "turtle" to listOf("🐢"),
        "monkey" to listOf("🐒"),
        "panda" to listOf("🐼"),
        "koala" to listOf("🐨"),
        "fox" to listOf("🦊"),
        "wolf" to listOf("🐺"),
        "hamster" to listOf("🐹"),
        "dolphin" to listOf("🐬"),
        "whale" to listOf("🐳"),
        "spider" to listOf("🕷️"),
        "bee" to listOf("🐝"),
        "honeybee" to listOf("🐝"),
        "goat" to listOf("🐐"),
        "llama" to listOf("🦙"),
        "bat" to listOf("🦇"),
        "jellyfish" to listOf("🪼"),
        "chameleon" to listOf("🦎"),
        "frog" to listOf("🐸"),
        "snail" to listOf("🐌"),
        "mosquito" to listOf("🦟"),
        "ant" to listOf("🐜"),
        "pet" to listOf("🐾"),
        "pets" to listOf("🐾"),
        "paw" to listOf("🐾"),

        // ── Work & Money ────────────────────────────────────────────
        "work" to listOf("💼"),
        "job" to listOf("💼"),
        "meeting" to listOf("📋", "🤝"),
        "meetings" to listOf("📋"),
        "boss" to listOf("👔"),
        "office" to listOf("🏢"),
        "money" to listOf("💰", "💵"),
        "bank" to listOf("🏦"),
        "salary" to listOf("💰"),
        "budget" to listOf("📊"),
        "deadline" to listOf("⏰", "🔥"),
        "schedule" to listOf("📅"),
        "resume" to listOf("📄"),
        "interview" to listOf("🤝"),
        "promotion" to listOf("📈"),
        "spreadsheet" to listOf("📊"),
        "coworker" to listOf("🏢"),
        "coworkers" to listOf("🏢"),
        "commute" to listOf("🚇"),
        "cubicle" to listOf("🏢"),
        "overtime" to listOf("⏰"),
        "startup" to listOf("🚀"),
        "corporate" to listOf("🏢"),
        "productive" to listOf("📈"),
        "productivity" to listOf("📈"),
        "motivation" to listOf("💪", "🔥"),
        "hustle" to listOf("💪"),
        "burnout" to listOf("🔥"),
        "freelance" to listOf("💻"),
        "paycheck" to listOf("💰"),
        "broke" to listOf("💸"),
        "debt" to listOf("💸"),
        "taxes" to listOf("🧾"),
        "rent" to listOf("🏠"),
        "loan" to listOf("💸"),
        "wallet" to listOf("👛"),
        "credit card" to listOf("💳"),
        "rich" to listOf("💰"),
        "billionaire" to listOf("💰"),
        "lottery" to listOf("🎰"),
        "shopping" to listOf("🛍️"),

        // ── Transport ───────────────────────────────────────────────
        "car" to listOf("🚗"),
        "bike" to listOf("🚲"),
        "train" to listOf("🚂"),
        "bus" to listOf("🚌"),
        "uber" to listOf("🚕"),
        "taxi" to listOf("🚕"),
        "traffic" to listOf("🚦"),
        "road" to listOf("🛣️"),
        "airplane" to listOf("✈️"),
        "flight" to listOf("✈️"),
        "broomstick" to listOf("🧹"),
        "subway" to listOf("🚇"),
        "parking" to listOf("🅿️"),
        "driving" to listOf("🚗"),
        "highway" to listOf("🛣️"),

        // ── Emotions & States ───────────────────────────────────────
        "love" to listOf("❤️", "💕"),
        "heart" to listOf("❤️", "💖"),
        "laugh" to listOf("😂"),
        "laughed" to listOf("😂"),
        "cry" to listOf("😢"),
        "crying" to listOf("😭"),
        "smile" to listOf("😊"),
        "happy" to listOf("😊", "🎉"),
        "sad" to listOf("😢"),
        "angry" to listOf("😤"),
        "scared" to listOf("😨"),
        "fear" to listOf("😰"),
        "brave" to listOf("💪"),
        "confidence" to listOf("💪"),
        "lazy" to listOf("🦥"),
        "laziness" to listOf("🦥"),
        "tired" to listOf("😴"),
        "exhausted" to listOf("😵"),
        "bored" to listOf("😑"),
        "confused" to listOf("🤔"),
        "mysterious" to listOf("🕵️"),
        "genius" to listOf("🧠"),
        "brain" to listOf("🧠"),
        "dream" to listOf("💭", "✨"),
        "dreams" to listOf("💭", "✨"),
        "nightmare" to listOf("😱"),
        "peace" to listOf("✌️", "☮️"),
        "chaos" to listOf("🌀"),
        "luck" to listOf("🍀"),
        "lucky" to listOf("🍀", "🎲"),
        "anxiety" to listOf("😰", "😬"),
        "stressed" to listOf("😫"),
        "stress" to listOf("😫"),
        "panic" to listOf("😱"),
        "overthinking" to listOf("🧠", "🌀"),
        "procrastination" to listOf("🦥", "⏳"),
        "procrastinate" to listOf("🦥"),
        "sarcasm" to listOf("🙃"),
        "sarcastic" to listOf("🙃"),
        "regret" to listOf("😬"),
        "awkward" to listOf("😅"),
        "embarrassing" to listOf("😳"),
        "cringe" to listOf("😬"),
        "jealous" to listOf("😒"),
        "lonely" to listOf("🥺"),
        "hope" to listOf("🌈"),
        "hopeless" to listOf("😞"),
        "optimist" to listOf("🌞"),
        "pessimist" to listOf("🌧️"),
        "grateful" to listOf("🙏"),
        "sorry" to listOf("🥺"),
        "guilty" to listOf("😬"),
        "stubborn" to listOf("🪨"),
        "patience" to listOf("⏳"),
        "moody" to listOf("🌦️"),
        "mood" to listOf("🌦️"),
        "emotional" to listOf("🥲"),
        "dramatic" to listOf("🎭"),
        "introvert" to listOf("🏠"),
        "extrovert" to listOf("🥳"),
        "antisocial" to listOf("🏠"),
        "vibes" to listOf("✨"),
        "vibe" to listOf("✨"),
        "chill" to listOf("🧊"),
        "relax" to listOf("😌"),
        "savage" to listOf("🔥"),
        "toxic" to listOf("☢️"),
        "wholesome" to listOf("🥰"),
        "petty" to listOf("💅"),
        "unbothered" to listOf("💅"),
        "flirting" to listOf("😏"),
        "crush" to listOf("😍"),
        "soulmate" to listOf("💞"),
        "soulmates" to listOf("💞"),
        "breakup" to listOf("💔"),
        "single" to listOf("💁"),
        "dating" to listOf("💘"),
        "romance" to listOf("💗"),
        "kiss" to listOf("💋"),
        "hug" to listOf("🤗"),

        // ── Activities & Entertainment ──────────────────────────────
        "gym" to listOf("💪", "🏋️"),
        "yoga" to listOf("🧘"),
        "run" to listOf("🏃"),
        "running" to listOf("🏃"),
        "marathon" to listOf("🏃"),
        "dance" to listOf("💃"),
        "dancing" to listOf("💃"),
        "party" to listOf("🎉", "🥳"),
        "vacation" to listOf("🏖️", "✈️"),
        "travel" to listOf("🧳", "✈️"),
        "adventure" to listOf("🗺️"),
        "game" to listOf("🎮"),
        "gaming" to listOf("🎮"),
        "movie" to listOf("🎬"),
        "movies" to listOf("🎬"),
        "music" to listOf("🎵"),
        "song" to listOf("🎵"),
        "singing" to listOf("🎤"),
        "guitar" to listOf("🎸"),
        "piano" to listOf("🎹"),
        "drums" to listOf("🥁"),
        "concert" to listOf("🎶"),
        "book" to listOf("📖"),
        "read" to listOf("📖"),
        "reading" to listOf("📖"),
        "write" to listOf("✍️"),
        "paint" to listOf("🎨"),
        "drawing" to listOf("🎨"),
        "camera" to listOf("📷"),
        "karaoke" to listOf("🎤"),
        "camping" to listOf("⛺"),
        "hiking" to listOf("🥾"),
        "surfing" to listOf("🏄"),
        "swimming" to listOf("🏊"),
        "fishing" to listOf("🎣"),
        "binge-watching" to listOf("📺"),
        "scrolling" to listOf("📱"),

        // ── Objects ─────────────────────────────────────────────────
        "key" to listOf("🔑"),
        "keys" to listOf("🔑"),
        "lock" to listOf("🔒"),
        "mirror" to listOf("🪞"),
        "glasses" to listOf("👓"),
        "sunglasses" to listOf("🕶️"),
        "hat" to listOf("🎩"),
        "crown" to listOf("👑"),
        "trophy" to listOf("🏆"),
        "medal" to listOf("🏅"),
        "gift" to listOf("🎁"),
        "balloon" to listOf("🎈"),
        "candle" to listOf("🕯️"),
        "fire" to listOf("🔥"),
        "bomb" to listOf("💣"),
        "flag" to listOf("🚩"),
        "red flag" to listOf("🚩"),
        "map" to listOf("🗺️"),
        "compass" to listOf("🧭"),
        "telescope" to listOf("🔭"),
        "microscope" to listOf("🔬"),
        "umbrella" to listOf("☂️"),
        "backpack" to listOf("🎒"),
        "suitcase" to listOf("🧳"),
        "hammer" to listOf("🔨"),
        "wrench" to listOf("🔧"),
        "scissors" to listOf("✂️"),
        "knife" to listOf("🔪"),
        "sword" to listOf("⚔️"),
        "shield" to listOf("🛡️"),
        "lightbulb" to listOf("💡"),
        "lamp" to listOf("💡"),

        // ── Nature & Weather ────────────────────────────────────────
        "rain" to listOf("🌧️", "☔"),
        "raining" to listOf("🌧️"),
        "snow" to listOf("❄️", "🌨️"),
        "storm" to listOf("⛈️"),
        "thunder" to listOf("⚡"),
        "lightning" to listOf("⚡"),
        "rainbow" to listOf("🌈"),
        "flower" to listOf("🌸"),
        "flowers" to listOf("💐"),
        "tree" to listOf("🌳"),
        "mountain" to listOf("🏔️"),
        "ocean" to listOf("🌊"),
        "wave" to listOf("🌊"),
        "wind" to listOf("💨"),
        "desert" to listOf("🏜️"),
        "volcano" to listOf("🌋"),
        "earthquake" to listOf("😱"),
        "grass" to listOf("🌿"),
        "beach" to listOf("🏖️"),
        "sunset" to listOf("🌅"),
        "sunrise" to listOf("🌅"),
        "tornado" to listOf("🌪️"),
        "hurricane" to listOf("🌀"),
        "ice" to listOf("🧊"),
        "fog" to listOf("🌫️"),
        "cloud" to listOf("☁️"),
        "clouds" to listOf("☁️"),
        "sunshine" to listOf("☀️"),
        "forest" to listOf("🌲"),
        "jungle" to listOf("🌴"),
        "garden" to listOf("🌻"),
        "river" to listOf("🏞️"),
        "lake" to listOf("🏞️"),
        "island" to listOf("🏝️"),
        "nature" to listOf("🌿"),

        // ── People & Social ─────────────────────────────────────────
        "friend" to listOf("🤝"),
        "friends" to listOf("🤝"),
        "family" to listOf("👨‍👩‍👧"),
        "baby" to listOf("👶"),
        "doctor" to listOf("🩺"),
        "teacher" to listOf("👩‍🏫"),
        "lawyer" to listOf("⚖️"),
        "therapist" to listOf("🛋️"),
        "neighbor" to listOf("🏘️"),
        "neighbors" to listOf("🏘️"),
        "stranger" to listOf("🕵️"),
        "superhero" to listOf("🦸"),
        "villain" to listOf("🦹"),
        "ghost" to listOf("👻"),
        "zombie" to listOf("🧟"),
        "alien" to listOf("🛸", "👽"),
        "wizard" to listOf("🧙"),
        "witch" to listOf("🧙‍♀️"),
        "vampire" to listOf("🧛"),
        "mermaid" to listOf("🧜‍♀️"),
        "prince" to listOf("🤴"),
        "princess" to listOf("👸"),
        "king" to listOf("🤴", "👑"),
        "queen" to listOf("👸", "👑"),
        "clown" to listOf("🤡"),
        "spy" to listOf("🕵️"),
        "detective" to listOf("🔍"),
        "celebrity" to listOf("⭐"),
        "influencer" to listOf("📱"),
        "barber" to listOf("💈"),
        "chef" to listOf("👨‍🍳"),
        "pilot" to listOf("✈️"),
        "astronaut" to listOf("🧑‍🚀"),
        "parent" to listOf("👪"),
        "parents" to listOf("👪"),
        "roommate" to listOf("🏠"),
        "partner" to listOf("💑"),
        "spouse" to listOf("💑"),
        "kids" to listOf("👶"),

        // ── Concepts & Abstract ─────────────────────────────────────
        "karma" to listOf("☯️"),
        "destiny" to listOf("🔮"),
        "fate" to listOf("🎲"),
        "magic" to listOf("✨", "🪄"),
        "miracle" to listOf("✨"),
        "horoscope" to listOf("🔮"),
        "zodiac" to listOf("♈"),
        "crystal" to listOf("🔮"),
        "crystal ball" to listOf("🔮"),
        "meditation" to listOf("🧘"),
        "philosophy" to listOf("🤔"),
        "science" to listOf("🔬"),
        "math" to listOf("🧮"),
        "art" to listOf("🎨"),
        "poetry" to listOf("📝"),
        "success" to listOf("🏆"),
        "failure" to listOf("💀"),
        "wisdom" to listOf("🦉"),
        "truth" to listOf("💡"),
        "lie" to listOf("🤥"),
        "excuse" to listOf("🤷"),
        "excuses" to listOf("🤷"),
        "boundaries" to listOf("🚧"),
        "comfort zone" to listOf("🏠"),
        "potential" to listOf("🌱"),
        "ambition" to listOf("🚀"),
        "reality" to listOf("🌍"),
        "imagination" to listOf("🌈"),
        "conspiracy" to listOf("🕵️"),
        "apocalypse" to listOf("💀", "🌋"),
        "doomsday" to listOf("💀"),
        "prophecy" to listOf("🔮"),
        "simulation" to listOf("👾"),
        "irony" to listOf("🙃"),
        "paradox" to listOf("🌀"),
        "society" to listOf("🌍"),
        "adulting" to listOf("😩"),
        "responsibility" to listOf("📋"),
        "responsibilities" to listOf("📋"),
        "self-care" to listOf("🧖"),
        "therapy" to listOf("🛋️"),

        // ── Pop Culture ─────────────────────────────────────────────
        "netflix" to listOf("📺"),
        "hogwarts" to listOf("🧙"),
        "jedi" to listOf("⚔️"),
        "darth vader" to listOf("⚔️"),
        "batman" to listOf("🦇"),
        "gotham" to listOf("🌃"),
        "mordor" to listOf("🌋"),
        "avengers" to listOf("🦸"),
        "tiktok" to listOf("📱"),
        "instagram" to listOf("📸"),
        "twitter" to listOf("🐦"),
        "spotify" to listOf("🎵"),
        "youtube" to listOf("📺"),
        "disney" to listOf("🏰"),
        "marvel" to listOf("🦸"),
        "star wars" to listOf("⚔️"),
        "cinderella" to listOf("👠"),
        "wonderwall" to listOf("🎸"),

        // ── Clothing & Appearance ───────────────────────────────────
        "pants" to listOf("👖"),
        "jeans" to listOf("👖"),
        "socks" to listOf("🧦"),
        "shoes" to listOf("👟"),
        "underwear" to listOf("😳"),
        "shirt" to listOf("👕"),
        "dress" to listOf("👗"),
        "jacket" to listOf("🧥"),
        "hoodie" to listOf("🧥"),
        "sunscreen" to listOf("🧴"),
        "makeup" to listOf("💄"),
        "haircut" to listOf("💇"),
        "hair" to listOf("💇"),
        "beard" to listOf("🧔"),
        "outfit" to listOf("👔"),
        "wardrobe" to listOf("👕"),
        "fashion" to listOf("👠"),

        // ── Sports & Games ──────────────────────────────────────────
        "football" to listOf("🏈"),
        "soccer" to listOf("⚽"),
        "basketball" to listOf("🏀"),
        "baseball" to listOf("⚾"),
        "tennis" to listOf("🎾"),
        "bowling" to listOf("🎳"),
        "chess" to listOf("♟️"),
        "dice" to listOf("🎲"),
        "coin" to listOf("🪙"),
        "treasure" to listOf("💎"),
        "pirate" to listOf("🏴‍☠️"),
        "ninja" to listOf("🥷"),
        "samurai" to listOf("⚔️"),
        "karate" to listOf("🥋"),
        "boxing" to listOf("🥊"),
        "champion" to listOf("🏆"),
        "lego" to listOf("🧱"),

        // ── Health & Body ───────────────────────────────────────────
        "diet" to listOf("🥗"),
        "calories" to listOf("🔢"),
        "weight" to listOf("⚖️"),
        "muscle" to listOf("💪"),
        "muscles" to listOf("💪"),
        "stretch" to listOf("🤸"),
        "sneeze" to listOf("🤧"),
        "headache" to listOf("🤕"),
        "toothache" to listOf("🦷"),
        "teeth" to listOf("🦷"),
        "immune" to listOf("🛡️"),
        "sick" to listOf("🤒"),
        "fever" to listOf("🤒"),
        "hospital" to listOf("🏥"),
        "pill" to listOf("💊"),
        "medicine" to listOf("💊"),
        "vitamin" to listOf("💊"),
        "allergic" to listOf("🤧"),
        "hangover" to listOf("🤕"),

        // ── Misc & Slang ────────────────────────────────────────────
        "neon" to listOf("💡"),
        "sign" to listOf("🪧"),
        "secret" to listOf("🤫"),
        "whisper" to listOf("🤫"),
        "scream" to listOf("😱"),
        "screaming" to listOf("😱"),
        "applause" to listOf("👏"),
        "silence" to listOf("🤐"),
        "nope" to listOf("🙅"),
        "absolutely" to listOf("💯"),
        "perfect" to listOf("👌"),
        "brilliant" to listOf("✨"),
        "disaster" to listOf("💥"),
        "dumpster fire" to listOf("🗑️🔥"),
        "hot mess" to listOf("🔥"),
        "hero" to listOf("🦸"),
        "legend" to listOf("🏆"),
        "masterpiece" to listOf("🖼️"),
        "limited edition" to listOf("✨"),
        "cryptid" to listOf("👻"),
        "hermit" to listOf("🏚️"),
        "rebel" to listOf("🤘"),
        "throne" to listOf("🪑"),
        "kingdom" to listOf("🏰"),
        "quest" to listOf("🗺️"),
        "potion" to listOf("🧪"),
        "spell" to listOf("🪄"),
        "wand" to listOf("🪄"),
        "cape" to listOf("🦸"),
        "mask" to listOf("🎭"),
        "costume" to listOf("🎭")
    )

    // ── Reaction pool for the "two emoji" case ────────────────────────

    private val reactions = listOf(
        "😂", "🤣", "🙃", "😅", "🤦", "🤷", "✨", "💀",
        "😏", "🫠", "😤", "🥲", "💅", "👀", "🫡", "😶"
    )

    // ── Pre-compiled regex patterns (built once) ──────────────────────

    private data class KeywordEntry(
        val keyword: String,
        val emojis: List<String>,
        val pattern: Regex
    )

    private val entries: List<KeywordEntry> = keywordMap.map { (keyword, emojis) ->
        // Whole-word matching, case-insensitive
        // For multi-word keywords (e.g. "ice cream", "darth vader"), match as-is
        val escaped = Regex.escape(keyword)
        val pattern = Regex("\\b$escaped\\b", RegexOption.IGNORE_CASE)
        KeywordEntry(keyword, emojis, pattern)
    }

    // ── Public API ────────────────────────────────────────────────────

    override fun decorate(text: String): String {
        // Step 1: Roll the dice for emoji count
        val roll = random.nextInt(100)
        val emojiCount = when {
            roll < 20 -> 0  // 20% → no emoji
            roll < 80 -> 1  // 60% → one emoji
            else -> 2       // 20% → two emojis (keyword + reaction)
        }

        if (emojiCount == 0) return text

        // Step 2: Find all keyword matches
        val matches = findMatches(text)
        if (matches.isEmpty()) return text

        // Step 3: Pick one random keyword match
        val match = matches.random(random)
        val emoji = match.emojis.random(random)

        // Step 4: Apply placement
        var result = applyPlacement(text, match, emoji)

        // Step 5: If two emojis, add a reaction at the end
        if (emojiCount == 2) {
            val reaction = reactions.random(random)
            // Avoid duplicating the same emoji
            if (reaction != emoji) {
                result = result.trimEnd() + " $reaction"
            }
        }

        return result
    }

    // ── Internal helpers ──────────────────────────────────────────────

    private data class MatchResult(
        val keyword: String,
        val emojis: List<String>,
        val matchRange: IntRange
    )

    private fun findMatches(text: String): List<MatchResult> {
        val results = mutableListOf<MatchResult>()
        for (entry in entries) {
            val matchResult = entry.pattern.find(text)
            if (matchResult != null) {
                results.add(
                    MatchResult(
                        keyword = entry.keyword,
                        emojis = entry.emojis,
                        matchRange = matchResult.range
                    )
                )
            }
        }
        return results
    }

    private fun applyPlacement(text: String, match: MatchResult, emoji: String): String {
        val roll = random.nextInt(100)
        return when {
            roll < 60 -> placeInline(text, match, emoji)   // 60% inline
            roll < 85 -> placeSuffix(text, emoji)           // 25% suffix
            else -> placePrefix(text, emoji)                // 15% prefix
        }
    }

    private fun placeInline(text: String, match: MatchResult, emoji: String): String {
        // Insert emoji right after the first occurrence of the matched keyword
        val insertPos = match.matchRange.last + 1
        return text.substring(0, insertPos) + " $emoji" + text.substring(insertPos)
    }

    private fun placeSuffix(text: String, emoji: String): String {
        return text.trimEnd() + " $emoji"
    }

    private fun placePrefix(text: String, emoji: String): String {
        return "$emoji " + text.trimStart()
    }
}
