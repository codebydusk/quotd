package com.github.codebydusk.quotd_quoteoftheday.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.codebydusk.quotd_quoteoftheday.data.QuoteRepository
import com.github.codebydusk.quotd_quoteoftheday.data.WidgetPrefsManager

/**
 * Configuration activity launched when the user places a new widget.
 * Allows selecting category and customizing foreground/background colors.
 * Each widget instance gets its own independent settings.
 */
class QuotdWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the intent properly in case user backs out
        val cancelResult = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_CANCELED, cancelResult)

        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Set RESULT_OK so if the user backs out, the widget is added with current state
        val okResult = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, okResult)

        val defaultCategory = WidgetPrefsManager.getCategory(this, appWidgetId)
        val defaultThemePresetId = WidgetPrefsManager.getThemePresetId(this, appWidgetId)
        val defaultThemeMode = WidgetPrefsManager.getThemeMode(this, appWidgetId)
        val defaultEmojiEnabled = WidgetPrefsManager.isEmojiEnabled(this, appWidgetId)
        val defaultFontSize = WidgetPrefsManager.getFontSize(this, appWidgetId)
        val defaultFontFamily = WidgetPrefsManager.getFontFamily(this, appWidgetId)

        enableEdgeToEdge()

        setContent {
            WidgetConfigScreen(
                defaultCategory = defaultCategory,
                defaultThemePresetId = defaultThemePresetId,
                defaultThemeMode = defaultThemeMode,
                defaultEmojiEnabled = defaultEmojiEnabled,
                defaultFontSize = defaultFontSize,
                defaultFontFamily = defaultFontFamily,
                onStateChanged = { category, themePresetId, themeMode, emojiEnabled, fontSize, fontFamily ->
                    applyConfiguration(category, themePresetId, themeMode, emojiEnabled, fontSize, fontFamily)
                }
            )
        }
    }

    private fun applyConfiguration(category: String, themePresetId: String, themeMode: WidgetPrefsManager.ThemeMode, emojiEnabled: Boolean, fontSize: Float, fontFamily: String) {
        WidgetPrefsManager.setCategory(this, appWidgetId, category)
        WidgetPrefsManager.setThemePresetId(this, appWidgetId, themePresetId)
        WidgetPrefsManager.setThemeMode(this, appWidgetId, themeMode)
        WidgetPrefsManager.setEmojiEnabled(this, appWidgetId, emojiEnabled)
        WidgetPrefsManager.setFontSize(this, appWidgetId, fontSize)
        WidgetPrefsManager.setFontFamily(this, appWidgetId, fontFamily)

        // Trigger initial widget update
        val appWidgetManager = AppWidgetManager.getInstance(this)
        QuotdWidgetProvider.updateWidget(this, appWidgetManager, appWidgetId)
    }
}

// ── Compose UI ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    defaultCategory: String,
    defaultThemePresetId: String,
    defaultThemeMode: WidgetPrefsManager.ThemeMode,
    defaultEmojiEnabled: Boolean,
    defaultFontSize: Float,
    defaultFontFamily: String,
    onStateChanged: (category: String, themePresetId: String, themeMode: WidgetPrefsManager.ThemeMode, emojiEnabled: Boolean, fontSize: Float, fontFamily: String) -> Unit
) {
    val categories = listOf(
        "no" to "🚫 No",
        "chaos" to "🌀 Chaos",
        "bad_advice" to "💀 Bad Advice",
        "emotional_damage" to "💥 Emotional Damage",
        "love" to "💌 Love",
        "horoscope" to "🔮 Horoscope",
        "insults" to "🎯 Funny Insults",
        "office_excuses" to "💼 Office Excuses"
    )

    var selectedCategory by remember { mutableStateOf(defaultCategory) }
    var selectedPresetIndex by remember { 
        mutableIntStateOf(WidgetPrefsManager.themePresets.indexOfFirst { it.id == defaultThemePresetId }.coerceAtLeast(0))
    }
    var selectedThemeMode by remember { mutableStateOf(defaultThemeMode) }
    var emojiEnabled by remember { mutableStateOf(defaultEmojiEnabled) }
    
    val isSystemDark = isSystemInDarkTheme()
    val previewUseDark = when (selectedThemeMode) {
        WidgetPrefsManager.ThemeMode.DARK -> true
        WidgetPrefsManager.ThemeMode.LIGHT -> false
        WidgetPrefsManager.ThemeMode.AUTO -> isSystemDark
    }
    val previewPreset = WidgetPrefsManager.themePresets[selectedPresetIndex]
    val bgColor = if (previewUseDark) previewPreset.darkBg else previewPreset.lightBg
    val fgColor = if (previewUseDark) previewPreset.darkFg else previewPreset.lightFg
    
    val defaultFontSizeIndex = WidgetPrefsManager.fontSizes.indexOf(defaultFontSize).takeIf { it >= 0 } ?: 2
    var fontSizeIndex by remember { mutableFloatStateOf(defaultFontSizeIndex.toFloat()) }
    
    val defaultFontFamilyIndex = WidgetPrefsManager.fontFamilies.indexOf(defaultFontFamily).takeIf { it >= 0 } ?: 0
    var fontFamilyIndex by remember { mutableIntStateOf(defaultFontFamilyIndex) }

    LaunchedEffect(selectedCategory, selectedPresetIndex, selectedThemeMode, emojiEnabled, fontSizeIndex, fontFamilyIndex) {
        onStateChanged(
            selectedCategory,
            WidgetPrefsManager.themePresets[selectedPresetIndex].id,
            selectedThemeMode,
            emojiEnabled,
            WidgetPrefsManager.fontSizes[fontSizeIndex.toInt()],
            WidgetPrefsManager.fontFamilies[fontFamilyIndex]
        )
    }

    val bgDark = Color(0xFF0D0D0D)
    val surfaceColor = Color(0xFF1A1A1A)
    val textPrimary = Color(0xFFF5F5F5)
    val textSecondary = Color(0xFF999999)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Header ──
            Text(
                text = "quotd",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = textPrimary,
                letterSpacing = (-1).sp
            )
            Text(
                text = "Configure your widget",
                fontSize = 14.sp,
                color = textSecondary,
                modifier = Modifier.offset(y = (-16).dp)
            )

            // ── Category Selection ──
            Text(
                text = "CATEGORY",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary,
                letterSpacing = 1.5.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { (key, label) ->
                    val isSelected = selectedCategory == key
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = key },
                        color = if (isSelected) Color(0xFF2A2A2A) else surfaceColor,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444))
                        } else null
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 15.sp,
                                color = if (isSelected) textPrimary else textSecondary,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(textPrimary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // ── Theme Mode ──
            Text(
                text = "THEME MODE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary,
                letterSpacing = 1.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(WidgetPrefsManager.ThemeMode.AUTO, WidgetPrefsManager.ThemeMode.LIGHT, WidgetPrefsManager.ThemeMode.DARK)
                val modeLabels = listOf("Auto", "Light", "Dark")
                modes.forEachIndexed { index, mode ->
                    val isSelected = selectedThemeMode == mode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedThemeMode = mode },
                        color = if (isSelected) Color(0xFF2A2A2A) else surfaceColor,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444))
                        } else null
                    ) {
                        Text(
                            text = modeLabels[index],
                            fontSize = 13.sp,
                            color = if (isSelected) textPrimary else textSecondary,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Color Presets ──
            Text(
                text = "PRESET - ${WidgetPrefsManager.themePresets[selectedPresetIndex].name.uppercase()}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary,
                letterSpacing = 1.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WidgetPrefsManager.themePresets.forEachIndexed { index, preset ->
                    val isSelected = index == selectedPresetIndex
                    val presetBg = if (previewUseDark) preset.darkBg else preset.lightBg
                    val presetFg = if (previewUseDark) preset.darkFg else preset.lightFg

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(presetBg))
                            .then(
                                if (isSelected) Modifier.border(2.dp, textPrimary, CircleShape)
                                else Modifier.border(1.dp, Color(0xFF333333), CircleShape)
                            )
                            .clickable {
                                selectedPresetIndex = index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(Color(presetFg), CircleShape)
                        )
                    }
                }
            }

            // ── Emoji Toggle ──
            Text(
                text = "EMOJI",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary,
                letterSpacing = 1.5.sp
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = surfaceColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic emoji",
                            fontSize = 15.sp,
                            color = textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Sprinkle contextual emojis into quotes",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                    Switch(
                        checked = emojiEnabled,
                        onCheckedChange = { emojiEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF0D0D0D),
                            checkedTrackColor = textPrimary,
                            uncheckedThumbColor = textSecondary,
                            uncheckedTrackColor = surfaceColor,
                            uncheckedBorderColor = Color(0xFF333333)
                        )
                    )
                }
            }

            // ── Typography ──
            Text(
                text = "TYPOGRAPHY",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary,
                letterSpacing = 1.5.sp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WidgetPrefsManager.fontFamilyLabels.forEachIndexed { index, label ->
                    val isSelected = fontFamilyIndex == index
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { fontFamilyIndex = index },
                        color = if (isSelected) Color(0xFF2A2A2A) else surfaceColor,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) {
                            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444))
                        } else null
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            color = if (isSelected) textPrimary else textSecondary,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = surfaceColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    val steps = WidgetPrefsManager.fontSizes.size - 2
                    Slider(
                        value = fontSizeIndex,
                        onValueChange = { fontSizeIndex = it },
                        valueRange = 0f..(WidgetPrefsManager.fontSizes.size - 1).toFloat(),
                        steps = steps,
                        colors = SliderDefaults.colors(
                            thumbColor = textPrimary,
                            activeTrackColor = textPrimary,
                            inactiveTrackColor = Color(0xFF333333)
                        )
                    )
                    Text(
                        text = WidgetPrefsManager.fontSizeLabels[fontSizeIndex.toInt()],
                        fontSize = 14.sp,
                        color = textSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 4.dp)
                    )
                }
            }

            // ── Live Preview ──
            Text(
                text = "PREVIEW",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary,
                letterSpacing = 1.5.sp
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                color = Color(bgColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "The stars have spoken. They'd like to remain anonymous.",
                        color = Color(fgColor),
                        fontSize = WidgetPrefsManager.fontSizes[fontSizeIndex.toInt()].sp,
                        fontFamily = when (WidgetPrefsManager.fontFamilies[fontFamilyIndex]) {
                            "serif" -> FontFamily.Serif
                            "monospace" -> FontFamily.Monospace
                            else -> FontFamily.SansSerif
                        },
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⟳",
                        color = Color(fgColor),
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
