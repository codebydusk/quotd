package com.github.codebydusk.quotd_quoteoftheday.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import com.github.codebydusk.quotd_quoteoftheday.R
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
        val defaultCornerShape = WidgetPrefsManager.getCornerShape(this, appWidgetId)
        val defaultRefreshInterval = WidgetPrefsManager.getRefreshInterval(this, appWidgetId)

        enableEdgeToEdge()

        setContent {
            WidgetConfigScreen(
                defaultCategory = defaultCategory,
                defaultThemePresetId = defaultThemePresetId,
                defaultThemeMode = defaultThemeMode,
                defaultEmojiEnabled = defaultEmojiEnabled,
                defaultFontSize = defaultFontSize,
                defaultFontFamily = defaultFontFamily,
                defaultCornerShape = defaultCornerShape,
                defaultRefreshInterval = defaultRefreshInterval,
                onStateChanged = { category, themePresetId, themeMode, emojiEnabled, fontSize, fontFamily, cornerShape, refreshInterval ->
                    applyConfiguration(category, themePresetId, themeMode, emojiEnabled, fontSize, fontFamily, cornerShape, refreshInterval)
                }
            )
        }
    }

    private fun applyConfiguration(category: String, themePresetId: String, themeMode: WidgetPrefsManager.ThemeMode, emojiEnabled: Boolean, fontSize: Float, fontFamily: String, cornerShape: WidgetPrefsManager.CornerShape, refreshInterval: Long) {
        WidgetPrefsManager.setCategory(this, appWidgetId, category)
        WidgetPrefsManager.setThemePresetId(this, appWidgetId, themePresetId)
        WidgetPrefsManager.setThemeMode(this, appWidgetId, themeMode)
        WidgetPrefsManager.setEmojiEnabled(this, appWidgetId, emojiEnabled)
        WidgetPrefsManager.setFontSize(this, appWidgetId, fontSize)
        WidgetPrefsManager.setFontFamily(this, appWidgetId, fontFamily)
        WidgetPrefsManager.setCornerShape(this, appWidgetId, cornerShape)
        WidgetPrefsManager.setRefreshInterval(this, appWidgetId, refreshInterval)

        // Trigger initial widget update and auto-refresh schedule
        val appWidgetManager = AppWidgetManager.getInstance(this)
        QuotdWidgetProvider.updateWidget(this, appWidgetManager, appWidgetId)
        QuotdWidgetProvider.scheduleAutoRefresh(this, appWidgetId)
    }
}

// ── Compose UI ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionContainer(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF999999),
                    letterSpacing = 1.5.sp
                )
            }
            content()
        }
    }
}

@Composable
fun WidgetConfigScreen(
    defaultCategory: String,
    defaultThemePresetId: String,
    defaultThemeMode: WidgetPrefsManager.ThemeMode,
    defaultEmojiEnabled: Boolean,
    defaultFontSize: Float,
    defaultFontFamily: String,
    defaultCornerShape: WidgetPrefsManager.CornerShape,
    defaultRefreshInterval: Long,
    onStateChanged: (category: String, themePresetId: String, themeMode: WidgetPrefsManager.ThemeMode, emojiEnabled: Boolean, fontSize: Float, fontFamily: String, cornerShape: WidgetPrefsManager.CornerShape, refreshInterval: Long) -> Unit
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
    val accentColor = if (previewUseDark) previewPreset.darkAccent else previewPreset.lightAccent
    
    val defaultFontSizeIndex = WidgetPrefsManager.fontSizes.indexOf(defaultFontSize).takeIf { it >= 0 } ?: 2
    var fontSizeIndex by remember { mutableFloatStateOf(defaultFontSizeIndex.toFloat()) }
    
    val defaultFontFamilyIndex = WidgetPrefsManager.fontFamilies.indexOf(defaultFontFamily).takeIf { it >= 0 } ?: 0
    var fontFamilyIndex by remember { mutableIntStateOf(defaultFontFamilyIndex) }

    var selectedCornerShape by remember { mutableStateOf(defaultCornerShape) }

    val defaultRefreshIntervalIndex = WidgetPrefsManager.refreshIntervals.indexOf(defaultRefreshInterval).takeIf { it >= 0 } ?: 6
    var refreshIntervalIndex by remember { mutableFloatStateOf(defaultRefreshIntervalIndex.toFloat()) }

    LaunchedEffect(selectedCategory, selectedPresetIndex, selectedThemeMode, emojiEnabled, fontSizeIndex, fontFamilyIndex, selectedCornerShape, refreshIntervalIndex) {
        onStateChanged(
            selectedCategory,
            WidgetPrefsManager.themePresets[selectedPresetIndex].id,
            selectedThemeMode,
            emojiEnabled,
            WidgetPrefsManager.fontSizes[fontSizeIndex.toInt()],
            WidgetPrefsManager.fontFamilies[fontFamilyIndex],
            selectedCornerShape,
            WidgetPrefsManager.refreshIntervals[refreshIntervalIndex.toInt()]
        )
    }

    val bgDark = Color(0xFF0D0D0D)
    val surfaceColor = Color.Transparent
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(64.dp).padding(end = 16.dp)
                )
                Column {
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
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // ── Category Selection ──
            SectionContainer(title = "CATEGORY") {
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
            }

            // ── Theme Mode ──
            SectionContainer(title = "THEME MODE") {
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
            }

            // ── Color Presets ──
            SectionContainer(title = "THEME: ${WidgetPrefsManager.themePresets[selectedPresetIndex].name}") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    WidgetPrefsManager.themePresets.chunked(4).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowPresets.forEach { preset ->
                                val index = WidgetPrefsManager.themePresets.indexOf(preset)
                                val isSelected = index == selectedPresetIndex
                                val presetBg = if (previewUseDark) preset.darkBg else preset.lightBg
                                val presetFg = if (previewUseDark) preset.darkFg else preset.lightFg

                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(presetBg))
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, textPrimary, CircleShape)
                                            else Modifier.border(1.dp, Color(0xFF333333), CircleShape)
                                        )
                                        .clickable {
                                            selectedPresetIndex = index
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(Color(presetFg), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Emoji Toggle ──
            SectionContainer(title = "EMOJI") {
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
            }

            // ── Typography ──
            SectionContainer(title = "TYPOGRAPHY") {
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
            }

            // ── Corner Shape ──
            SectionContainer(title = "WIDGET CORNERS") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val shapes = listOf(
                        WidgetPrefsManager.CornerShape.PILL to "Pill",
                        WidgetPrefsManager.CornerShape.ROUNDED to "Rounded",
                        WidgetPrefsManager.CornerShape.SHARP to "Sharp",
                        WidgetPrefsManager.CornerShape.DEFAULT to "OS Default"
                    )
                    shapes.forEach { (shape, label) ->
                        val isSelected = selectedCornerShape == shape
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCornerShape = shape },
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
            }

            // ── Auto-Refresh ──
            SectionContainer(title = "AUTO-REFRESH INTERVAL") {
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
                        val steps = WidgetPrefsManager.refreshIntervals.size - 2
                        Slider(
                            value = refreshIntervalIndex,
                            onValueChange = { refreshIntervalIndex = it },
                            valueRange = 0f..(WidgetPrefsManager.refreshIntervals.size - 1).toFloat(),
                            steps = steps,
                            colors = SliderDefaults.colors(
                                thumbColor = textPrimary,
                                activeTrackColor = textPrimary,
                                inactiveTrackColor = Color(0xFF333333)
                            )
                        )
                        Text(
                            text = WidgetPrefsManager.refreshIntervalLabels[refreshIntervalIndex.toInt()],
                            fontSize = 14.sp,
                            color = textSecondary,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 4.dp)
                        )
                    }
                }
            }

            // ── Live Preview ──
            SectionContainer(title = "PREVIEW") {
                val previewShape = when (selectedCornerShape) {
                    WidgetPrefsManager.CornerShape.PILL -> RoundedCornerShape(100.dp)
                    WidgetPrefsManager.CornerShape.ROUNDED -> RoundedCornerShape(24.dp)
                    WidgetPrefsManager.CornerShape.SHARP -> RoundedCornerShape(0.dp)
                    WidgetPrefsManager.CornerShape.DEFAULT -> RoundedCornerShape(16.dp)
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    color = Color(bgColor),
                    shape = previewShape
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
                            color = Color(accentColor),
                            fontSize = 20.sp
                        )
                    }
                }

                Text(
                    text = "* Sharp corners might be overridden by your launcher's default widget styling on Android 12+",
                    fontSize = 11.sp,
                    color = textSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
