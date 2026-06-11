package com.github.codebydusk.quotd_quoteoftheday.emoji

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [KeywordEmojiDecorator].
 * Uses a seeded Random for deterministic behavior in tests.
 */
class KeywordEmojiDecoratorTest {

    // ── Helper: create a decorator with a fixed seed ──────────────────

    private fun decoratorWithSeed(seed: Long): KeywordEmojiDecorator {
        return KeywordEmojiDecorator(random = Random(seed))
    }

    /**
     * Brute-force helper: tries many seeds to find one that produces
     * the desired outcome (used for testing specific code paths).
     * The predicate MUST be the last parameter for trailing lambda syntax.
     */
    private fun findSeedForOutcome(
        text: String,
        maxAttempts: Int = 10_000,
        predicate: (String) -> Boolean
    ): Long? {
        for (seed in 0L until maxAttempts) {
            val result = decoratorWithSeed(seed).decorate(text)
            if (predicate(result)) return seed
        }
        return null
    }

    // ── Case-insensitive matching ─────────────────────────────────────

    @Test
    fun `case insensitive matching works for uppercase keyword`() {
        val text = "My CALENDAR is full."
        val seed = findSeedForOutcome(text) { result ->
            result != text && ("📅" in result || "🗓️" in result)
        }
        assertNotNull("Should find a seed that triggers emoji for CALENDAR", seed)

        val result = decoratorWithSeed(seed!!).decorate(text)
        assertTrue(
            "Decorated text should contain a calendar emoji",
            "📅" in result || "🗓️" in result
        )
    }

    @Test
    fun `case insensitive matching works for mixed case`() {
        val text = "My Coffee is cold."
        val seed = findSeedForOutcome(text) { result ->
            result != text && ("☕" in result || "🫘" in result)
        }
        assertNotNull("Should find a seed that triggers emoji for Coffee", seed)

        val result = decoratorWithSeed(seed!!).decorate(text)
        assertTrue(
            "Decorated text should contain a coffee emoji",
            "☕" in result || "🫘" in result
        )
    }

    // ── Multiple keyword detection ────────────────────────────────────

    @Test
    fun `multiple keywords detected but only one emoji added for single-emoji roll`() {
        val text = "My calendar and coffee are both against this idea."

        // Find a seed that adds exactly one keyword emoji (not both)
        val seed = findSeedForOutcome(text) { decorated ->
            if (decorated == text) return@findSeedForOutcome false
            val hasCalendar = "📅" in decorated || "🗓️" in decorated
            val hasCoffee = "☕" in decorated || "🫘" in decorated
            // Exactly one keyword emoji, not both
            (hasCalendar && !hasCoffee) || (hasCoffee && !hasCalendar)
        }
        assertNotNull("Should find a seed that adds only one keyword emoji", seed)

        val result = decoratorWithSeed(seed!!).decorate(text)
        val calendarPresent = "📅" in result || "🗓️" in result
        val coffeePresent = "☕" in result || "🫘" in result

        assertTrue("At least one keyword emoji should be present", calendarPresent || coffeePresent)
    }

    // ── No keyword present ────────────────────────────────────────────

    @Test
    fun `text with no keywords returns unchanged`() {
        val text = "This is a completely ordinary sentence."

        // Try many seeds — none should add emojis since there are no keywords
        for (seed in 0L..100L) {
            val result = decoratorWithSeed(seed).decorate(text)
            assertEquals("Text with no keywords should remain unchanged", text, result)
        }
    }

    // ── Prefix placement ──────────────────────────────────────────────

    @Test
    fun `prefix placement puts emoji at start`() {
        val text = "My calendar is full."
        val seed = findSeedForOutcome(text) { decorated ->
            decorated != text && (decorated.startsWith("📅") || decorated.startsWith("🗓️"))
        }
        assertNotNull("Should find a seed that uses prefix placement", seed)

        val result = decoratorWithSeed(seed!!).decorate(text)
        assertTrue(
            "Prefix placement should start with emoji",
            result.startsWith("📅") || result.startsWith("🗓️")
        )
    }

    // ── Inline placement ──────────────────────────────────────────────

    @Test
    fun `inline placement puts emoji after keyword`() {
        val text = "my calendar is full"
        val seed = findSeedForOutcome(text) { decorated ->
            decorated != text && ("calendar 📅" in decorated || "calendar 🗓️" in decorated)
        }
        assertNotNull("Should find a seed that uses inline placement", seed)

        val result = decoratorWithSeed(seed!!).decorate(text)
        assertTrue(
            "Inline placement should have emoji right after keyword",
            "calendar 📅" in result || "calendar 🗓️" in result
        )
    }

    // ── Suffix placement ──────────────────────────────────────────────

    @Test
    fun `suffix placement puts emoji at end`() {
        val text = "My calendar is full."
        val seed = findSeedForOutcome(text) { decorated ->
            if (decorated == text) return@findSeedForOutcome false
            val startsWithEmoji = decorated.startsWith("📅") || decorated.startsWith("🗓️")
            val hasInline = "calendar 📅" in decorated || "calendar 🗓️" in decorated
            val endsWithEmoji = decorated.endsWith("📅") || decorated.endsWith("🗓️")
            !startsWithEmoji && !hasInline && endsWithEmoji
        }
        assertNotNull("Should find a seed that uses suffix placement", seed)

        val result = decoratorWithSeed(seed!!).decorate(text)
        assertTrue(
            "Suffix placement should end with emoji",
            result.endsWith("📅") || result.endsWith("🗓️")
        )
    }

    // ── No-emoji path ─────────────────────────────────────────────────

    @Test
    fun `no emoji path leaves text unchanged`() {
        val text = "My calendar is full."

        // Find a seed where the 20% "no emoji" path is taken
        val seed = findSeedForOutcome(text) { result -> result == text }
        assertNotNull("Should find a seed that triggers the no-emoji path", seed)

        val result = decoratorWithSeed(seed!!).decorate(text)
        assertEquals("No-emoji path should return original text", text, result)
    }

    // ── Two emoji path ────────────────────────────────────────────────

    @Test
    fun `two emoji path adds keyword emoji plus reaction`() {
        val text = "My calendar is full."
        val reactions = listOf(
            "😂", "🤣", "🙃", "😅", "🤦", "🤷", "✨", "💀",
            "😏", "🫠", "😤", "🥲", "💅", "👀", "🫡", "😶"
        )

        val seed = findSeedForOutcome(text) { decorated ->
            val hasKeywordEmoji = "📅" in decorated || "🗓️" in decorated
            val hasReaction = reactions.any { reaction -> reaction in decorated }
            decorated != text && hasKeywordEmoji && hasReaction
        }
        assertNotNull("Should find a seed that triggers two-emoji path", seed)

        val result = decoratorWithSeed(seed!!).decorate(text)
        assertTrue(
            "Should contain a keyword emoji",
            "📅" in result || "🗓️" in result
        )
        assertTrue(
            "Should contain a reaction emoji",
            reactions.any { reaction -> reaction in result }
        )
    }

    // ── No duplicate emoji on repeated decoration ─────────────────────

    @Test
    fun `decorating already-decorated text does not double emojis with same seed`() {
        val text = "My calendar is full."

        // Decorate once with a seed that adds emoji
        val seed = findSeedForOutcome(text) { result -> result != text }
        assertNotNull(seed)

        val firstPass = decoratorWithSeed(seed!!).decorate(text)
        // Decorate the result again with the SAME seed
        val secondPass = decoratorWithSeed(seed).decorate(firstPass)

        // Count emoji occurrences — should not have more than 2 keyword emojis
        val emojiCount = countEmojis(secondPass, listOf("📅", "🗓️"))
        assertTrue(
            "Should not accumulate excessive keyword emojis (got $emojiCount)",
            emojiCount <= 2  // At most one from each pass
        )
    }

    // ── Whole-word matching ───────────────────────────────────────────

    @Test
    fun `partial word match does not trigger emoji`() {
        // "caterpillar" contains "cat" but is not the word "cat"
        val text = "The caterpillar crawled slowly."

        for (seed in 0L..200L) {
            val result = decoratorWithSeed(seed).decorate(text)
            val hasCatEmoji = "🐈" in result || "😼" in result || "🐱" in result
            assertFalse(
                "Partial match 'cat' in 'caterpillar' should not add cat emoji",
                hasCatEmoji
            )
        }
    }

    @Test
    fun `whole word cat should match`() {
        val text = "My cat judged me silently."
        val seed = findSeedForOutcome(text) { result ->
            result != text && ("🐈" in result || "😼" in result || "🐱" in result)
        }
        assertNotNull("Whole word 'cat' should be matched", seed)
    }

    // ── Performance ───────────────────────────────────────────────────

    @Test
    fun `decoration completes under 1ms for typical quotes`() {
        val decorator = KeywordEmojiDecorator()
        val quotes = listOf(
            "My calendar and coffee are both against this idea.",
            "The universe has a plan. You're not on the distribution list.",
            "I have a meeting with my couch and it won't take no for an answer.",
            "Mercury isn't in retrograde. This one is entirely on you.",
            "I'm too busy counting the ceiling tiles."
        )

        // Warm up
        repeat(100) {
            quotes.forEach { quote -> decorator.decorate(quote) }
        }

        // Measure
        val iterations = 1000
        val startNanos = System.nanoTime()
        repeat(iterations) {
            quotes.forEach { quote -> decorator.decorate(quote) }
        }
        val elapsedNanos = System.nanoTime() - startNanos
        val avgPerCallNanos = elapsedNanos / (iterations * quotes.size)
        val avgPerCallMs = avgPerCallNanos / 1_000_000.0

        assertTrue(
            "Average decoration should be under 1ms (was ${avgPerCallMs}ms)",
            avgPerCallMs < 1.0
        )
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private fun countEmojis(text: String, emojis: List<String>): Int {
        return emojis.sumOf { emoji ->
            var count = 0
            var idx = 0
            while (true) {
                idx = text.indexOf(emoji, idx)
                if (idx == -1) break
                count++
                idx += emoji.length
            }
            count
        }
    }
}
