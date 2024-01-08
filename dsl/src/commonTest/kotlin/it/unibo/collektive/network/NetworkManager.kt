package it.unibo.collektive.network

import it.unibo.collektive.ID
import it.unibo.collektive.networking.InboundMessage
import it.unibo.collektive.networking.OutboundMessage

/**
 * Implementation of the Network interface.
 */
class NetworkManager {
    private var messageBuffer: Set<OutboundMessage> = emptySet()

    /**
     * Adds the [message] to the message buffer.
     */
    fun send(message: OutboundMessage) {
        messageBuffer = messageBuffer.filterNot { it.senderId == message.senderId }.toSet() + message
    }

    /**
     * Return the messages directed to a specific [receiverId].
     */
    fun receive(receiverId: ID): Collection<InboundMessage> = messageBuffer
        .filterNot { it.senderId == receiverId }
        .map { received ->
            InboundMessage(
                received.senderId,
                received.messages.mapValues { (_, single) ->
                    single.overrides.getOrElse(receiverId) { single.default }
                },
            )
        }
}
