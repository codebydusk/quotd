package com.github.codebydusk.quotd_quoteoftheday.data

import android.content.Context
import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlin.random.Random

/**
 * Reads copy-success message packs from assets and returns non-repeating random messages.
 *
 * The current copy.json schema is a JSON array of strings. Object entries are also supported
 * so future packs can add metadata such as rarity without changing the call site:
 * { "text": "Forbidden knowledge acquired.", "rarity": "legendary" }
 */
object CopyMessageRepository {

    private const val TAG = "CopyMessageRepository"
    const val FALLBACK_MESSAGE = "Copied! \uD83D\uDCCB"

    enum class MessagePack(val assetFileName: String) {
        COPY("copy.json")
    }

    enum class MessageRarity(val weight: Int) {
        COMMON(100),
        UNCOMMON(40),
        RARE(15),
        EPIC(5),
        LEGENDARY(1);

        companion object {
            fun from(value: String?): MessageRarity =
                entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: COMMON
        }
    }

    data class CopyMessage(
        val text: String,
        val rarity: MessageRarity = MessageRarity.COMMON
    )

    private val cache = mutableMapOf<MessagePack, List<CopyMessage>>()
    private val lastMessageByPack = mutableMapOf<MessagePack, String>()

    fun getRandomMessage(context: Context): String =
        getRandomMessage(context, MessagePack.COPY)

    fun getRandomMessage(context: Context, messagePack: MessagePack): String = synchronized(this) {
        val messages = getMessages(context.applicationContext, messagePack)
        if (messages.isEmpty()) return FALLBACK_MESSAGE

        val eligibleMessages = if (messages.size == 1) {
            messages
        } else {
            messages.filterNot { it.text == lastMessageByPack[messagePack] }
        }

        val selected = eligibleMessages.randomWeighted()
        lastMessageByPack[messagePack] = selected.text
        selected.text
    }

    fun clearCache() = synchronized(this) {
        cache.clear()
        lastMessageByPack.clear()
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
        if (!root.isJsonArray) return emptyList()

        return root.asJsonArray.mapNotNull { element ->
            element.toCopyMessage()
        }
    }

    private fun JsonElement.toCopyMessage(): CopyMessage? = when {
        isJsonPrimitive && asJsonPrimitive.isString -> {
            asString.takeIf { it.isNotBlank() }?.let { CopyMessage(text = it) }
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

        return CopyMessage(text = text, rarity = MessageRarity.from(rarity))
    }

    private fun List<CopyMessage>.randomWeighted(): CopyMessage {
        val totalWeight = sumOf { it.rarity.weight }
        var target = Random.nextInt(totalWeight)

        for (message in this) {
            target -= message.rarity.weight
            if (target < 0) return message
        }

        return last()
    }
}
