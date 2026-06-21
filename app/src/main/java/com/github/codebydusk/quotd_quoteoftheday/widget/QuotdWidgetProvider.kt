package com.github.codebydusk.quotd_quoteoftheday.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import com.github.codebydusk.quotd_quoteoftheday.R
import com.github.codebydusk.quotd_quoteoftheday.data.CopyMessageRepository
import com.github.codebydusk.quotd_quoteoftheday.data.QuoteRepository
import com.github.codebydusk.quotd_quoteoftheday.data.WidgetPrefsManager
import com.github.codebydusk.quotd_quoteoftheday.emoji.KeywordEmojiDecorator

/** A single, configurable AppWidgetProvider with compact and expanded layouts. */
class QuotdWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.github.codebydusk.quotd_quoteoftheday.ACTION_REFRESH"
        const val ACTION_COPY = "com.github.codebydusk.quotd_quoteoftheday.ACTION_COPY"
        const val ACTION_REVERT = "com.github.codebydusk.quotd_quoteoftheday.ACTION_REVERT"
        const val ACTION_AUTO_REFRESH = "com.github.codebydusk.quotd_quoteoftheday.ACTION_AUTO_REFRESH"
        const val EXTRA_WIDGET_ID = "extra_widget_id"
        const val EXTRA_RECEIVER_CLASS = "extra_receiver_class"

        private const val REFRESH_INTERVAL_MS = 60 * 60 * 1000L // 1 hour
        private const val REVERT_DELAY_MS = 2000L

        /** Shared emoji decorator instance (stateless, thread-safe). */
        private val emojiDecorator = KeywordEmojiDecorator()

        private val receiverClass = QuotdWidgetProvider::class.java

        /**
         * Determines the layout resource from the widget's current resize options.
         * Widgets taller than 60dp use the expanded layout.
         */
        fun getLayoutForWidget(context: Context, appWidgetId: Int): Int {
            val manager = AppWidgetManager.getInstance(context)
            val currentHeight = manager.getAppWidgetOptions(appWidgetId)
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                .takeIf { it > 0 }
                ?: manager.getAppWidgetInfo(appWidgetId)?.minHeight
                ?: 0
            return if (currentHeight >= 100) {
                R.layout.widget_layout_4x2
            } else {
                R.layout.widget_layout_4x1
            }
        }

        /**
         * Resolves which receiver class owns a given appWidgetId.
         */
        fun getReceiverClass(context: Context, appWidgetId: Int): Class<out QuotdWidgetProvider> {
            val manager = AppWidgetManager.getInstance(context)
            val info = manager.getAppWidgetInfo(appWidgetId) ?: return QuotdWidgetProvider::class.java
            val providerName = info.provider.className
            return try {
                @Suppress("UNCHECKED_CAST")
                Class.forName(providerName) as Class<out QuotdWidgetProvider>
            } catch (_: Exception) {
                QuotdWidgetProvider::class.java
            }
        }

        /**
         * Updates a single widget with a new random quote.
         */
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val category = WidgetPrefsManager.getCategory(context, appWidgetId)
            val presetId = WidgetPrefsManager.getThemePresetId(context, appWidgetId)
            val themeMode = WidgetPrefsManager.getThemeMode(context, appWidgetId)
            val (bgColor, fgColor, accentColor) = WidgetPrefsManager.resolveColors(context, presetId, themeMode)
            val fontSize = WidgetPrefsManager.getFontSize(context, appWidgetId)
            val fontFamily = WidgetPrefsManager.getFontFamily(context, appWidgetId)
            val cornerShape = WidgetPrefsManager.getCornerShape(context, appWidgetId)
            val quote = QuoteRepository.getRandomQuote(context, category)

            // Apply emoji decoration if enabled for this widget
            val displayText = if (WidgetPrefsManager.isEmojiEnabled(context, appWidgetId)) {
                emojiDecorator.decorate(quote)
            } else {
                quote
            }

            // Store the finalized (decorated) quote for clipboard copy and layout resize
            WidgetPrefsManager.setCurrentQuote(context, appWidgetId, displayText)

            val layoutId = getLayoutForWidget(context, appWidgetId)
            val views = buildRemoteViews(context, appWidgetId, layoutId, displayText, bgColor, fgColor, accentColor, fontSize, fontFamily, cornerShape)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /** Rebuilds a widget after resizing without replacing its current quote. */
        fun updateWidgetLayout(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val displayText = WidgetPrefsManager.getCurrentQuote(context, appWidgetId)
                ?: run {
                    updateWidget(context, appWidgetManager, appWidgetId)
                    return
                }
            val presetId = WidgetPrefsManager.getThemePresetId(context, appWidgetId)
            val themeMode = WidgetPrefsManager.getThemeMode(context, appWidgetId)
            val (bgColor, fgColor, accentColor) = WidgetPrefsManager.resolveColors(context, presetId, themeMode)
            val fontSize = WidgetPrefsManager.getFontSize(context, appWidgetId)
            val fontFamily = WidgetPrefsManager.getFontFamily(context, appWidgetId)
            val cornerShape = WidgetPrefsManager.getCornerShape(context, appWidgetId)
            
            val layoutId = getLayoutForWidget(context, appWidgetId)
            val views = buildRemoteViews(context, appWidgetId, layoutId, displayText, bgColor, fgColor, accentColor, fontSize, fontFamily, cornerShape)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Builds the RemoteViews with text, colors, and click listeners.
         */
        private fun buildRemoteViews(
            context: Context,
            appWidgetId: Int,
            layoutId: Int,
            text: String,
            bgColor: Int,
            fgColor: Int,
            accentColor: Int,
            fontSize: Float,
            fontFamily: String,
            cornerShape: WidgetPrefsManager.CornerShape
        ): RemoteViews {
            val receiverClass = getReceiverClass(context, appWidgetId)
            val views = RemoteViews(context.packageName, layoutId)

            val spannableString = android.text.SpannableString(text)
            spannableString.setSpan(android.text.style.TypefaceSpan(fontFamily), 0, text.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            views.setTextViewText(R.id.widget_text, spannableString)
            views.setTextColor(R.id.widget_text, fgColor)
            views.setTextViewTextSize(R.id.widget_text, android.util.TypedValue.COMPLEX_UNIT_SP, fontSize)

            val shapeRes = when (cornerShape) {
                WidgetPrefsManager.CornerShape.PILL -> R.drawable.widget_shape_pill
                WidgetPrefsManager.CornerShape.ROUNDED -> R.drawable.widget_shape_rounded
                WidgetPrefsManager.CornerShape.SHARP -> R.drawable.widget_shape_sharp
                WidgetPrefsManager.CornerShape.DEFAULT -> R.drawable.widget_shape_default
            }
            views.setImageViewResource(R.id.widget_background_image, shapeRes)
            views.setInt(R.id.widget_background_image, "setColorFilter", bgColor)
            
            views.setInt(R.id.widget_refresh, "setColorFilter", accentColor)

            // Copy click
            val copyIntent = Intent(context, receiverClass).apply {
                action = ACTION_COPY
                putExtra(EXTRA_WIDGET_ID, appWidgetId)
            }
            views.setOnClickPendingIntent(
                R.id.widget_text,
                PendingIntent.getBroadcast(
                    context, appWidgetId * 10 + 1, copyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            // Refresh click
            val refreshIntent = Intent(context, receiverClass).apply {
                action = ACTION_REFRESH
                putExtra(EXTRA_WIDGET_ID, appWidgetId)
            }
            views.setOnClickPendingIntent(
                R.id.widget_refresh,
                PendingIntent.getBroadcast(
                    context, appWidgetId * 10 + 2, refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            return views
        }

        /**
         * Schedules auto-refresh for a specific widget.
         */
        fun scheduleAutoRefresh(context: Context, appWidgetId: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, receiverClass).apply {
                action = ACTION_AUTO_REFRESH
                putExtra(EXTRA_WIDGET_ID, appWidgetId)
            }
            val pending = PendingIntent.getBroadcast(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val interval = WidgetPrefsManager.getRefreshInterval(context, appWidgetId)
            alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + interval,
                interval,
                pending
            )
        }

        /**
         * Cancels auto-refresh alarm for a specific widget.
         */
        fun cancelAutoRefresh(context: Context, appWidgetId: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, receiverClass).apply {
                action = ACTION_AUTO_REFRESH
                putExtra(EXTRA_WIDGET_ID, appWidgetId)
            }
            val pending = PendingIntent.getBroadcast(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pending)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
            scheduleAutoRefresh(context, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidgetLayout(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_AUTO_REFRESH -> {
                val id = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val manager = AppWidgetManager.getInstance(context)
                    updateWidget(context, manager, id)
                }
                return
            }
        }

        val appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val appWidgetManager = AppWidgetManager.getInstance(context)

        when (intent.action) {
            ACTION_REFRESH -> {
                updateWidget(context, appWidgetManager, appWidgetId)
            }

            ACTION_COPY -> {
                val quote = WidgetPrefsManager.getCurrentQuote(context, appWidgetId) ?: return

                // Copy to clipboard
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("quotd", quote)
                clipboard.setPrimaryClip(clip)

                // Show a copy-success message in the widget
                val presetId = WidgetPrefsManager.getThemePresetId(context, appWidgetId)
                val themeMode = WidgetPrefsManager.getThemeMode(context, appWidgetId)
                val (bgColor, fgColor, accentColor) = WidgetPrefsManager.resolveColors(context, presetId, themeMode)
                val fontSize = WidgetPrefsManager.getFontSize(context, appWidgetId)
                val fontFamily = WidgetPrefsManager.getFontFamily(context, appWidgetId)
                val cornerShape = WidgetPrefsManager.getCornerShape(context, appWidgetId)
                val layoutId = getLayoutForWidget(context, appWidgetId)
                val copyMessage = CopyMessageRepository.getRandomMessage(context)
                val views = buildRemoteViews(context, appWidgetId, layoutId, copyMessage, bgColor, fgColor, accentColor, fontSize, fontFamily, cornerShape)
                val receiverClass = getReceiverClass(context, appWidgetId)

                appWidgetManager.updateAppWidget(appWidgetId, views)

                // Schedule revert after 2 seconds
                val revertIntent = Intent(context, receiverClass).apply {
                    action = ACTION_REVERT
                    putExtra(EXTRA_WIDGET_ID, appWidgetId)
                }
                val revertPending = PendingIntent.getBroadcast(
                    context, appWidgetId * 10 + 3, revertIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                try {
                    alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME,
                        SystemClock.elapsedRealtime() + REVERT_DELAY_MS,
                        revertPending
                    )
                } catch (e: SecurityException) {
                    // Fallback for Android 14+ (e.g. Realme UI 5) where EXACT_ALARM is denied by default
                    val pendingResult = goAsync()
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        try {
                            context.sendBroadcast(revertIntent)
                        } finally {
                            pendingResult.finish()
                        }
                    }, REVERT_DELAY_MS)
                }
            }

            ACTION_REVERT -> {
                val quote = WidgetPrefsManager.getCurrentQuote(context, appWidgetId) ?: return
                val presetId = WidgetPrefsManager.getThemePresetId(context, appWidgetId)
                val themeMode = WidgetPrefsManager.getThemeMode(context, appWidgetId)
                val (bgColor, fgColor, accentColor) = WidgetPrefsManager.resolveColors(context, presetId, themeMode)
                val fontSize = WidgetPrefsManager.getFontSize(context, appWidgetId)
                val fontFamily = WidgetPrefsManager.getFontFamily(context, appWidgetId)
                val cornerShape = WidgetPrefsManager.getCornerShape(context, appWidgetId)
                val layoutId = getLayoutForWidget(context, appWidgetId)

                val views = buildRemoteViews(context, appWidgetId, layoutId, quote, bgColor, fgColor, accentColor, fontSize, fontFamily, cornerShape)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) {
            cancelAutoRefresh(context, id)
            WidgetPrefsManager.deletePrefs(context, id)
        }
    }
}
