package it.unibo.collektive.networking

import it.unibo.collektive.path.Path
import it.unibo.collektive.path.PathSummary

/**
 * Types of messages.
 */
sealed interface Message

/**
 * [messages] received by a node from [senderId].
 */
data class InboundMessage<ID : Any>(val senderId: ID, val messages: Map<PathSummary, *>) : Message

/**
 * An [OutboundMessage] are messages that a device [senderId] sends to all other neighbors.
 */
class OutboundMessage<ID : Any>(
    expectedSize: Int,
    val senderId: ID,
) : Message {

    private val overrides: MutableMap<ID, MutableList<Pair<PathSummary, Any?>>> = LinkedHashMap(expectedSize * 2)
    private val defaults: MutableMap<PathSummary, Any?> = LinkedHashMap(expectedSize * 2)

    /**
     * Check if the [OutboundMessage] is empty.
     */
    fun isEmpty(): Boolean = defaults.isEmpty()

    /**
     * Check if the [OutboundMessage] is not empty.
     */
    fun isNotEmpty(): Boolean = defaults.isNotEmpty()

    /**
     * Returns the messages for device [id].
     */
    fun messagesFor(id: ID): Map<PathSummary, *> = LinkedHashMap<PathSummary, Any?>(
        defaults.size + overrides.size,
        1.0f,
    ).also { result ->
        result.putAll(defaults)
        overrides[id]?.let { result.putAll(it) }
    }

    /**
     * Add a [message] to the [OutboundMessage].
     */
    fun addMessage(path: PathSummary, message: SingleOutboundMessage<ID, *>) {
        check(!defaults.containsKey(path)) {
            """
            Aggregate alignment clash by multiple aligned calls originated at the same path: $path.
            The most likely cause is an aggregate function call within a loop without proper manual alignment.
            """.trimIndent()
        }
        defaults[path] = message.default
//        mutableMessages[path] = message
        message.overrides.forEach { (id, value) ->
            val destination = overrides.getOrPut(id) { mutableListOf() }
            destination += path to value
        }
    }
}

/**
 * A [SingleOutboundMessage] contains the values associated to a [Path] in the messages of [OutboundMessage].
 * Has a [default] value that is sent regardless the awareness the device's neighbours, [overrides] specifies the
 * payload depending on the neighbours values.
 */
data class SingleOutboundMessage<ID : Any, Payload>(val default: Payload, val overrides: Map<ID, Payload> = emptyMap())
