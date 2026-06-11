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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

        // Default result is CANCELED — if user backs out, widget won't be placed
        setResult(RESULT_CANCELED)

        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Determine default category from the widget provider metadata
        val defaultCategory = determineDefaultCategory()

        enableEdgeToEdge()

        setContent {
            WidgetConfigScreen(
                defaultCategory = defaultCategory,
                onApply = { category, bgColor, fgColor ->
                    applyConfiguration(category, bgColor, fgColor)
                },
                onCancel = { finish() }
            )
        }
    }

    /**
     * Infers the default category from which widget the user selected in the picker.
     */
    private fun determineDefaultCategory(): String {
        val info = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)
        val label = info?.loadLabel(packageManager)?.lowercase() ?: ""
        return when {
            "horoscope" in label -> "horoscope"
            else -> "no"
        }
    }

    private fun applyConfiguration(category: String, bgColor: Int, fgColor: Int) {
        WidgetPrefsManager.setCategory(this, appWidgetId, category)
        WidgetPrefsManager.setBackgroundColor(this, appWidgetId, bgColor)
        WidgetPrefsManager.setForegroundColor(this, appWidgetId, fgColor)

        // Trigger initial widget update
        val appWidgetManager = AppWidgetManager.getInstance(this)
        QuotdWidgetProvider.updateWidget(this, appWidgetManager, appWidgetId)

        // Return success
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
}

// ── Compose UI ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    defaultCategory: String,
    onApply: (category: String, bgColor: Int, fgColor: Int) -> Unit,
    onCancel: () -> Unit
) {
    val categories = listOf(
        "no" to "🚫 No",
        "horoscope" to "🔮 Horoscope",
        "bad_advice" to "💀 Bad Advice",
        "emotional_damage" to "💥 Emotional Damage",
        "love_letters" to "💌 Love Letters"
    )

    var selectedCategory by remember { mutableStateOf(defaultCategory) }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    var bgColor by remember { mutableIntStateOf(WidgetPrefsManager.DEFAULT_BG_COLOR) }
    var fgColor by remember { mutableIntStateOf(WidgetPrefsManager.DEFAULT_FG_COLOR) }

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

            // ── Color Presets ──
            Text(
                text = "THEME",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondary,
                letterSpacing = 1.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WidgetPrefsManager.colorPresets.forEachIndexed { index, (presetBg, presetFg) ->
                    val isSelected = index == selectedPresetIndex
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
                                bgColor = presetBg
                                fgColor = presetFg
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
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
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

            // ── Action Buttons ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = textSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333))
                ) {
                    Text("Cancel", fontSize = 15.sp)
                }

                Button(
                    onClick = { onApply(selectedCategory, bgColor, fgColor) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = textPrimary,
                        contentColor = bgDark
                    )
                ) {
                    Text(
                        "Apply",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
