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

    private val keywordMap: Map<String, List<String>> = mapOf(
        // Time & Space
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

        // Celestial
        "universe" to listOf("🌌", "✨"),
        "stars" to listOf("✨", "🌟", "⭐"),
        "moon" to listOf("🌙", "🌕"),
        "sun" to listOf("☀️", "🌞"),
        "planet" to listOf("🪐"),
        "cosmos" to listOf("🌌"),
        "galaxy" to listOf("🌌"),
        "sky" to listOf("🌤️"),
        "constellation" to listOf("✨"),

        // Technology
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
        "password" to listOf("🔑"),
        "google" to listOf("🔍"),
        "matrix" to listOf("👾"),
        "robot" to listOf("🤖"),
        "algorithm" to listOf("🤖"),
        "data" to listOf("📊"),
        "zoom" to listOf("💻"),
        "texting" to listOf("💬"),
        "typing" to listOf("⌨️"),

        // Food & Drink
        "coffee" to listOf("☕", "🫘"),
        "tea" to listOf("🍵"),
        "pizza" to listOf("🍕"),
        "cake" to listOf("🎂", "🍰"),
        "snack" to listOf("🍿", "🍪"),
        "food" to listOf("🍽️"),
        "dinner" to listOf("🍽️"),
        "lunch" to listOf("🥪"),
        "breakfast" to listOf("🥞"),
        "wine" to listOf("🍷"),
        "beer" to listOf("🍺"),
        "tequila" to listOf("🥃"),
        "cocktail" to listOf("🍸"),
        "ice cream" to listOf("🍦"),
        "chocolate" to listOf("🍫"),
        "popcorn" to listOf("🍿"),
        "cookie" to listOf("🍪"),

        // Home & Comfort
        "couch" to listOf("🛋️"),
        "bed" to listOf("🛏️"),
        "sleep" to listOf("😴", "💤"),
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

        // Animals
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
        "moth" to listOf("🦋"),
        "kangaroo" to listOf("🦘"),

        // Work & Money
        "work" to listOf("💼"),
        "job" to listOf("💼"),
        "meeting" to listOf("📋", "🤝"),
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

        // Transport
        "car" to listOf("🚗"),
        "bike" to listOf("🚲"),
        "train" to listOf("🚂"),
        "bus" to listOf("🚌"),
        "uber" to listOf("🚕"),
        "traffic" to listOf("🚦"),
        "road" to listOf("🛣️"),
        "airplane" to listOf("✈️"),
        "flight" to listOf("✈️"),
        "broomstick" to listOf("🧹"),

        // Emotions & States
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
        "tired" to listOf("😴"),
        "bored" to listOf("😑"),
        "confused" to listOf("🤔"),
        "mysterious" to listOf("🕵️"),
        "genius" to listOf("🧠"),
        "brain" to listOf("🧠"),
        "dream" to listOf("💭", "✨"),
        "nightmare" to listOf("😱"),
        "peace" to listOf("✌️", "☮️"),
        "chaos" to listOf("🌀"),
        "luck" to listOf("🍀"),
        "lucky" to listOf("🍀", "🎲"),

        // Activities
        "gym" to listOf("💪", "🏋️"),
        "yoga" to listOf("🧘"),
        "run" to listOf("🏃"),
        "marathon" to listOf("🏃"),
        "dance" to listOf("💃"),
        "party" to listOf("🎉", "🥳"),
        "vacation" to listOf("🏖️", "✈️"),
        "travel" to listOf("🧳", "✈️"),
        "adventure" to listOf("🗺️"),
        "game" to listOf("🎮"),
        "movie" to listOf("🎬"),
        "music" to listOf("🎵"),
        "book" to listOf("📖"),
        "read" to listOf("📖"),
        "write" to listOf("✍️"),
        "paint" to listOf("🎨"),

        // Objects
        "key" to listOf("🔑"),
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
        "map" to listOf("🗺️"),
        "compass" to listOf("🧭"),
        "telescope" to listOf("🔭"),
        "microscope" to listOf("🔬"),

        // Nature & Weather
        "rain" to listOf("🌧️", "☔"),
        "snow" to listOf("❄️", "🌨️"),
        "storm" to listOf("⛈️"),
        "thunder" to listOf("⚡"),
        "lightning" to listOf("⚡"),
        "rainbow" to listOf("🌈"),
        "flower" to listOf("🌸"),
        "tree" to listOf("🌳"),
        "mountain" to listOf("🏔️"),
        "ocean" to listOf("🌊"),
        "wave" to listOf("🌊"),
        "wind" to listOf("💨"),
        "desert" to listOf("🏜️"),
        "volcano" to listOf("🌋"),
        "earthquake" to listOf("😱"),
        "grass" to listOf("🌿"),

        // People & Social
        "friend" to listOf("🤝"),
        "family" to listOf("👨‍👩‍👧"),
        "baby" to listOf("👶"),
        "doctor" to listOf("🩺"),
        "teacher" to listOf("👩‍🏫"),
        "lawyer" to listOf("⚖️"),
        "therapist" to listOf("🛋️"),
        "neighbor" to listOf("🏘️"),
        "stranger" to listOf("🕵️"),
        "superhero" to listOf("🦸"),
        "villain" to listOf("🦹"),
        "ghost" to listOf("👻"),
        "zombie" to listOf("🧟"),
        "alien" to listOf("🛸", "👽"),
        "wizard" to listOf("🧙"),

        // Concepts
        "karma" to listOf("☯️"),
        "destiny" to listOf("🔮"),
        "fate" to listOf("🎲"),
        "magic" to listOf("✨", "🪄"),
        "miracle" to listOf("✨"),
        "horoscope" to listOf("🔮"),
        "zodiac" to listOf("♈"),
        "crystal" to listOf("🔮"),
        "meditation" to listOf("🧘"),
        "philosophy" to listOf("🤔"),
        "science" to listOf("🔬"),
        "math" to listOf("🧮"),
        "art" to listOf("🎨"),
        "poetry" to listOf("📝"),

        // Pop Culture
        "netflix" to listOf("📺"),
        "hogwarts" to listOf("🧙"),
        "jedi" to listOf("⚔️"),
        "darth vader" to listOf("⚔️"),
        "batman" to listOf("🦇"),
        "gotham" to listOf("🌃"),
        "mordor" to listOf("🌋"),
        "avengers" to listOf("🦸"),

        // Miscellaneous
        "pants" to listOf("👖"),
        "socks" to listOf("🧦"),
        "shoes" to listOf("👟"),
        "underwear" to listOf("😳"),
        "laundry" to listOf("🧺"),
        "dishes" to listOf("🍽️"),
        "trash" to listOf("🗑️"),
        "gym" to listOf("💪"),
        "diet" to listOf("🥗"),
        "lego" to listOf("🧱"),
        "neon" to listOf("💡"),
        "sign" to listOf("🪧"),
        "secret" to listOf("🤫"),
        "whisper" to listOf("🤫"),
        "scream" to listOf("😱"),
        "applause" to listOf("👏"),
        "silence" to listOf("🤐"),
        "chess" to listOf("♟️"),
        "dice" to listOf("🎲"),
        "coin" to listOf("🪙"),
        "treasure" to listOf("💎"),
        "pirate" to listOf("🏴‍☠️"),
        "ninja" to listOf("🥷"),
        "samurai" to listOf("⚔️")
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
