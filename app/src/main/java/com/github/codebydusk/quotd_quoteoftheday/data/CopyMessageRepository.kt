package com.github.codebydusk.quotd_quoteoftheday.data

import android.content.Context
import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlin.random.Random

/**
 * Copy-success message parsed from a message pack.
 * Older string-only JSON entries are migrated to this model in memory as common messages.
 */
data class CopyMessage(
    val text: String,
    val rarity: Rarity,
    val enabled: Boolean = true
)

enum class Rarity(val selectionWeight: Int) {
    COMMON(70),
    RARE(20),
    EPIC(8),
    LEGENDARY(2);

    val jsonName: String
        get() = name.lowercase()

    companion object {
        fun from(value: String?): Rarity =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: COMMON
    }
}

/**
 * Reads copy-success message packs from assets and returns non-repeating random messages.
 */
object CopyMessageRepository {

    private const val TAG = "CopyMessageRepository"
    private const val MAX_RARITY_STREAK = 3
    const val FALLBACK_MESSAGE = "Copied! \uD83D\uDCCB"

    enum class MessagePack(val assetFileName: String) {
        COPY("copy.json")
    }

    private data class LastSelection(
        val text: String,
        val rarity: Rarity,
        val rarityStreak: Int
    )

    private val cache = mutableMapOf<MessagePack, List<CopyMessage>>()
    private val lastSelectionByPack = mutableMapOf<MessagePack, LastSelection>()

    fun getRandomMessage(context: Context): String =
        getRandomMessage(context, MessagePack.COPY)

    fun getRandomMessage(context: Context, messagePack: MessagePack): String = synchronized(this) {
        val messages = getMessages(context.applicationContext, messagePack).filter { it.enabled }
        if (messages.isEmpty()) return@synchronized FALLBACK_MESSAGE

        val lastSelection = lastSelectionByPack[messagePack]
        val textEligibleMessages = if (messages.size == 1) {
            messages
        } else {
            messages.filterNot { it.text == lastSelection?.text }.ifEmpty { messages }
        }

        val rarityEligibleMessages = if (
            lastSelection != null &&
            lastSelection.rarityStreak >= MAX_RARITY_STREAK &&
            textEligibleMessages.any { it.rarity != lastSelection.rarity }
        ) {
            textEligibleMessages.filterNot { it.rarity == lastSelection.rarity }
        } else {
            textEligibleMessages
        }

        val selected = rarityEligibleMessages.randomByRarityWeight()
        val rarityStreak = if (selected.rarity == lastSelection?.rarity) {
            lastSelection.rarityStreak + 1
        } else {
            1
        }
        lastSelectionByPack[messagePack] = LastSelection(selected.text, selected.rarity, rarityStreak)
        selected.text
    }

    fun clearCache() = synchronized(this) {
        cache.clear()
        lastSelectionByPack.clear()
    }

    private fun getMessages(context: Context, messagePack: MessagePack): List<CopyMessage> {
        cache[messagePack]?.let { return it }

        return try {
            val json = context.assets.open(messagePack.assetFileName).bufferedReader().use {
                it.readText()
            }
            parseMessages(json).also { messages ->
                cache[messagePack] = messages
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load message pack: ${messagePack.assetFileName}", e)
            emptyList<CopyMessage>().also { cache[messagePack] = it }
        }
    }

    private fun parseMessages(json: String): List<CopyMessage> {
        val root = JsonParser.parseString(json)
        require(root.isJsonArray) { "Message pack root must be a JSON array." }

        return root.asJsonArray.mapNotNull { element ->
            element.toCopyMessage()
        }
    }

    private fun JsonElement.toCopyMessage(): CopyMessage? = when {
        isJsonPrimitive && asJsonPrimitive.isString -> {
            asString.takeIf { it.isNotBlank() }?.let { text ->
                CopyMessage(text = text, rarity = Rarity.COMMON)
            }
        }

        isJsonObject -> asJsonObject.toCopyMessage()

        else -> null
    }

    private fun JsonObject.toCopyMessage(): CopyMessage? {
        val text = get("text")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
            ?.asString
            ?.takeIf { it.isNotBlank() }
            ?: return null
        val rarity = get("rarity")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
            ?.asString
        val enabled = get("enabled")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isBoolean }
            ?.asBoolean
            ?: true

        return CopyMessage(text = text, rarity = Rarity.from(rarity), enabled = enabled)
    }

    private fun List<CopyMessage>.randomByRarityWeight(): CopyMessage {
        val messagesByRarity = groupBy { it.rarity }
        val availableRarities = Rarity.entries.filter { rarity ->
            messagesByRarity[rarity].orEmpty().isNotEmpty()
        }
        val totalWeight = availableRarities.sumOf { it.selectionWeight }
        var target = Random.nextInt(totalWeight)
        val selectedRarity = availableRarities.first { rarity ->
            target -= rarity.selectionWeight
            target < 0
        }

        return messagesByRarity.getValue(selectedRarity).random()
    }
}
