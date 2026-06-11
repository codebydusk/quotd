package com.github.codebydusk.quotd_quoteoftheday.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Reads quote JSON arrays from the assets folder and provides random quotes.
 * Caches loaded arrays in memory to avoid repeated I/O.
 */
object QuoteRepository {

    private val cache = mutableMapOf<String, List<String>>()
    private val gson = Gson()

    /**
     * Available categories mapped to their asset filenames.
     */
    val categories = mapOf(
        "no" to "no.json",
        "horoscope" to "horoscope.json",
        "bad_advice" to "bad_advice.json",
        "emotional_damage" to "emotional_damage.json",
        "love_letters" to "love_letters.json"
    )

    /**
     * Returns a random quote from the specified category.
     * Falls back to a default message if the category is empty or missing.
     */
    fun getRandomQuote(context: Context, category: String): String {
        val quotes = getQuotes(context, category)
        return if (quotes.isNotEmpty()) {
            quotes.random()
        } else {
            "No quotes available yet."
        }
    }

    /**
     * Loads and caches the quote list for a given category.
     */
    private fun getQuotes(context: Context, category: String): List<String> {
        cache[category]?.let { return it }

        val filename = categories[category] ?: return emptyList()
        return try {
            val json = context.assets.open(filename).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<String>>() {}.type
            val quotes: List<String> = gson.fromJson(json, type)
            cache[category] = quotes
            quotes
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Clears the in-memory cache (useful if assets are updated).
     */
    fun clearCache() {
        cache.clear()
    }
}
