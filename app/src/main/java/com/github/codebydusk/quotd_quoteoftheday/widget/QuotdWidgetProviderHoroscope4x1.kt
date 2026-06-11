package com.github.codebydusk.quotd_quoteoftheday.widget

/**
 * Thin subclass for the Horoscope (4×1) widget variant.
 * Android requires unique receiver class names in the manifest for each widget
 * to appear as a separate entry in the launcher's widget picker.
 * All logic is inherited from [QuotdWidgetProvider].
 */
class QuotdWidgetProviderHoroscope4x1 : QuotdWidgetProvider()
