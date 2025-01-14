package it.unibo.collektive.networking

import it.unibo.collektive.path.Path

/**
 * Types of messages.
 */
sealed interface Message

/**
 * [messages] received by a node from [senderId].
 */
data class InboundMessage<ID : Any>(val senderId: ID, val messages: Map<Path, *>) : Message

/**
 * An [OutboundMessage] are [messages] that a device [senderId] sends to all other neighbors.
 */
data class OutboundMessage<ID : Any>(val senderId: ID, val messages: Map<Path, SingleOutboundMessage<ID, *>>) : Message

/**
 * A [SingleOutboundMessage] contains the values associated to a [Path] in the [messages] of [OutboundMessage].
 * Has a [default] value that is sent regardless the awareness the device's neighbours, [overrides] specifies the
 * payload depending on the neighbours values.
 */
data class SingleOutboundMessage<ID : Any, Payload>(val default: Payload, val overrides: Map<ID, Payload> = emptyMap())
