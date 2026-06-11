package com.github.codebydusk.quotd_quoteoftheday.emoji

/**
 * Interface for dynamically decorating quote text with emojis at runtime.
 * The original JSON quotes must remain emoji-free; decoration is display-only.
 */
interface EmojiDecorator {
    /**
     * Decorates the given text with contextually relevant emojis.
     * The output may vary across calls for the same input to keep things fresh.
     *
     * @param text The original, emoji-free quote text.
     * @return The decorated text, or the original text unchanged.
     */
    fun decorate(text: String): String
}
