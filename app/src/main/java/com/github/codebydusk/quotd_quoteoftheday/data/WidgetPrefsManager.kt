package com.github.codebydusk.quotd_quoteoftheday.data

import android.content.Context
import android.graphics.Color

/**
 * Manages per-widget-instance preferences using SharedPreferences.
 * Each widget has its own set of prefs keyed by appWidgetId, allowing
 * independent color schemes and categories per widget.
 */
object WidgetPrefsManager {

    private const val PREFS_NAME = "quotd_widget_prefs"
    private const val KEY_CATEGORY = "category_"
    private const val KEY_BG_COLOR = "bg_color_"
    private const val KEY_FG_COLOR = "fg_color_"
    private const val KEY_CURRENT_QUOTE = "current_quote_"
    private const val KEY_EMOJI_ENABLED = "emoji_enabled_"
    private const val KEY_FONT_SIZE = "font_size_"

    // Defaults
    const val DEFAULT_BG_COLOR = 0xFF1A1A1A.toInt()
    const val DEFAULT_FG_COLOR = 0xFFF5F5F5.toInt()
    const val DEFAULT_CATEGORY = "no"
    const val DEFAULT_FONT_SIZE = 16f

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Category ──────────────────────────────────────────────────────

    fun getCategory(context: Context, appWidgetId: Int): String =
        prefs(context).getString(KEY_CATEGORY + appWidgetId, DEFAULT_CATEGORY) ?: DEFAULT_CATEGORY

    fun setCategory(context: Context, appWidgetId: Int, category: String) {
        prefs(context).edit().putString(KEY_CATEGORY + appWidgetId, category).apply()
    }

    // ── Background Color ──────────────────────────────────────────────

    fun getBackgroundColor(context: Context, appWidgetId: Int): Int =
        prefs(context).getInt(KEY_BG_COLOR + appWidgetId, DEFAULT_BG_COLOR)

    fun setBackgroundColor(context: Context, appWidgetId: Int, color: Int) {
        prefs(context).edit().putInt(KEY_BG_COLOR + appWidgetId, color).apply()
    }

    // ── Foreground (Text) Color ───────────────────────────────────────

    fun getForegroundColor(context: Context, appWidgetId: Int): Int =
        prefs(context).getInt(KEY_FG_COLOR + appWidgetId, DEFAULT_FG_COLOR)

    fun setForegroundColor(context: Context, appWidgetId: Int, color: Int) {
        prefs(context).edit().putInt(KEY_FG_COLOR + appWidgetId, color).apply()
    }

    // ── Current Quote (for copy/revert) ───────────────────────────────

    fun getCurrentQuote(context: Context, appWidgetId: Int): String? =
        prefs(context).getString(KEY_CURRENT_QUOTE + appWidgetId, null)

    fun setCurrentQuote(context: Context, appWidgetId: Int, quote: String) {
        prefs(context).edit().putString(KEY_CURRENT_QUOTE + appWidgetId, quote).apply()
    }

    // ── Emoji Toggle ──────────────────────────────────────────────────

    fun isEmojiEnabled(context: Context, appWidgetId: Int): Boolean =
        prefs(context).getBoolean(KEY_EMOJI_ENABLED + appWidgetId, true)

    fun setEmojiEnabled(context: Context, appWidgetId: Int, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_EMOJI_ENABLED + appWidgetId, enabled).apply()
    }

    // ── Font Size ─────────────────────────────────────────────────────

    fun getFontSize(context: Context, appWidgetId: Int): Float =
        prefs(context).getFloat(KEY_FONT_SIZE + appWidgetId, DEFAULT_FONT_SIZE)

    fun setFontSize(context: Context, appWidgetId: Int, size: Float) {
        prefs(context).edit().putFloat(KEY_FONT_SIZE + appWidgetId, size).apply()
    }

    // ── Cleanup ───────────────────────────────────────────────────────

    fun deletePrefs(context: Context, appWidgetId: Int) {
        prefs(context).edit()
            .remove(KEY_CATEGORY + appWidgetId)
            .remove(KEY_BG_COLOR + appWidgetId)
            .remove(KEY_FG_COLOR + appWidgetId)
            .remove(KEY_CURRENT_QUOTE + appWidgetId)
            .remove(KEY_EMOJI_ENABLED + appWidgetId)
            .remove(KEY_FONT_SIZE + appWidgetId)
            .apply()
    }

    // ── Color Presets ─────────────────────────────────────────────────

    /** Preset color pairs (background, foreground) for the config UI. */
    val colorPresets: List<Pair<Int, Int>> = listOf(
        0xFF1A1A1A.toInt() to 0xFFF5F5F5.toInt(),  // Dark / Light (default)
        0xFFF5F5F5.toInt() to 0xFF1A1A1A.toInt(),  // Light / Dark
        0xFF0D1B2A.toInt() to 0xFFE0E1DD.toInt(),  // Navy / Cream
        0xFF1B4332.toInt() to 0xFFD8F3DC.toInt(),  // Forest / Mint
        0xFF3C1642.toInt() to 0xFFF8E9A1.toInt(),  // Plum / Gold
        0xFF2B2D42.toInt() to 0xFFEDF2F4.toInt(),  // Slate / Snow
        0xFFFDF0D5.toInt() to 0xFF003049.toInt(),  // Parchment / Ink
        0xFF780000.toInt() to 0xFFFDF0D5.toInt(),  // Crimson / Parchment
    )

    // ── Font Size Presets ─────────────────────────────────────────────
    
    val fontSizes = listOf(12f, 14f, 16f, 20f, 24f)
    val fontSizeLabels = listOf("Extra Small", "Small", "Normal", "Large", "Extra Large")
}
