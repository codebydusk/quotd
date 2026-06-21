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
    enum class ThemeMode { AUTO, LIGHT, DARK }
    enum class CornerShape { PILL, ROUNDED, SHARP, DEFAULT }

    data class ThemePreset(
        val id: String,
        val name: String,
        val lightBg: Int,
        val lightFg: Int,
        val darkBg: Int,
        val darkFg: Int
    )

    private const val KEY_CATEGORY = "category_"
    private const val KEY_THEME_PRESET_ID = "theme_preset_id_"
    private const val KEY_THEME_MODE = "theme_mode_"
    private const val KEY_CURRENT_QUOTE = "current_quote_"
    private const val KEY_EMOJI_ENABLED = "emoji_enabled_"
    private const val KEY_FONT_SIZE = "font_size_"
    private const val KEY_FONT_FAMILY = "font_family_"
    private const val KEY_CORNER_SHAPE = "corner_shape_"

    // Defaults
    const val DEFAULT_CATEGORY = "no"
    const val DEFAULT_THEME_PRESET_ID = "default"
    val DEFAULT_THEME_MODE = ThemeMode.AUTO.name
    const val DEFAULT_FONT_SIZE = 16f
    const val DEFAULT_FONT_FAMILY = "sans-serif"
    val DEFAULT_CORNER_SHAPE = CornerShape.DEFAULT.name

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Category ──────────────────────────────────────────────────────

    fun getCategory(context: Context, appWidgetId: Int): String =
        prefs(context).getString(KEY_CATEGORY + appWidgetId, DEFAULT_CATEGORY) ?: DEFAULT_CATEGORY

    fun setCategory(context: Context, appWidgetId: Int, category: String) {
        prefs(context).edit().putString(KEY_CATEGORY + appWidgetId, category).apply()
    }

    // ── Theme Configuration ───────────────────────────────────────────

    fun getThemePresetId(context: Context, appWidgetId: Int): String =
        prefs(context).getString(KEY_THEME_PRESET_ID + appWidgetId, DEFAULT_THEME_PRESET_ID) ?: DEFAULT_THEME_PRESET_ID

    fun setThemePresetId(context: Context, appWidgetId: Int, presetId: String) {
        prefs(context).edit().putString(KEY_THEME_PRESET_ID + appWidgetId, presetId).apply()
    }

    fun getThemeMode(context: Context, appWidgetId: Int): ThemeMode {
        val modeStr = prefs(context).getString(KEY_THEME_MODE + appWidgetId, DEFAULT_THEME_MODE) ?: DEFAULT_THEME_MODE
        return try { ThemeMode.valueOf(modeStr) } catch (e: Exception) { ThemeMode.AUTO }
    }

    fun setThemeMode(context: Context, appWidgetId: Int, mode: ThemeMode) {
        prefs(context).edit().putString(KEY_THEME_MODE + appWidgetId, mode.name).apply()
    }

    fun resolveColors(context: Context, presetId: String, mode: ThemeMode): Pair<Int, Int> {
        val preset = themePresets.find { it.id == presetId } ?: themePresets.first()
        val isSystemDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val useDark = when (mode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.AUTO -> isSystemDark
        }
        if (preset.id == "default" && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val bgRes = if (useDark) android.R.color.system_neutral1_900 else android.R.color.system_neutral1_50
            val fgRes = if (useDark) android.R.color.system_neutral1_50 else android.R.color.system_neutral1_900
            return androidx.core.content.ContextCompat.getColor(context, bgRes) to androidx.core.content.ContextCompat.getColor(context, fgRes)
        }

        return if (useDark) {
            preset.darkBg to preset.darkFg
        } else {
            preset.lightBg to preset.lightFg
        }
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

    // ── Font Family ───────────────────────────────────────────────────

    fun getFontFamily(context: Context, appWidgetId: Int): String =
        prefs(context).getString(KEY_FONT_FAMILY + appWidgetId, DEFAULT_FONT_FAMILY) ?: DEFAULT_FONT_FAMILY

    fun setFontFamily(context: Context, appWidgetId: Int, family: String) {
        prefs(context).edit().putString(KEY_FONT_FAMILY + appWidgetId, family).apply()
    }

    // ── Corner Shape ──────────────────────────────────────────────────

    fun getCornerShape(context: Context, appWidgetId: Int): CornerShape {
        val shapeStr = prefs(context).getString(KEY_CORNER_SHAPE + appWidgetId, DEFAULT_CORNER_SHAPE) ?: DEFAULT_CORNER_SHAPE
        return try { CornerShape.valueOf(shapeStr) } catch (e: Exception) { CornerShape.DEFAULT }
    }

    fun setCornerShape(context: Context, appWidgetId: Int, shape: CornerShape) {
        prefs(context).edit().putString(KEY_CORNER_SHAPE + appWidgetId, shape.name).apply()
    }

    // ── Cleanup ───────────────────────────────────────────────────────

    fun deletePrefs(context: Context, appWidgetId: Int) {
        prefs(context).edit()
            .remove(KEY_CATEGORY + appWidgetId)
            .remove(KEY_THEME_PRESET_ID + appWidgetId)
            .remove(KEY_THEME_MODE + appWidgetId)
            .remove(KEY_CURRENT_QUOTE + appWidgetId)
            .remove(KEY_EMOJI_ENABLED + appWidgetId)
            .remove(KEY_FONT_SIZE + appWidgetId)
            .remove(KEY_FONT_FAMILY + appWidgetId)
            .remove(KEY_CORNER_SHAPE + appWidgetId)
            .apply()
    }

    // ── Theme Presets ─────────────────────────────────────────────────

    val themePresets: List<ThemePreset> = listOf(
        ThemePreset("default", "Default", 0xFFF5F5F5.toInt(), 0xFF1A1A1A.toInt(), 0xFF1A1A1A.toInt(), 0xFFF5F5F5.toInt()),
        ThemePreset("ubuntu", "Ubuntu", 0xFFFFFFFF.toInt(), 0xFFE95420.toInt(), 0xFF300A24.toInt(), 0xFFE95420.toInt()),
        ThemePreset("nothing_os", "Nothing OS", 0xFFFDFBFF.toInt(), 0xFFD71921.toInt(), 0xFF1B1B1D.toInt(), 0xFFD71921.toInt()),
        ThemePreset("oled_lime", "OLED Lime", 0xFF000000.toInt(), 0xFFCAFE48.toInt(), 0xFF000000.toInt(), 0xFFCAFE48.toInt()),
        ThemePreset("navy", "Navy", 0xFFE0E1DD.toInt(), 0xFF0D1B2A.toInt(), 0xFF0D1B2A.toInt(), 0xFFE0E1DD.toInt()),
        ThemePreset("forest", "Forest", 0xFFD8F3DC.toInt(), 0xFF1B4332.toInt(), 0xFF1B4332.toInt(), 0xFFD8F3DC.toInt()),
        ThemePreset("plum", "Plum", 0xFFF8E9A1.toInt(), 0xFF3C1642.toInt(), 0xFF3C1642.toInt(), 0xFFF8E9A1.toInt()),
        ThemePreset("crimson", "Crimson", 0xFFFDF0D5.toInt(), 0xFF780000.toInt(), 0xFF780000.toInt(), 0xFFFDF0D5.toInt())
    )

    // ── Typography Presets ────────────────────────────────────────────
    
    val fontSizes = listOf(12f, 14f, 16f, 20f, 24f)
    val fontSizeLabels = listOf("Extra Small", "Small", "Normal", "Large", "Extra Large")

    val fontFamilies = listOf("sans-serif", "serif", "monospace")
    val fontFamilyLabels = listOf("Sans Serif", "Serif", "Monospace")
}
